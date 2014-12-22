package com.pvnsys.ttts.strategy.impl

import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.TttsStrategyMessage
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.FeedPayload
import scala.concurrent.Future

object Strategy {
  
  object StrategySignal extends Enumeration {
    type StrategySignal = Value
//    val Buy, Sell, HoldLong, Short, Cover, HoldShort, SellStop, CoverStop, NotAvailabe = Value
    val Buy, Close, Hold, Short, CloseStop, ProfitStop, NotAvailabe = Value
//    val Buy = Value("BUY")
//    val Sell = Value("SELL")
//    val Hold = Value("HOLD")
//    val Short = Value("SHORT")
//    val Cover = Value("COVER")
//    val SellStop = Value("SELL_STOP")
//    val CoverStop = Value("COVER_STOP")
//    val NotAvailabe = Value("NOT_AVAIL")
  }
  
  implicit def strategySignal2String(i: StrategySignal.Value)= i.toString
  
//  type StrategyKdbType = (Double, Double, Long, Boolean, Long)
  type StrategyKdbType = (String, String, Double, Double, Double, Double, Long, Double, Long, Double, Double, Double, Double, Double)

//  quotes:([]datetime:`timestamp$();sym:`symbol$();open:`float$();high:`float$();low:`float$();close:`float$();volume:`long$();wap:`float$();size:`long$()) 
  type TransactionKdbType = (String, String, Double, Double, Double, Double, Long, Double, Long)
  
}

trait Strategy {
  
  /*
   * Main Strategy implementation. Accepts TttsStrategyMessage message and generates TttsStrategyMessage as outcome.
   * 
   * Parameter - TttsStrategyMessage, message from Feed Microservice with a quote price.
   * 
   * - ResponseFeedFacadeTopicMessage, if request came from the client through Facade Microservice, or ResponseFeedServicesTopicMessage, if request came from another Microservice through Services Topic
   *     ResponseFeedFacadeTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, serviceId: String) extends TttsStrategyMessage
   *   		id, client and timestamp fiends will be generated automatically via com.pvnsys.ttts.strategy.utilUtils.generateMessageTraits
   *   		msgType is always FEED_RESPONSE_MESSAGE_TYPE = "FEED_RSP" (see com.pvnsys.ttts.strategy.messages.TttsStrategyMessages)
   *   		payload - quote price.
   *   		sequenceNum - is an order number of this message (1,2,3,4,...,n)   
   *   
   * Returns message of type:
   * 
   * - ResponseStrategyFacadeTopicMessage, if request came from the client through Facade Microservice
   *     ResponseStrategyFacadeTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, signal: String) extends TttsStrategyMessage
   *   		id, client and timestamp fiends will be generated automatically via com.pvnsys.ttts.strategy.utilUtils.generateMessageTraits
   *   		msgType is always STRATEGY_RESPONSE_MESSAGE_TYPE = "STRATEGY_RSP" (see com.pvnsys.ttts.strategy.messages.TttsStrategyMessages)
   *   		payload - any result to return to the client. For example, e quote price.
   *   		sequenceNum - is an order number of this message (1,2,3,4,...,n)   
   *   		signal - main result of applying strategy algorithm to the stock price. Could be a signal: BUY, SELL or HOLD 
   *   
   *   
   * - ResponseStrategyServicesTopicMessage, if request came from another Microservice through Services Topic
   *     RequestStrategyServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, serviceId: String) extends TttsStrategyMessage
   *   		id, client and timestamp and serviceId fiends will be generated automatically via com.pvnsys.ttts.strategy.utilUtils.generateMessageTraits
   *   		msgType is always STRATEGY_RESPONSE_MESSAGE_TYPE = "STRATEGY_RSP" (see com.pvnsys.ttts.strategy.messages.TttsStrategyMessages)
   *   		payload - any result to return to the client. For example, e quote price.
   *   		sequenceNum - is an order number of this message (1,2,3,4,...,n)   
   *   		signal - main result of applying strategy algorithm to the stock price. Could be a signal: BUY, SELL or HOLD 
   * 
   * 
   */
  def applyStrategy(serviceId: String, message: TttsStrategyMessage): TttsStrategyMessage
  
}

