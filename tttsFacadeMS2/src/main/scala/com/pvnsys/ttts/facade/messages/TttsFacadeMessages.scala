package com.pvnsys.ttts.facade.messages

object TttsFacadeMessages {
	sealed trait TttsFacadeMessage
	case object Stop extends TttsFacadeMessage
	// Message received from UI clients from WebSocket
	case class FacadeClientMessage(id: String, msgType: String, client: String, payload: String) extends TttsFacadeMessage
	// Message published to Facade Topic MQ to request services processing
	case class RequestFacadeMessage(id: String, msgType: String, client: String, payload: String) extends TttsFacadeMessage
	// Message received from Facade Topic MQ in response to services processing request
	case class ResponseFacadeMessage(id: String, msgType: String, client: String, payload: String) extends TttsFacadeMessage
}


