package com.pvnsys.ttts.engine.messages

import akka.actor.ActorRef

object TttsEngineMessages {
  
//	val FEED_REQUEST_MESSAGE_TYPE = "FEED_REQ"
//	val FEED_RESPONSE_MESSAGE_TYPE = "FEED_RSP"
//	val FEED_STOP_REQUEST_MESSAGE_TYPE = "FEED_STOP_REQ"

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

	case class FacadeTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String) extends TttsEngineMessage
	case class RequestEngineFacadeTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String) extends TttsEngineMessage
	case class ResponseEngineFacadeTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, signal: String) extends TttsEngineMessage
//	case class RequestFeedFacadeTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, serviceId: String) extends TttsEngineMessage
//	case class ResponseFeedFacadeTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, serviceId: String) extends TttsEngineMessage

	case class ServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, signal: String, serviceId: String) extends TttsEngineMessage
//	case class RequestFeedServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, serviceId: String) extends TttsEngineMessage
//	case class ResponseFeedServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, serviceId: String) extends TttsEngineMessage
	case class RequestStrategyServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, serviceId: String) extends TttsEngineMessage
	case class ResponseStrategyServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, signal: String, serviceId: String) extends TttsEngineMessage

	case class RequestEngineServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, serviceId: String) extends TttsEngineMessage
	case class ResponseEngineServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, signal: String, serviceId: String) extends TttsEngineMessage
	
}


