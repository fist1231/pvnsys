package com.pvnsys.ttts.facade.messages

object TttsFacadeMessages {
	sealed trait TttsFacadeMessage
	case object Stop extends TttsFacadeMessage
	case class FacadeIncomingMessage(key: String, message: String) extends TttsFacadeMessage
	case class FacadeOutgoingMessage(id: String) extends TttsFacadeMessage
	
	case class FacadeClientFeedRequestMessage(id: String, msgType: String, payload: String) extends TttsFacadeMessage
	case class FacadeOutgoingFeedRequestMessage(id: String, client: String) extends TttsFacadeMessage
	case class FacadeIncomingFeedResponseMessage(id: String, client: String, quote: String) extends TttsFacadeMessage
	
	//case class KafkaConsumerMessage() extends TttsFacadeMessage
}


