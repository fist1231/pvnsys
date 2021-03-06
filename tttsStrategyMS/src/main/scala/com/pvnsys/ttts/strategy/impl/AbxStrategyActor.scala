package com.pvnsys.ttts.strategy.impl

import akka.actor.{Actor, ActorLogging, Props, PoisonPill, ActorRef}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.pvnsys.ttts.strategy.util.Utils
import java.sql._
import kx.c
import kx.c._
import kx.c.Flip
import scala.Array
import com.pvnsys.ttts.strategy.db.ReadKdbActor
import com.pvnsys.ttts.strategy.db.WriteKdbActor
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import java.util.Date



object AbxStrategyActor {

  import TttsStrategyMessages._
  /*
   *   
   */ 
  sealed trait AbxStrategyMessages
  case class StartAbxStrategyMessage(message: TttsStrategyMessage, serviceId: String) extends AbxStrategyMessages
  case object StopAbxStrategyMessage extends AbxStrategyMessages
  case class AbxStrategyResponseMessage(message: TttsStrategyMessage) extends AbxStrategyMessages

}

/**
 * abx strategy.
 * 
 */
class AbxStrategyActor extends Actor with ActorLogging {

  import TttsStrategyMessages._
  import Strategy._
  import WriteKdbActor._
  import ReadKdbActor._
  import AbxStrategyActor._
  
  override def receive = {
    case m: StartAbxStrategyMessage => {
      log.debug("AbxStrategyActor received StartAbxStrategyMessage: {}", m)
	  val client = sender()
	  val host = self
      val result = execute(client, host, m.message, m.serviceId)
//      client ! result
      
    }
    case StopAbxStrategyMessage => {
      log.debug("AbxStrategyActor received StopAbxStrategyMessage")
      context stop self
    }
  }  
  
/*
 * Do Strategy processing, create ResponseStrategyFacadeTopicMessage (reply to FacadeMS)
 * 
 */ 
  private def execute(client: ActorRef, host: ActorRef, msg: TttsStrategyMessage, serviceId: String) = {
    
    val tableId = constructTableId(msg, serviceId)
    
    /*
     * - Assign unique message id and timestamp generated by Utils.generateMessageTraits.
     * - Pass along client, payload and sequenceNum from Strategy message.
     * - Add signal field
     * - Return ResponseStrategyFacadeTopicMessage or ResponseStrategyServiceTopicMessage, depending on incoming message type.
     */ 
    
	val sequenceNum = msg match {
	  case x if msg.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage].sequenceNum  
	  case x if msg.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage].sequenceNum
	  case _ => ""
	}
    
	implicit val timeout = Timeout(2 seconds)
	// First message in request - reset strategy schema
	if(sequenceNum.toInt == 1) {
	    val writeKdbActor = context.actorOf(WriteKdbActor.props(tableId))
	    val resetSchemaResult: Future[Option[String]] = (writeKdbActor ? ResetStrategyKdbMessage).mapTo[ResponseResetStrategyKdbMessage] map {resultMsg =>
    	       log.info("Strategy schema Generation")
    	       Some("DOne")
	    }
    	resetSchemaResult.onComplete {
    	  case Success(result) => {
    	       log.info("Strategy schema reset was successfull")
    	       generateResponse(client, host, msg, tableId)
    	  }
    	  case Failure(error) => {
    		  log.info("Strategy schema reset was unsuccessfull; error: {}", error.getMessage())
    	  }
    	}
	  
	} else {
		generateResponse(client, host, msg, tableId)
	}

  }
  
  private def generateResponse(client: ActorRef, host: ActorRef, msg: TttsStrategyMessage, tableId: String) = {
    
	  		val payload = msg match {
	  		  case x if msg.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage].payload 
	  		  case x if msg.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage].payload
	  		  case _ => None
	  		}

	  		val clnt = msg match {
	  		  case x if msg.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage].client  
	  		  case x if msg.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage].client
	  		  case _ => ""
	  		}

	  		val sequenceNum = msg match {
	  		  case x if msg.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage].sequenceNum  
	  		  case x if msg.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage].sequenceNum
	  		  case _ => ""
	  		}

	  		val sid = msg match {
	  		  case x if msg.isInstanceOf[ResponseFeedFacadeTopicMessage] => None  
	  		  case x if msg.isInstanceOf[ResponseFeedServicesTopicMessage] => Some(x.asInstanceOf[ResponseFeedServicesTopicMessage].serviceId)
	  		  case _ => None
	  		}
	  		
//	  		val message = msg match {
//	  		  case x if msg.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage] 
//	  		  case x if msg.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage]
//	  		  case _ => None
//	  		}
	  		
	  		val messageTraits = Utils.generateMessageTraits
	  		
	        payload match {
	          case Some(payload) => {
			    	val strategyResult: Future[String] = process(tableId, payload)
			    	strategyResult.onComplete {
			    	  case Success(result) => {
					       val payloadStr = s"${result}"
					       val payloadRsp = StrategyPayload(payload.datetime, "abx", payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size, payloadStr, 0.00, 0.00, 0.00)
					       
					       val response = sid match {
					         case Some(servId) => ResponseStrategyServicesTopicMessage(messageTraits._1, STRATEGY_RESPONSE_MESSAGE_TYPE, clnt, Some(payloadRsp), messageTraits._2, sequenceNum, result, servId)
					         case None => ResponseStrategyFacadeTopicMessage(messageTraits._1, STRATEGY_RESPONSE_MESSAGE_TYPE, clnt, Some(payloadRsp), messageTraits._2, sequenceNum, result)
					       }
			    	       log.debug("Execute ResponseStrategyFacadeTopicMessage:client: {} ; message: {}", client, response)
					       client ! response
//					       host ! StopAbxStrategyMessage
			    	  }
			    	  case Failure(error) => {
			    	    log.error("AbxStrategy process method error: {}", error.getMessage())
			    	  }
			    	}
	          }
	          case None => 
	        }
    
  }

  /*
   * ################################# Strategy business logic goes here #################################################
   */
  def process(tableId: String, payload: FeedPayload): Future[String] = {
//    val data = getQuotesData()
    
	implicit val timeout = Timeout(2 seconds)
    val readKdbActor = context.actorOf(ReadKdbActor.props(tableId))
    
    (readKdbActor ? ReadKdbMessage).mapTo[ReadKdbResultMessage] map {resultMessage =>
    	// Process previously stored quotes
        val data = resultMessage.result 
		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val maxHigh: Double = data(6).getOrElse(0.00)

    	val inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    	val inputDate = inputSdf.parse(payload.datetime)
    	val outputSdf = new java.text.SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS")
    	val outputDateStr = outputSdf.format(inputDate)
    	
        // Store Feed Quote to Quotes table
    	val writeData = (outputDateStr, payload.ticker, payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size)
        writeQuotesData(tableId, writeData)
		
		// Doo strategy business logic and return result signal
        if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && maxHigh != 0.00) {
		  /*
		   * Close > Prev. high - buy; Close < Prev. Low - sell. ==> No bueno, lost 5k over 200 trades.
		   */
//		  if(l1c > l2h) {
		  
		  /*
		   * Close > Last 10 max(High) - Buy; Close < Prev. Low - Sell
		   * 
		   */
		  if(l1c > maxHigh) {
		    "BUY"
		  } else if(l1c < l2l) {
		    "SELL"
		  } else {
		    "HOLD"
		  }
		} else {
		  "NOT ENOUGH DATA"
		}
	}
  }
  /*
   * ################################# Strategy business logic ends here #################################################
   */
  
  
  
  private def resetStrategySchema(tableId: String): Unit = {

    val writeKdbActor = context.actorOf(WriteKdbActor.props(tableId))
    writeKdbActor ! ResetStrategyKdbMessage
    writeKdbActor ! StopWriteKdbActor
  
  }  
  
  
  private def writeStrategyData(tableId: String, data: StrategyKdbType): Unit = {

    val writeKdbActor = context.actorOf(WriteKdbActor.props(tableId))
    writeKdbActor ! WriteStrategyKdbMessage(data)
    writeKdbActor ! StopWriteKdbActor
  
  }  

  private def writeQuotesData(tableId: String, data: TransactionKdbType): Unit = {

    val writeKdbActor = context.actorOf(WriteKdbActor.props(tableId))
    writeKdbActor ! WriteTransactionKdbMessage(data)
    writeKdbActor ! StopWriteKdbActor
  
  }  

  private def constructTableId(msg: TttsStrategyMessage, serviceId: String): String = {
    msg match {
	    case x: ResponseFeedFacadeTopicMessage => {
		    val clientId = x.client.replaceAll("\\p{Punct}", "")
		    "_" + serviceId + "_" + clientId
	    }    
	    case x: ResponseFeedServicesTopicMessage => {
	    	val clientId = x.client.replaceAll("\\p{Punct}", "")
		    "_" + serviceId + "_" + clientId
	    }
	    case _ =>  "Invalid"
    }	    
  }
  
  
}
  
