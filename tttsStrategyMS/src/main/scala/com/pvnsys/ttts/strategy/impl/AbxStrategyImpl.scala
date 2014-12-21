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
}


class AbxStrategyImpl extends Strategy with LazyLogging {

  import TttsStrategyMessages._
  import AbxStrategyImpl._
  import WriteKdbActor._
  import ReadKdbActor._
  import Strategy._
  import StrategySignal._
  
  	def createSchema(serviceId: String, message: TttsStrategyMessage): TttsStrategyMessage = {
	  	val tableId = constructTableId(message, serviceId)
	  	WriteKdbActor.resetStrategyData(tableId)
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
          }
          case None => 
        }

    	message
  	}  
  

  override def applyStrategy(serviceId: String, message: TttsStrategyMessage): TttsStrategyMessage = {
    
	  	val tableId = constructTableId(message, serviceId)
	  	
	  	//TODO: replace blocking call with GK what
	  	val data = ReadKdbActor.getAbxQuotesWithBBData(tableId)
	  	
		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val minLow: Double = data(6).getOrElse(0.00)
		val maxHigh: Double = data(7).getOrElse(0.00)
		val lowerBB: Double = data(8).getOrElse(0.00)
		val middBB: Double = data(9).getOrElse(0.00)
		val upperBB: Double = data(10).getOrElse(0.00)
    
		// Do strategy business logic and return result signal
		
//		val result = abovePreviousStrategy(data)
//		val result = maxHighStrategy(data)
//		val result = bbUpperToMidShortStrategy(data)
//		val result = bbUpperToLowerShortStrategy(data)
//		val result = aboveMaxNToBelowMinMLongStrategy(data)
//		val result = belowMinNToAboveMaxMShortStrategy(data)
//		val result = closeBelow2AboveMidBBPercentageLong(data)
//		val result = closeAbove2BelowMidBBPercentageShort(data)
  
		val result = fromLowerBBLongStrategy(data)
		
//		val result = fromUpperBBShortStrategy(data)
		
		
		val payload = message match {
		  case x: ResponseFeedFacadeTopicMessage => x.payload 
		  case x: ResponseFeedServicesTopicMessage => x.payload
		  case _ => None
		}
	
        val strategyResponseMessage = payload match {
          case Some(payload) => {
		       val payloadStr = s"${result}"
		       val payloadRsp = StrategyPayload(payload.datetime, "abx", payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size, payloadStr, lowerBB, middBB, upperBB)

		       
		    	val inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		    	val inputDate = inputSdf.parse(payload.datetime)
		    	val outputSdf = new java.text.SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS")
		    	val outputDateStr = outputSdf.format(inputDate)
			  	val writeData = (outputDateStr, payload.ticker, payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size, minLow, maxHigh, lowerBB, middBB, upperBB)
			  	WriteKdbActor.setStrategyData(tableId, writeData)
			  	

				val clnt = message match {
				  case x: ResponseFeedFacadeTopicMessage => x.client  
				  case x: ResponseFeedServicesTopicMessage=> x.client
				  case _ => ""
				}
			
				val sequenceNum = message match {
				  case x: ResponseFeedFacadeTopicMessage => x.sequenceNum  
				  case x: ResponseFeedServicesTopicMessage => x.sequenceNum
				  case _ => ""
				}
			
				val sid = message match {
				  case x: ResponseFeedFacadeTopicMessage => None  
				  case x: ResponseFeedServicesTopicMessage => Some(x.serviceId)
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
  
  
  /*
   * ============== Strategies ===============================
   */

    private def closeAbove2BelowMidBBPercentageShort(data: List[Option[Double]]) = {
      
		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val minLow: Double = data(6).getOrElse(0.00)
		val maxHigh: Double = data(7).getOrElse(0.00)
		val lowerBB: Double = data(8).getOrElse(0.00)
		val middBB: Double = data(9).getOrElse(0.00)
		val upperBB: Double = data(10).getOrElse(0.00)
    
		val percentage = 2.00
		
        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && minLow != 0.00 && maxHigh != 0.00 && lowerBB != 0.00 && middBB != 0.00 && upperBB != 0.00) {
		  if((l2c / middBB - 1) * 100  >= percentage) {
		    Short 
		  } else if((l2c / middBB - 1) * 100  <= (-1) * percentage) {
		    Cover
		  } else {
	    	HoldShort
		  }
		} else {
		  NotAvailabe
		}
        result
  	}

    private def closeBelow2AboveMidBBPercentageLong(data: List[Option[Double]]) = {
      
		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val minLow: Double = data(6).getOrElse(0.00)
		val maxHigh: Double = data(7).getOrElse(0.00)
		val lowerBB: Double = data(8).getOrElse(0.00)
		val middBB: Double = data(9).getOrElse(0.00)
		val upperBB: Double = data(10).getOrElse(0.00)
    
		val percentage = 3.00
		
        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && minLow != 0.00 && maxHigh != 0.00 && lowerBB != 0.00 && middBB != 0.00 && upperBB != 0.00) {
		  if((l2c / middBB - 1) * 100  <= (-1) * percentage) {
		    Buy 
		  } else if((l2c / middBB - 1) * 100  >= percentage) {
		    Sell
		  } else {
	    	HoldLong
		  }
		} else {
		  NotAvailabe
		}
        result
  }

    private def aboveMaxNToBelowMinMLongStrategy(data: List[Option[Double]]) = {
      
		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val minLow: Double = data(6).getOrElse(0.00)
		val maxHigh: Double = data(7).getOrElse(0.00)
		val lowerBB: Double = data(8).getOrElse(0.00)
		val middBB: Double = data(9).getOrElse(0.00)
		val upperBB: Double = data(10).getOrElse(0.00)
    
        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && minLow != 0.00 && maxHigh != 0.00 && lowerBB != 0.00 && middBB != 0.00 && upperBB != 0.00) {
		  if(l2c <= minLow) {
		    Buy 
		  } else if(l2c >= maxHigh) {
		    Sell
		  } else {
	    	HoldLong
		  }
		} else {
		  NotAvailabe
		}
        result
  }
  
    private def belowMinNToAboveMaxMShortStrategy(data: List[Option[Double]]) = {
      
		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val minLow: Double = data(6).getOrElse(0.00)
		val maxHigh: Double = data(7).getOrElse(0.00)
		val lowerBB: Double = data(8).getOrElse(0.00)
		val middBB: Double = data(9).getOrElse(0.00)
		val upperBB: Double = data(10).getOrElse(0.00)
    
        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && minLow != 0.00 && maxHigh != 0.00 && lowerBB != 0.00 && middBB != 0.00 && upperBB != 0.00) {
		  if(l2c <= minLow) {
		    Short
		  } else if(l2c >= maxHigh) {
		    Cover
		  } else {
	    	HoldShort
		  }
		} else {
		  NotAvailabe
		}
        result
  }
  
    private def bbUpperToLowerShortStrategy(data: List[Option[Double]]) = {
      
		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val minLow: Double = data(6).getOrElse(0.00)
		val maxHigh: Double = data(7).getOrElse(0.00)
		val lowerBB: Double = data(8).getOrElse(0.00)
		val middBB: Double = data(9).getOrElse(0.00)
		val upperBB: Double = data(10).getOrElse(0.00)
    
        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && minLow != 0.00 && maxHigh != 0.00 && lowerBB != 0.00 && middBB != 0.00 && upperBB != 0.00) {
		  if(l2c > upperBB) {
		    Short
		  } else if(l2c < lowerBB) {
		    Cover
		  } else {
	    	HoldShort
		  }
		} else {
		  NotAvailabe
		}
        result
  }

  
  private def bbUpperToMidShortStrategy(data: List[Option[Double]]) = {

		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val minLow: Double = data(6).getOrElse(0.00)
		val maxHigh: Double = data(7).getOrElse(0.00)
		val lowerBB: Double = data(8).getOrElse(0.00)
		val middBB: Double = data(9).getOrElse(0.00)
		val upperBB: Double = data(10).getOrElse(0.00)
    
        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && minLow != 0.00 && maxHigh != 0.00 && lowerBB != 0.00 && middBB != 0.00 && upperBB != 0.00) {
		  if(l2c > upperBB) {
		    Short
		  } else if(l2c < middBB) {
		    Cover
		  } else {
		    HoldShort
		  }
		} else {
		  NotAvailabe
		}
        result
  }
  
  private def maxHighStrategy(data: List[Option[Double]]) = {

		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val minLow: Double = data(6).getOrElse(0.00)
		val maxHigh: Double = data(7).getOrElse(0.00)
		val lowerBB: Double = data(8).getOrElse(0.00)
		val middBB: Double = data(9).getOrElse(0.00)
		val upperBB: Double = data(10).getOrElse(0.00)
    
        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && minLow != 0.00 && maxHigh != 0.00 && lowerBB != 0.00 && middBB != 0.00 && upperBB != 0.00) {
          
		  //Close > Last N max(High) - Buy; Close < Prev. Low - Sell
		  if(l1c > maxHigh) {
		    Sell
		  } else if(l1c < l2l) {
		    Buy
		  } else {
		    HoldLong
		  }
		} else {
		  NotAvailabe
		}
        result
  }
  
  private def abovePreviousStrategy(data: List[Option[Double]]) = {
    
		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val minLow: Double = data(6).getOrElse(0.00)
		val maxHigh: Double = data(7).getOrElse(0.00)
		val lowerBB: Double = data(8).getOrElse(0.00)
		val middBB: Double = data(9).getOrElse(0.00)
		val upperBB: Double = data(10).getOrElse(0.00)
    
        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && minLow != 0.00 && maxHigh != 0.00 && lowerBB != 0.00 && middBB != 0.00 && upperBB != 0.00) {
			// Close > Prev. high - buy; Close < Prev. Low - sell. 
			if(l1c > l2h) {
				Buy
			} else if(l1c < l2l) {
				Sell
			} else {
				HoldLong
			}
		} else {
		  NotAvailabe
		}
        result
  }

  
  private def fromLowerBBLongStrategy(data: List[Option[Double]]) = {
    
		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val minLow: Double = data(6).getOrElse(0.00)
		val maxHigh: Double = data(7).getOrElse(0.00)
		val l1LowerBB: Double = data(8).getOrElse(0.00)
		val l1MiddBB: Double = data(9).getOrElse(0.00)
		val l1UpperBB: Double = data(10).getOrElse(0.00)
		val l2LowerBB: Double = data(11).getOrElse(0.00)
		val l2MiddBB: Double = data(12).getOrElse(0.00)
		val l2UpperBB: Double = data(13).getOrElse(0.00)
    
		val percentage = 2.00
		
        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && minLow != 0.00 && maxHigh != 0.00 && l2LowerBB != 0.00 && l2MiddBB != 0.00 && l2UpperBB != 0.00 && l1LowerBB != 0.00 && l1MiddBB != 0.00 && l1UpperBB != 0.00) {
		  if(l2l < l2LowerBB && l2c < l2LowerBB && l1c > l1LowerBB) {
		    Buy 
		  } else if(l1c/l2c >= 1.5) {
		    Sell
		  } else {
	    	HoldLong
		  }
		} else {
		  NotAvailabe
		}
        result
  }

  private def fromUpperBBShortStrategy(data: List[Option[Double]]) = {

//	  	List(l2h, l2l, l2c, l1h, l1l, l1c, minLow, maxHigh, l1LowerBBVal, l1MiddBBVal, l1UpperBBVal, l2LowerBBVal, l2MiddBBVal, l2UpperBBVal)
    
		val l2h: Double = data(0).getOrElse(0.00)
		val l2l: Double = data(1).getOrElse(0.00)
		val l2c: Double = data(2).getOrElse(0.00)
		val l1h: Double = data(3).getOrElse(0.00)
		val l1l: Double = data(4).getOrElse(0.00)
		val l1c: Double = data(5).getOrElse(0.00)
		val minLow: Double = data(6).getOrElse(0.00)
		val maxHigh: Double = data(7).getOrElse(0.00)
		val l1LowerBB: Double = data(8).getOrElse(0.00)
		val l1MiddBB: Double = data(9).getOrElse(0.00)
		val l1UpperBB: Double = data(10).getOrElse(0.00)
		val l2LowerBB: Double = data(11).getOrElse(0.00)
		val l2MiddBB: Double = data(12).getOrElse(0.00)
		val l2UpperBB: Double = data(13).getOrElse(0.00)
    
		val percentage = 2.00
		
        val result = if(l2h != 0.00 && l2l != 0.00 && l2c != 0.00 && l1h != 0.00 && l1l != 0.00 && l1c != 0.00 && minLow != 0.00 && maxHigh != 0.00 && l2LowerBB != 0.00 && l2MiddBB != 0.00 && l2UpperBB != 0.00 && l1LowerBB != 0.00 && l1MiddBB != 0.00 && l1UpperBB != 0.00) {
		  if(l2h > l2UpperBB && l2c > l2UpperBB && l1c < l1UpperBB) {
		    Short 
//		  } else if(l1h > l1UpperBB) { //2014.11.07T15:30:00.000 abx 12.16 0  4480.49 290
		  } else if(l1c/l1c < 0.5) {
		    Cover
		  } else {
	    	HoldShort
		  }
		} else {
		  NotAvailabe
		}
        result
  }
  
  
}
