package com.pvnsys.ttts.engine.impl

import akka.actor.{Actor, ActorLogging, Props, PoisonPill, ActorRef}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import com.pvnsys.ttts.engine.util.Utils
import java.sql._
import kx.c
import kx.c._
import kx.c.Flip
import scala.Array
import com.pvnsys.ttts.engine.db.ReadKdbActor
import com.pvnsys.ttts.engine.db.WriteKdbActor
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object SimulatorEngineActor {
  import TttsEngineMessages._
  sealed trait SimulatorEngineMessages
  case class StartSimulatorEngineMessage(message: TttsEngineMessage, serviceId: String) extends SimulatorEngineMessages
  case object StopSimulatorEngineMessage extends SimulatorEngineMessages
  case class SimulatorEngineResponseMessage(message: TttsEngineMessage) extends SimulatorEngineMessages
}


/**
 * Example of some engine.
 * 
 */
class SimulatorEngineActor extends Actor with ActorLogging {

  import TttsEngineMessages._
  import SimulatorEngineActor._
  import ReadKdbActor._
  import WriteKdbActor._
  import Engine._
  
  
  /*
   * ################################# Engine business logic goes here #################################################
   */
      
  def process(tableId: String, payload: StrategyPayload, strategySignal: String): Future[Option[EnginePayload]] = {
    
	implicit val timeout = Timeout(2 seconds)
	
    val readKdbActor = context.actorOf(ReadKdbActor.props(tableId))
    
    (readKdbActor ? ReadKdbMessage).mapTo[ReadKdbResultMessage] map {resultMessage =>
      	
	//    val data = (0f, 0f, 0l, true, 0l)
	    
        val data = resultMessage.result 
      
	    strategySignal match {
	      case "BUY" => if(!data._4) {
		        val comission = 10
		        val newPossize = ((data._2 - comission) / payload.close).longValue
		        if(newPossize > 0) {
		            val newFunds = data._1 
			        val position =  newPossize * payload.close + comission
			        val newTransnum = data._3 + 1
			        val newBalance =  data._2 - position 
			        val newIntrade = true
			        val newIsLong = true
			        
			        val pl = 0.0
			        val plac = 0.0
	
			        val newData = (newFunds, newBalance, newTransnum, newIntrade, newPossize, payload.close, newIsLong)
			        writeEngineData(tableId, newData)

			        /*
			         * trade:([]time:`time$();sym:`symbol$();price:`float$();size:`int$();oper:`symbol$();cost:`float$())
			         */
			        
			    	val inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
			    	val inputDate = inputSdf.parse(payload.datetime)
			    	val outputSdf = new java.text.SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS")
			    	val outputDateStr = outputSdf.format(inputDate)

			    	val transactionData = (outputDateStr, payload.ticker, payload.close, newPossize, "BUY", -1 * position, newBalance, pl, plac, newTransnum)
			        writeTransactionData(tableId, transactionData)
			        Some(EnginePayload(payload.datetime, payload.ticker, payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size, "BOUGHT", newFunds, newBalance, newTransnum, newIntrade, newPossize))
			        
		        } else {
		        	None //"margin call"
		        }
	      	} else {
	      	  None // "HOLD"
	      	}
	      case "SELL" => if(data._4) {
		        val comission = 10
	            val newFunds = data._1 
		        
		        val newTransnum = data._3 + 1
		        val sellProceeds = data._5 * payload.close - comission
		        val newBalance =  data._5 * payload.close - comission + data._2 
		        val newPossize = 0l
		        val newIntrade = false
		        val newIsLong = false
	
		        val newData = (newFunds, newBalance, newTransnum, newIntrade, newPossize, payload.close, newIsLong)
		        writeEngineData(tableId, newData)
	
		    	val inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		    	val inputDate = inputSdf.parse(payload.datetime)
		    	val outputSdf = new java.text.SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS")
		    	val outputDateStr = outputSdf.format(inputDate)
		    	
		        val pl = 0.0
		        val plac = 0.0
		    	
		        val transactionData = (outputDateStr, payload.ticker, payload.close, newPossize, "SELL", sellProceeds, newBalance, pl, plac, newTransnum)
		        writeTransactionData(tableId, transactionData)
		        Some(EnginePayload(payload.datetime, payload.ticker, payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size, "SOLD", newFunds, newBalance, newTransnum, newIntrade, newPossize))

	        } else {
	      	  None // "NO POSITION"
	      	}
	      case "HOLD" => None //"PASS"
	      case _ => None //"Nothing"
	    }
	}
    
  }

  /*
   * #################################    Engine business logic ends   #################################################
   */
  
  
  override def receive = {
    case m: StartSimulatorEngineMessage => {
      log.debug("SimulatorEngineActor received StartSimulatorEngineMessage: {}", m)
	  val client = sender()
	  val host = self
      val result = execute(client, host, m.message, m.serviceId)
//      client ! result
      
    }
    case StopSimulatorEngineMessage => {
      log.debug("SimulatorEngineActor received StopSimulatorEngineMessage")
      context stop self
    }
  }  
  
  
//  override def process(msg: TttsEngineMessage, serviceId: String): TttsEngineMessage = {
//    msg
//  }
  

/*
 * Do Engine processing, create ResponseEngineFacadeTopicMessage (reply to FacadeMS)
 * 
 */ 
  def execute(client: ActorRef, host: ActorRef, msg: TttsEngineMessage, serviceId: String) = {
    
//    var response: TttsEngineMessage = msg 
    // Generate unique message ID, timestamp and sequence number to be assigned to every message.
    val messageTraits = Utils.generateMessageTraits
    
    val tableId = constructTableId(msg, serviceId)

    /*
     * - Assign unique message id and timestamp generated by Utils.generateMessageTraits.
     * - Pass along client, payload and sequenceNum from Engine message.
     * - Add signal field
     * - Return ResponseEngineFacadeTopicMessage or ResponseEngineServiceTopicMessage, depending on incoming message type.
     */ 
    msg match {
	    case x: ResponseStrategyFacadeTopicMessage => {
	      
			// First message in request - reset engine schema
			if(x.sequenceNum.toInt == 1) {
			  resetEngineSchema(tableId)
			}
			
	        x.payload match {
		          case Some(payload) => {
			    	val engineResult: Future[Option[EnginePayload]] = process(tableId, payload, x.signal)
			    	engineResult.onComplete {
			    	  case Success(result) => {
					       val response = ResponseEngineFacadeTopicMessage(messageTraits._1, ENGINE_RESPONSE_MESSAGE_TYPE, x.client, result, messageTraits._2, x.sequenceNum, x.signal)
			    	       log.debug("Execute ResponseStrategyFacadeTopicMessage:client: {} ; message: {}", client, response)
					       client ! response
					       host ! StopSimulatorEngineMessage
			    	  }
			    	  case Failure(error) => {
			    	    log.error("SimulatorEngine process method error: {}", error.getMessage())
			    	  }
			    	}
	          }
	          case None => 
	        }
	    }
	    case x: ResponseStrategyServicesTopicMessage => {
	      
			// First message in request - reset engine schema
			if(x.sequenceNum.toInt == 1) {
			  resetEngineSchema(tableId)
			}

	        x.payload match {
		          case Some(payload) => {
			    	val engineResult: Future[Option[EnginePayload]] = process(tableId, payload, x.signal)
			    	engineResult.onComplete {
			    	  case Success(result) => {
					       val response = ResponseEngineServicesTopicMessage(messageTraits._1, ENGINE_RESPONSE_MESSAGE_TYPE, x.client, result, messageTraits._2, x.sequenceNum, x.signal, x.serviceId)	 
			    	       log.debug("Execute ResponseStrategyServicesTopicMessage: {} ; message: {}", client, response)
					       client ! response
					       host ! StopSimulatorEngineMessage
			    	  }
			    	  case Failure(error) => {
			    	    log.error("SimulatorEngine process method error: {}", error.getMessage())
			    	  }
			    	}
	          }
	          case None => 
	        }
	    }
	    case _ =>  {
	      log.error("SimulatorEngine Received unsupported message type")
	    }
    }
//    response
  }
  
  private def resetEngineSchema(tableId: String): Unit = {

    val writeKdbActor = context.actorOf(WriteKdbActor.props(tableId))
    writeKdbActor ! ResetEngineKdbMessage
    writeKdbActor ! StopWriteKdbActor
  
  }  
  
  
  private def writeEngineData(tableId: String, data: EngineKdbType): Unit = {

    val writeKdbActor = context.actorOf(WriteKdbActor.props(tableId))
    writeKdbActor ! WriteEngineKdbMessage(data)
    writeKdbActor ! StopWriteKdbActor
  
  }  

  private def writeTransactionData(tableId: String, data: TransactionKdbType): Unit = {

    val writeKdbActor = context.actorOf(WriteKdbActor.props(tableId))
    writeKdbActor ! WriteTransactionKdbMessage(data)
    writeKdbActor ! StopWriteKdbActor
  
  }  
  
  private def constructTableId(msg: TttsEngineMessage, serviceId: String): String = {
    msg match {
	    case x: ResponseStrategyFacadeTopicMessage => {
		    val clientId = x.client.replaceAll("\\p{Punct}", "")
		    "_" + serviceId + "_" + clientId
	    }    
	    case x: ResponseStrategyServicesTopicMessage => {
		    val clientId = x.client.replaceAll("\\p{Punct}", "")
		    "_" + serviceId + "_" + clientId
	    }
	    case _ =>  "Invalid"
    }	    
  }
  
  
}
  
