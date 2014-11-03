package com.pvnsys.ttts.feed.messages

object TttsFeedMessages {
	sealed trait TttsFeedMessage
	case object StartFeedServiceMessage extends TttsFeedMessage
	case object StopFeedServiceMessage extends TttsFeedMessage

	case object StartListeningFacadeTopicMessage extends TttsFeedMessage
	case object StartListeningServicesTopicMessage extends TttsFeedMessage
	
	case object StartKafkaServicesTopicConsumerMessage extends TttsFeedMessage

	case class FacadeTopicMessage(id: String, msgType: String, client: String, payload: String) extends TttsFeedMessage
	case class RequestFeedFacadeTopicMessage(id: String, msgType: String, client: String, payload: String) extends TttsFeedMessage
	case class ResponseFeedFacadeTopicMessage(id: String, msgType: String, client: String, payload: String) extends TttsFeedMessage

	case class ServicesTopicMessage(id: String, msgType: String, client: String, payload: String) extends TttsFeedMessage
	case class RequestFeedServicesTopicMessage(id: String, msgType: String, client: String, payload: String) extends TttsFeedMessage
	case class ResponseFeedServicesTopicMessage(id: String, msgType: String, client: String, payload: String) extends TttsFeedMessage
	
}


