package com.pvnsys.ttts.facade.messages

object TttsFacadeMessages {

	val FEED_REQUEST_MESSAGE_TYPE = "FEED_REQ"
	val FEED_STOP_REQUEST_MESSAGE_TYPE = "FEED_STOP_REQ"
	val FEED_RESPONSE_MESSAGE_TYPE = "FEED_RSP"
  
    sealed trait TttsFacadeMessage
	case object Stop extends TttsFacadeMessage
	// Message received from UI clients from WebSocket
	case class FacadeClientMessage(msgType: String, payload: String) extends TttsFacadeMessage
	// Message published to Facade Topic MQ to request services processing
	case class RequestFacadeMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String) extends TttsFacadeMessage
	// Message received from Facade Topic MQ in response to services processing request
	case class ResponseFacadeMessage(id: String, msgType: String, client: String, payload: String, timestamp: String, sequenceNum: String) extends TttsFacadeMessage
}


