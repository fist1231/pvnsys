package com.pvnsys.ttts.engine.messages

import akka.actor.ActorRef

object TttsEngineMessages {
  
	val STRATEGY_REQUEST_MESSAGE_TYPE = "STRATEGY_REQ"
	val STRATEGY_RESPONSE_MESSAGE_TYPE = "STRATEGY_RSP"
	val STRATEGY_STOP_REQUEST_MESSAGE_TYPE = "STRATEGY_STOP_REQ"

	val ENGINE_REQUEST_MESSAGE_TYPE = "ENGINE_REQ"
	val ENGINE_RESPONSE_MESSAGE_TYPE = "ENGINE_RSP"
	val ENGINE_STOP_REQUEST_MESSAGE_TYPE = "ENGINE_STOP_REQ"
	  
	sealed trait TttsEngineMessage
	case object StartEngineServiceMessage extends TttsEngineMessage
	case object StopEngineServiceMessage extends TttsEngineMessage
	case object StartListeningFacadeTopicMessage extends TttsEngineMessage
	case object StartListeningServicesTopicMessage extends TttsEngineMessage
	case object StartKafkaServicesTopicConsumerMessage extends TttsEngineMessage

	case class FacadePayload(payload: String) extends TttsEngineMessage
	case class StrategyPayload(datetime: String, ticker: String, open: Double, high: Double, low: Double, close: Double, volume: Long, wap: Double, size: Long, payload: String, lowerBB: Double, middBB: Double, upperBB: Double) extends TttsEngineMessage
	case class EnginePayload(datetime: String, ticker: String, open: Double, high: Double, low: Double, close: Double, volume: Long, wap: Double, size: Long, payload: String, funds: Double, balance: Double, tradesNum: Long, inTrade: Boolean, positionSize: Long) extends TttsEngineMessage
	
	case class FacadeTopicMessage(id: String, msgType: String, client: String, payload: Option[FacadePayload], timestamp: String, sequenceNum: String) extends TttsEngineMessage
	case class RequestEngineFacadeTopicMessage(id: String, msgType: String, client: String, payload: Option[EnginePayload], timestamp: String, sequenceNum: String) extends TttsEngineMessage
	case class ResponseEngineFacadeTopicMessage(id: String, msgType: String, client: String, payload: Option[EnginePayload], timestamp: String, sequenceNum: String, signal: String) extends TttsEngineMessage
	case class ResponseStrategyFacadeTopicMessage(id: String, msgType: String, client: String, payload: Option[StrategyPayload], timestamp: String, sequenceNum: String, signal: String, serviceId: String) extends TttsEngineMessage

	case class ServicesTopicMessage(id: String, msgType: String, client: String, payload: Option[EnginePayload], timestamp: String, sequenceNum: String, signal: String, serviceId: String) extends TttsEngineMessage
	case class RequestStrategyServicesTopicMessage(id: String, msgType: String, client: String, payload: Option[StrategyPayload], timestamp: String, sequenceNum: String, serviceId: String) extends TttsEngineMessage
	case class ResponseStrategyServicesTopicMessage(id: String, msgType: String, client: String, payload: Option[StrategyPayload], timestamp: String, sequenceNum: String, signal: String, serviceId: String) extends TttsEngineMessage

	case class RequestEngineServicesTopicMessage(id: String, msgType: String, client: String, payload: Option[EnginePayload], timestamp: String, sequenceNum: String, serviceId: String) extends TttsEngineMessage
	case class ResponseEngineServicesTopicMessage(id: String, msgType: String, client: String, payload: Option[EnginePayload], timestamp: String, sequenceNum: String, signal: String, serviceId: String) extends TttsEngineMessage
	
}


