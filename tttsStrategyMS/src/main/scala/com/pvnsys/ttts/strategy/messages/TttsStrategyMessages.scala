package com.pvnsys.ttts.strategy.messages

object TttsStrategyMessages {
  
	val FEED_REQUEST_MESSAGE_TYPE = "FEED_REQ"
	val FEED_RESPONSE_MESSAGE_TYPE = "FEED_RSP"
	val FEED_STOP_REQUEST_MESSAGE_TYPE = "FEED_STOP_REQ"

	val STRATEGY_REQUEST_MESSAGE_TYPE = "STRATEGY_REQ"
	val STRATEGY_RESPONSE_MESSAGE_TYPE = "STRATEGY_RSP"
	val STRATEGY_STOP_REQUEST_MESSAGE_TYPE = "STRATEGY_STOP_REQ"
	  
	sealed trait TttsStrategyMessage
	case object StartStrategyServiceMessage extends TttsStrategyMessage
	case object StopStrategyServiceMessage extends TttsStrategyMessage

	case object StartListeningFacadeTopicMessage extends TttsStrategyMessage
	case object StartListeningServicesTopicMessage extends TttsStrategyMessage
	
	case object StartKafkaServicesTopicConsumerMessage extends TttsStrategyMessage

	case class FacadeTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String) extends TttsStrategyMessage
	case class RequestStrategyFacadeTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String) extends TttsStrategyMessage
	case class ResponseStrategyFacadeTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, signal: String) extends TttsStrategyMessage

	case class ServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, signal: String) extends TttsStrategyMessage
	case class RequestFeedServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String) extends TttsStrategyMessage
	case class ResponseFeedServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String) extends TttsStrategyMessage
	case class RequestStrategyServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String) extends TttsStrategyMessage
	case class ResponseStrategyServicesTopicMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String, signal: String) extends TttsStrategyMessage
	
}


