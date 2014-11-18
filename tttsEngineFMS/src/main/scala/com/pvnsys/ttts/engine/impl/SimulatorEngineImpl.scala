package com.pvnsys.ttts.engine.impl

import akka.actor.ActorContext
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
import com.typesafe.scalalogging.slf4j.LazyLogging

object SimulatorEngineImpl {

  import TttsEngineMessages._
  /*
   * Engine table row Tuple: 
   *   funds - initial balance; 
   *   balance - current balance; 
   *   transnum - number of transactions; 
   *   intrade - is trade in progress; 
   *   possize - trade in progress position size
   *   
   *   engine:([]funds:`float$();balance:`float$();transnum:`long$();intrade:`boolean$();possize:`long$())
   */ 
  type EngineKdbType = (Double, Double, Long, Boolean, Long)
  
  // trade:([]time:`time$();sym:`symbol$();price:`float$();size:`long$();oper:`symbol$();cost:`float$()) 
  type TransactionKdbType = (String, String, Double, Long, String, Double)
  
}


/**
 * Example of some engine.
 * 
 */
class SimulatorEngineImpl(context: ActorContext) extends Engine with LazyLogging {

  import TttsEngineMessages._
  import SimulatorEngineImpl._
  import ReadKdbActor._
  import WriteKdbActor._
  

  	def createSchema(serviceId: String, message: TttsEngineMessage): TttsEngineMessage = {

	  	val tableId = constructTableId(message, serviceId)
	  	WriteKdbActor.resetEngineData(tableId)
        message
  	}	
  
  
    override def applyEngine(serviceId: String, message: TttsEngineMessage): TttsEngineMessage = {
    
	  	val tableId = constructTableId(message, serviceId)
	  	
	  	
	  	//TODO: replace blocking call with GK what
	  	val data = ReadKdbActor.getEngineData(tableId)

		val payload = message match {
		  case x: ResponseStrategyFacadeTopicMessage => x.payload 
		  case x: ResponseStrategyServicesTopicMessage => x.payload
		  case _ => None
		}
	  	
        val engineResponseMessage = payload match {
          case Some(payload) => {
            
				val strategySignal = message match {
				  case x : ResponseStrategyFacadeTopicMessage => x.signal 
				  case x : ResponseStrategyServicesTopicMessage => x.signal
				  case _ => ""
				}

		        // TODO: pass from UI through FacadeMS
		        val sellShort = true
				
			    val enginePayload = strategySignal match {
			      case "BUY" => if(!data._4) {
				        val comission = 10
				        var newPossize = ((data._2 - comission) / payload.close).longValue
				        // $10 balance is the limit. No less allowed.
				        if(newPossize > 0) {
				            val newFunds = data._1 
					        val position =  newPossize * payload.close + comission

					        // Do not let balance drop below $1.00
					        val newBalance =  (data._2 - position) match {
				              case x if(x<1) => 0L
				              case _ => (data._2 - position)	
				            } 
				            
//					        // Do not let balance drop below $1.00
//					        var disc = 1
//					        while(newBalance < 1) {
//					          newPossize = newPossize - disc 
//					          position =  newPossize * payload.close + comission
//					          newBalance =  data._2 - position
//					          disc += 1
//					        }
					        
				            val newTransnum = data._3 + 1
					        val newIntrade = true
					        val price = payload.close
			
					        val newData = (newFunds, newBalance, newTransnum, newIntrade, newPossize, price)
					        WriteKdbActor.setEngineData(tableId, newData)
		
					        /*
					         * trade:([]time:`time$();sym:`symbol$();price:`float$();size:`int$();oper:`symbol$();cost:`float$())
					         */
					        
					    	val inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					    	val inputDate = inputSdf.parse(payload.datetime)
					    	val outputSdf = new java.text.SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS")
					    	val outputDateStr = outputSdf.format(inputDate)

					        val tradeType =  sellShort match {
					          case false => "BUY" // Long position: BUY = open long; SELL = close long
					          case true => "SHORT" // Short position: BUY - open short; SELL = cover
					        }
					    	
					    	val transactionData = (outputDateStr, payload.ticker, payload.close, newPossize, tradeType, -1 * position)
					        WriteKdbActor.setTransactionData(tableId, transactionData)
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
				        
				        val newBalance =  sellShort match {
				          case false => data._2 + ((data._5 * payload.close - comission) - data._2) // Long position: BUY = open long; SELL = close long
				          case true =>  2* (data._5 * data._6 + comission) - (data._5 * payload.close - comission) + data._2  // Short position: BUY - open short; SELL = cover
				        }
				        
//				        val newBalance =  data._2 + data._5 * payload.close - comission 
				        
				        
				        val newPossize = 0l
				        val newIntrade = false
				        val price = payload.close
			
				        val newData = (newFunds, newBalance, newTransnum, newIntrade, newPossize, price)
				        WriteKdbActor.setEngineData(tableId, newData)
			
				    	val inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				    	val inputDate = inputSdf.parse(payload.datetime)
				    	val outputSdf = new java.text.SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS")
				    	val outputDateStr = outputSdf.format(inputDate)
				        
				        val tradeType =  sellShort match {
				          case false => "SELL" // Long position: BUY = open long; SELL = close long
				          case true => "COVER" // Short position: BUY - open short; SELL = cover
				        }
				        val transactionData = (outputDateStr, payload.ticker, payload.close, newPossize, tradeType, sellProceeds)
				        WriteKdbActor.setTransactionData(tableId, transactionData)
				        Some(EnginePayload(payload.datetime, payload.ticker, payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size, "SOLD", newFunds, newBalance, newTransnum, newIntrade, newPossize))
		
			        } else {
			      	  None // "NO POSITION"
			      	}
			      case "HOLD" => None //"PASS"
			      case _ => None //"Nothing"
			    }
            
			    val messageTraits = Utils.generateMessageTraits
			    message match {
			      case x: ResponseStrategyFacadeTopicMessage => ResponseEngineFacadeTopicMessage(messageTraits._1, ENGINE_RESPONSE_MESSAGE_TYPE, x.client, enginePayload, messageTraits._2, x.sequenceNum, x.signal)
			      case x: ResponseStrategyServicesTopicMessage => ResponseEngineServicesTopicMessage(messageTraits._1, ENGINE_RESPONSE_MESSAGE_TYPE, x.client, enginePayload, messageTraits._2, x.sequenceNum, x.signal, x.serviceId)
			      case _ => message
			    }
			    			    
          }		
          case None => message
        }
        
        engineResponseMessage
        
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
