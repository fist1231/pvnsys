package com.pvnsys.ttts.facade.messages

object TttsFacadeMessages {

	val FEED_REQUEST_MESSAGE_TYPE = "FEED_REQ"
	val FEED_STOP_REQUEST_MESSAGE_TYPE = "FEED_STOP_REQ"
	val FEED_RESPONSE_MESSAGE_TYPE = "FEED_RSP"

	val STRATEGY_REQUEST_MESSAGE_TYPE = "STRATEGY_REQ"
	val STRATEGY_STOP_REQUEST_MESSAGE_TYPE = "STRATEGY_STOP_REQ"
	val STRATEGY_RESPONSE_MESSAGE_TYPE = "STRATEGY_RSP"

	val ENGINE_REQUEST_MESSAGE_TYPE = "ENGINE_REQ"
	val ENGINE_STOP_REQUEST_MESSAGE_TYPE = "ENGINE_STOP_REQ"
	val ENGINE_RESPONSE_MESSAGE_TYPE = "ENGINE_RSP"
	  
    sealed trait TttsFacadeMessage
	case object Stop extends TttsFacadeMessage
	// Message received from UI clients from WebSocket
	case class FacadePayload(payload: String) extends TttsFacadeMessage
	case class FeedPayload(datetime: String, ticker: String, open: Double, high: Double, low: Double, close: Double, volume: Long, wap: Double, size: Long, payload: String) extends TttsFacadeMessage
	case class StrategyPayload(datetime: String, ticker: String, open: Double, high: Double, low: Double, close: Double, volume: Long, wap: Double, size: Long, payload: String, lowerBB: Double, middBB: Double, upperBB: Double) extends TttsFacadeMessage
	case class EnginePayload(datetime: String, ticker: String, open: Double, high: Double, low: Double, close: Double, volume: Long, wap: Double, size: Long, payload: String, funds: Double, balance: Double, tradesNum: Long, inTrade: Boolean, positionSize: Long) extends TttsFacadeMessage

	case class FacadeClientMessage(msgType: String, payload: Option[FacadePayload]) extends TttsFacadeMessage
	// Message published to Facade Topic MQ to request services processing
	case class RequestFacadeMessage(id: String, msgType: String, client: String, payload: Option[FacadePayload], timestamp: String, sequenceNum: String) extends TttsFacadeMessage
	// Message received from Facade Topic MQ in response to services processing request
	case class ResponseFacadeMessage(id: String, msgType: String, client: String, payload: Option[FacadePayload], timestamp: String, sequenceNum: String) extends TttsFacadeMessage

	// Message published to Facade Topic MQ to request services processing
	case class RequestFeedFacadeMessage(id: String, msgType: String, client: String, payload: Option[FeedPayload], timestamp: String, sequenceNum: String) extends TttsFacadeMessage
	// Message received from Facade Topic MQ in response to services processing request
	case class ResponseFeedFacadeMessage(id: String, msgType: String, client: String, payload: Option[FeedPayload], timestamp: String, sequenceNum: String) extends TttsFacadeMessage
	
	// Message published to Facade Topic MQ to request services processing
	case class RequestStrategyFacadeMessage(id: String, msgType: String, client: String, payload: Option[StrategyPayload], timestamp: String, sequenceNum: String) extends TttsFacadeMessage
	// Message received from Facade Topic MQ in response to services processing request
	case class ResponseStrategyFacadeMessage(id: String, msgType: String, client: String, payload: Option[StrategyPayload], timestamp: String, sequenceNum: String, signal: String) extends TttsFacadeMessage

	// Message published to Facade Topic MQ to request services processing
	case class RequestEngineFacadeMessage(id: String, msgType: String, client: String, payload: Option[EnginePayload], timestamp: String, sequenceNum: String) extends TttsFacadeMessage
	// Message received from Facade Topic MQ in response to services processing request
	case class ResponseEngineFacadeMessage(id: String, msgType: String, client: String, payload: Option[EnginePayload], timestamp: String, sequenceNum: String, signal: String) extends TttsFacadeMessage

}


