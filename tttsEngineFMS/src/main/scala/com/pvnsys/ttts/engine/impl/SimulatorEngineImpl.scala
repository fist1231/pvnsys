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
}

/**
 * Example of some engine.
 * 
 */
class SimulatorEngineImpl extends Engine with LazyLogging {

	import TttsEngineMessages._
	import ReadKdbActor._
	import WriteKdbActor._
	import Engine._
	import StrategySignal._
  
    // =============== Parameters ======================
	// TODO: pass from UI through FacadeMS
//    val sellShort = true // Is this a short sell
	val comission = 9.99 // Comission in $
	val minimumBalanceAllowed = 1.00 // Minimum allowed balance amount
//  	val stopLossLongPercentage = 2.00 
//  	val stopLossShortPercentage = 2.00  
//  	val profitTakingLongPercentage = 13.00
//  	val profitTakingShortPercentage = 13.00
  	val stopLossLongPercentage = 0.50
  	val stopLossShortPercentage = 0.10
  	val profitTakingLongPercentage = 15.00
  	val profitTakingShortPercentage = 5.00
    // =================================================

  	def createSchema(serviceId: String, message: TttsEngineMessage): TttsEngineMessage = {

	  	val tableId = constructTableId(message, serviceId)
	  	WriteKdbActor.resetEngineData(tableId)
        message
  	}	
  
	
	/*
	 * ####################  Engine business logic ##################################################
	 */
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
				  case _ => message.asInstanceOf[ResponseStrategyFacadeTopicMessage].signal 
				}

				val sSig = StrategySignal.withName(strategySignal)
				
			    val enginePayload = sSig match {
			      case Buy | Short =>  openPosition(tableId, payload, data, sSig)
//			      case Short =>  openPosition(tableId, payload, data, Short)
//			      case Sell | Cover => closePosition(tableId, payload, data, sSig) 
			      case Close => closePosition(tableId, payload, data, sSig, false, false) 
//			      case Cover => closePosition(tableId, payload, data, Cover, false) 
//			      case HoldLong | HoldShort => {
			      case Hold => {
			    	  // Check if stop loss triggered. If a current price (low) falls below some $payload.close of the purchase price ($data._6)
			    	  if(isStopLossTriggered(data, payload.close, sSig)) {
			    	    val isStopped = true
			    	    val isProfitStopped = false
			    	    closePosition(tableId, payload, data, sSig, isStopped, isProfitStopped)
			    	  } else if(isProfitTakingTriggered(data, payload, sSig)) {
			    	    val isStopped = false
			    	    val isProfitStopped = true
			    	    closePosition(tableId, payload, data, sSig, isStopped, isProfitStopped)
			    	  } else {
			    		  holdPosition //"PASS"
			    	  }
			      }
			      case _ => holdPosition //"Nothing"
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
	/*
	 * ####################  End Engine business logic ##################################################
	 */
    

  private def openPosition(tableId: String, payload: StrategyPayload, data: EngineKdbType, tradeType: Value): Option[EnginePayload] = {
		  if(!data._4) {
		        var newPossize = ((data._2 - comission) / payload.close).longValue
		        if(newPossize > 0) {
		            val newFunds = data._1 
			        val position =  newPossize * payload.close + comission
			        // Do not let balance drop below $minimumBalanceAllowed
			        val newBalance =  (data._2 - position) match {
		              case x if(x < minimumBalanceAllowed) => 0L
		              case _ => (data._2 - position)	
		            } 
		            val newTransnum = data._3 + 1
			        val newIntrade = true
			        val price = payload.close
			        val newIslong = tradeType match {
		              case Buy => true
		              case Short => false
		            }
			        val newData = (newFunds, newBalance, newTransnum, newIntrade, newPossize, price, newIslong)
			        WriteKdbActor.setEngineData(tableId, newData)
			        /*
			         * trade:([]time:`time$();sym:`symbol$();price:`float$();size:`int$();oper:`symbol$();cost:`float$())
			         */
			    	val inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
			    	val inputDate = inputSdf.parse(payload.datetime)
			    	val outputSdf = new java.text.SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS")
			    	val outputDateStr = outputSdf.format(inputDate)

			    	val pl = 0.0
			    	val plac = 0.0
			    	
//			        val tradeType =  positionType match {
//			          case false => "BUY" // Long position: BUY = open long; SELL = close long
//			          case true => "SHORT" // Short position: BUY - open short; SELL = cover
//			        }
			    	val transactionData = (outputDateStr, payload.ticker, payload.close, newPossize, tradeType.toString, -1 * position, newBalance, pl, plac, newTransnum)
			        WriteKdbActor.setTransactionData(tableId, transactionData)
			        Some(EnginePayload(payload.datetime, payload.ticker, payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size, tradeType, newFunds, newBalance, newTransnum, newIntrade, newPossize))
		        } else {
		        	holdPosition //"margin call"
		        }
	  	} else {
	  	  holdPosition // "HOLD"
	  	}
  }    
    
  private def closePosition(tableId: String, payload: StrategyPayload, data: EngineKdbType, tradeType: Value, isStopped: Boolean, isProfitStopped: Boolean): Option[EnginePayload] = {
	  	if(data._4) {
	  	    val isLong = data._7 
	  	   
            val newFunds = data._1 
	        val newTransnum = data._3 + 1
	        val sellProceeds = data._5 * payload.close - comission
	        val result =  (tradeType, isLong) match {
//	          case Sell | HoldLong => data._2 + ((data._5 * payload.close - comission) - data._2) // Long position: BUY = open long; SELL = close long
	          case (Close | Hold, true) => { 
	            val newBalance = data._2 + ((data._5 * payload.close - comission) - data._2) // Long position: BUY = open long; SELL = close long
	            val pl = ((data._5 * payload.close) / (data._5 * data._6) -1 ) * 100
	            val plac = ((data._5 * payload.close - comission) / (data._5 * data._6 + comission) - 1) * 100
	            List(newBalance, pl, plac)
	          }
//	          case Cover | HoldShort =>  {
	          case (Close | Hold, false) =>  {
	            val position = data._5 * data._6 + comission
	            val proceeds = data._5 * payload.close - comission
	            val profit = (-1) * (proceeds - position) - (4 * comission)
	            val newBal = position + profit + data._2
	            
	            val pl = (1 - (data._5 * payload.close) / (data._5 * data._6)) * 100
	            val plac = (1 - (data._5 * payload.close + comission) / (data._5 * data._6 - comission)) * 100
	            List(newBal, pl, plac)
	          }
	          case (_, _) => List(0.0, 0.0, 0.0)
	        }
	        val newPossize = 0l
	        val newIntrade = false
	        val newIslong = false
	        val price = payload.close
	        val newData = (newFunds, result(0), newTransnum, newIntrade, newPossize, price, newIslong)
	        WriteKdbActor.setEngineData(tableId, newData)
	    	val inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
	    	val inputDate = inputSdf.parse(payload.datetime)
	    	val outputSdf = new java.text.SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS")
	    	val outputDateStr = outputSdf.format(inputDate)
	    	val pl = 0.0
	    	val plac = 0.0
//	        val tradeTypeOutcome =  tradeType match {
//	          case Sell => Sell
//	          case HoldLong => SellStop
//	          case Cover => Cover
//	          case HoldShort => CoverStop
//	        }
	        val tradeTypeOutcome =  (tradeType, isLong, isStopped, isProfitStopped) match {
	          case (Close, true, false, false) => Close
	          case (Hold, true, true, false) => CloseStop
	          case (Hold, true, false, true) => ProfitStop
	          case (Close, false, false, false) => Close
	          case (Hold, false, true, false) => CloseStop
	          case (Hold, false, false, true) => ProfitStop
	          case (_, _, _, _) => NotAvailabe
	        }
	        
	        val tt = tradeTypeOutcome.toString
	        val transactionData = (outputDateStr, payload.ticker, payload.close, newPossize, tt, sellProceeds, result(0), result(1), result(2), newTransnum)
	        WriteKdbActor.setTransactionData(tableId, transactionData)
	        Some(EnginePayload(payload.datetime, payload.ticker, payload.open, payload.high, payload.low, payload.close, payload.volume, payload.wap, payload.size, tradeTypeOutcome, newFunds, result(0), newTransnum, newIntrade, newPossize))
        } else {
      	  holdPosition // "NO POSITION"
      	}
  }
  
  private val holdPosition = None

  private def isStopLossTriggered(data: EngineKdbType, currentPrice: Double, tradeType: Value) = {
      val tradePrice = data._6
      val isLong = data._7
      val result = (tradeType, isLong) match {
        case (Hold, true) => (100 - currentPrice/tradePrice * 100) >= stopLossLongPercentage
        case (Hold, false) => (currentPrice/tradePrice * 100 - 100) >= stopLossShortPercentage
        case (_, _) => false
      }
	  result
  }

  private def isProfitTakingTriggered(data: EngineKdbType, payload: StrategyPayload, tradeType: Value) = {
      val tradePrice = data._6
      val isLong = data._7
      val vwap = payload.wap 
	  val currentPrice = payload.close 
	  val middBB = payload.middBB 
	  val upperBB = payload.upperBB 
	  val lowerBB = payload.lowerBB 
      val result = (tradeType, isLong) match {
//        case HoldLong => (((currentPrice/tradePrice * 100 - 100) >= profitTakingPercentage) && (currentPrice < middBB))
//        case HoldLong => ((currentPrice/tradePrice * 100 - 100) >= profitTakingPercentage)
//        case (Hold, true) => (((currentPrice/tradePrice * 100 - 100) >= profitTakingPercentage) && (currentPrice > upperBB))
//        case (Hold, true) => ((currentPrice/tradePrice * 100 - 100) >= profitTakingLongPercentage)
        
        case (Hold, true) => (((currentPrice/tradePrice * 100 - 100) >= profitTakingLongPercentage) && (currentPrice < vwap)) // short
        case (Hold, false) => (((100 - currentPrice/tradePrice * 100) >= profitTakingShortPercentage) && (currentPrice > middBB)) //short
//        case (Hold, true) => (((currentPrice/tradePrice * 100 - 100) >= profitTakingLongPercentage) && (currentPrice < vwap)) // short
//        case (Hold, false) => (((100 - currentPrice/tradePrice * 100) >= profitTakingShortPercentage) && (currentPrice > vwap)) //short
//        case (Hold, true) => ((currentPrice/tradePrice * 100 - 100) >= profitTakingLongPercentage) // short
//        case (Hold, false) => ((100 - currentPrice/tradePrice * 100) >= profitTakingShortPercentage) //short
        
//        case (Hold, true) => (((currentPrice/tradePrice * 100 - 100) >= profitTakingLongPercentage) && (currentPrice < middBB))
//        case HoldShort => (((100 - currentPrice/tradePrice * 100) >= profitTakingPercentage) && (currentPrice > middBB))
//        case HoldLong => (((currentPrice/tradePrice * 100 - 100) >= profitTakingPercentage) && (currentPrice > upperBB))
//        case (Hold, false) => (((100 - currentPrice/tradePrice * 100) >= profitTakingPercentage) && (currentPrice < lowerBB))
//        case (Hold, false) => ((100 - currentPrice/tradePrice * 100) >= profitTakingShortPercentage)
//        case (Hold, false) => (((100 - currentPrice/tradePrice * 100) >= profitTakingShortPercentage) && (currentPrice > vwap))
        case (_, _) => false
      }
	  result
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
