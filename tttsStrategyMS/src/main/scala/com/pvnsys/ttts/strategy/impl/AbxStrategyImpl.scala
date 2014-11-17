package com.pvnsys.ttts.strategy.impl

import akka.actor.{ActorContext, Actor, ActorLogging, Props, PoisonPill, ActorRef}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.TttsStrategyMessage
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
import com.typesafe.scalalogging.slf4j.LazyLogging



object AbxStrategyImpl {

  import TttsStrategyMessages._

  type StrategyKdbType = (Double, Double, Long, Boolean, Long)

//  type QuotesKdbType = (Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double])
  
  // quotes:([]datetime:`timestamp$();sym:`symbol$();open:`float$();high:`float$();low:`float$();close:`float$();volume:`long$();wap:`float$();size:`long$()) 
  type TransactionKdbType = (String, String, Double, Double, Double, Double, Long, Double, Long)
  
  sealed trait AbxStrategyMessages
  case class StartAbxStrategyMessage(message: TttsStrategyMessage, serviceId: String) extends AbxStrategyMessages
  case object StopAbxStrategyMessage extends AbxStrategyMessages
  case class AbxStrategyResponseMessage(message: TttsStrategyMessage) extends AbxStrategyMessages

  
}


class AbxStrategyImpl(context: ActorContext) extends Strategy with LazyLogging {

  import TttsStrategyMessages._
  import AbxStrategyImpl._
  import WriteKdbActor._
  import ReadKdbActor._
  
  
  	def createSchema(serviceId: String, message: TttsStrategyMessage): TttsStrategyMessage = {

	  	implicit val timeout = Timeout(2 seconds)
    
	  	val tableId = constructTableId(message, serviceId)
	  	
	  	WriteKdbActor.resetStrategyData(tableId)
	  	
//	    val writeKdbActor = context.actorOf(WriteKdbActor.props(tableId))
//	    val resetSchemaResult: Future[Option[String]] = (writeKdbActor ? ResetStrategyKdbMessage).mapTo[ResponseResetStrategyKdbMessage] map {resultMsg =>
//    	       logger.info("Received response from Strategy schema Generation")
//    	       Some("SUCCESS")
//	    }
//    	resetSchemaResult.onComplete {
//    	  case Success(result) => {
//    	       logger.info("Strategy schema reset was {}", result)
//    	       writeKdbActor ! StopWriteKdbActor
//    	  }
//    	  case Failure(error) => {
//    		  logger.error("Strategy schema reset was unsuccessfull; error: {}", error.getMessage())
//    	  }
//    	}
        message
  	}	

  
  	def writeQuotesData(serviceId: String, message: TttsStrategyMessage): TttsStrategyMessage = {

	  	implicit val timeout = Timeout(2 seconds)
    
	  	val tableId = constructTableId(message, serviceId)

  		val payload = message match {
  		  case x if message.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage].payload 
  		  case x if message.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage].payload
  		  case _ => None
  		}
	  	
        payload match {
          case Some(payload) => {
            
		    	val inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		    	val inputDate = inputSdf.parse(payload.datetime)
		    	val outputSdf = new java.text.SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS")
		    	val outputDateStr = outputSdf.format(inputDate)
            
			  	val writeData = (outputDateStr, payload.ticker, payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size)
			  	
			  	WriteKdbActor.setTransactionData(tableId, writeData)
			  	
//			    val writeKdbActor = context.actorOf(WriteKdbActor.props(tableId))
//			    val writeSchemaResult: Future[Option[String]] = (writeKdbActor ? WriteTransactionKdbMessage(writeData)).mapTo[ResponseWriteTransactionKdbMessage] map {resultMsg =>
//		    	       logger.info("Received response from WriteTransaction operation")
//		    	       Some("SUCCESS")
//			    }
//		    	writeSchemaResult.onComplete {
//		    	  case Success(result) => {
//		    	       logger.info("WriteTransaction was {}", result)
//		    	       writeKdbActor ! StopWriteKdbActor
//		    	  }
//		    	  case Failure(error) => {
//		    		  logger.error("WriteTransaction was unsuccessfull; error: {}", error.getMessage())
//		    	  }
//		    	}
          }
          case None => 
        }

    	message
  
  	}  
  

  override def applyStrategy(serviceId: String, message: TttsStrategyMessage): TttsStrategyMessage = {
    
	implicit val timeout = Timeout(2 seconds)
  	val tableId = constructTableId(message, serviceId)
  	
  	
  	//TODO: replace blocking call with GK what
  	val data = ReadKdbActor.getQuotesData(tableId)
  	
		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val maxHigh: Double = data(6).getOrElse(0.00)

		// Doo strategy business logic and return result signal
        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && maxHigh != 0.00) {
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
        
		val payload = message match {
		  case x if message.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage].payload 
		  case x if message.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage].payload
		  case _ => None
		}
	
        val strategyResponseMessage = payload match {
          case Some(payload) => {
		       val payloadStr = s"${result}"
		       val payloadRsp = StrategyPayload(payload.datetime, "abx", payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size, payloadStr)


				val clnt = message match {
				  case x if message.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage].client  
				  case x if message.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage].client
				  case _ => ""
				}
			
				val sequenceNum = message match {
				  case x if message.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage].sequenceNum  
				  case x if message.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage].sequenceNum
				  case _ => ""
				}
			
				val sid = message match {
				  case x if message.isInstanceOf[ResponseFeedFacadeTopicMessage] => None  
				  case x if message.isInstanceOf[ResponseFeedServicesTopicMessage] => Some(x.asInstanceOf[ResponseFeedServicesTopicMessage].serviceId)
				  case _ => None
				}
		       
		       val messageTraits = Utils.generateMessageTraits
		       val response: TttsStrategyMessage = sid match {
		         case Some(servId) => ResponseStrategyServicesTopicMessage(messageTraits._1, STRATEGY_RESPONSE_MESSAGE_TYPE, clnt, Some(payloadRsp), messageTraits._2, sequenceNum, result, servId)
		         case None => ResponseStrategyFacadeTopicMessage(messageTraits._1, STRATEGY_RESPONSE_MESSAGE_TYPE, clnt, Some(payloadRsp), messageTraits._2, sequenceNum, result)
		       }
		       response
          }	    
          case None => message
        }
        
  	strategyResponseMessage
  	
  	
//    val readKdbActor = context.actorOf(ReadKdbActor.props(tableId))
//    val strategyResult: Future[Option[TttsStrategyMessage]] = (readKdbActor ? ReadKdbMessage).mapTo[ReadKdbResultMessage] map {resultMessage =>
//    	// Process previously stored quotes
//        val data = resultMessage.result 
//		val l2h: Double = data(0).getOrElse(0.00)
//		val l2l: Double = data(1).getOrElse(0.00)
//		val l2c: Double = data(2).getOrElse(0.00)
//		val l1h: Double = data(3).getOrElse(0.00)
//		val l1l: Double = data(4).getOrElse(0.00)
//		val l1c: Double = data(5).getOrElse(0.00)
//		val maxHigh: Double = data(6).getOrElse(0.00)
//
//		// Doo strategy business logic and return result signal
//        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && maxHigh != 0.00) {
//		  /*
//		   * Close > Prev. high - buy; Close < Prev. Low - sell. ==> No bueno, lost 5k over 200 trades.
//		   */
////		  if(l1c > l2h) {
//		  
//		  /*
//		   * Close > Last 10 max(High) - Buy; Close < Prev. Low - Sell
//		   * 
//		   */
//		  if(l1c > maxHigh) {
//		    "BUY"
//		  } else if(l1c < l2l) {
//		    "SELL"
//		  } else {
//		    "HOLD"
//		  }
//		} else {
//		  "NOT ENOUGH DATA"
//		}
//        
//		val payload = message match {
//		  case x if message.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage].payload 
//		  case x if message.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage].payload
//		  case _ => None
//		}
//	
//        val strategyResponseMessage = payload match {
//          case Some(payload) => {
//		       val payloadStr = s"${result}"
//		       val payloadRsp = StrategyPayload(payload.datetime, "abx", payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size, payloadStr)
//
//
//				val clnt = message match {
//				  case x if message.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage].client  
//				  case x if message.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage].client
//				  case _ => ""
//				}
//			
//				val sequenceNum = message match {
//				  case x if message.isInstanceOf[ResponseFeedFacadeTopicMessage] => x.asInstanceOf[ResponseFeedFacadeTopicMessage].sequenceNum  
//				  case x if message.isInstanceOf[ResponseFeedServicesTopicMessage] => x.asInstanceOf[ResponseFeedServicesTopicMessage].sequenceNum
//				  case _ => ""
//				}
//			
//				val sid = message match {
//				  case x if message.isInstanceOf[ResponseFeedFacadeTopicMessage] => None  
//				  case x if message.isInstanceOf[ResponseFeedServicesTopicMessage] => Some(x.asInstanceOf[ResponseFeedServicesTopicMessage].serviceId)
//				  case _ => None
//				}
//		       
//		       val messageTraits = Utils.generateMessageTraits
//		       val response: TttsStrategyMessage = sid match {
//		         case Some(servId) => ResponseStrategyServicesTopicMessage(messageTraits._1, STRATEGY_RESPONSE_MESSAGE_TYPE, clnt, Some(payloadRsp), messageTraits._2, sequenceNum, result, servId)
//		         case None => ResponseStrategyFacadeTopicMessage(messageTraits._1, STRATEGY_RESPONSE_MESSAGE_TYPE, clnt, Some(payloadRsp), messageTraits._2, sequenceNum, result)
//		       }
//		       Some(response)
//          }	    
//          case None => None
//        }
//        strategyResponseMessage
//	}
//
//	strategyResult.onComplete {
//	  case Success(result) => {
//	    result match {
//	      case Some(result) => result
//	      case None =>
//	    }
//	  }
//	  case Failure(error) => {
//	    logger.error("AbxStrategy process method error: {}", error.getMessage())
//	  }
//	}
    
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
