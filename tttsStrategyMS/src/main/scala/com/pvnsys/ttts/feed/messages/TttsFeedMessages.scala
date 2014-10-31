package com.pvnsys.ttts.feed.messages

object TttsFeedMessages {
	sealed trait TttsFeedMessage
	case object StartFeedServiceMessage extends TttsFeedMessage
	case object StopFeedServiceMessage extends TttsFeedMessage
	case object StartListeningFacadeTopicMessage extends TttsFeedMessage
	case object StartKafkaServicesTopicConsumerMessage extends TttsFeedMessage

	case class FacadeTopicMessage(id: String, msgType: String, client: String, payload: String) extends TttsFeedMessage
	case class RequestFeedFacadeTopicMessage(id: String, msgType: String, client: String, payload: String) extends TttsFeedMessage
	case class ResponseFeedFacadeTopicMessage(id: String, msgType: String, client: String, payload: String) extends TttsFeedMessage

	case class ServicesTopicMessage(id: String, msgType: String, client: String, payload: String) extends TttsFeedMessage
	
//	case object FeedPushMessage
//	case class TickQuote(wSock: WebSocket)
//	case class KafkaNewMessage(message: String)
//	case class KafkaProducerMessage(id: String)
//	case class KafkaConsumerMessage()
//	case class KafkaReceivedMessage(key: String, message: String)
//	
//	
//	case class FeedIncomingMessage(key: String, message: String) extends TttsFeedMessage
//	case class FeedOutgoingMessage(id: String) extends TttsFeedMessage
//	
//	case class FeedClientFeedRequestMessage(id: String, msgType: String, payload: String) extends TttsFeedMessage
//	case class FeedOutgoingFeedRequestMessage(id: String, client: String) extends TttsFeedMessage
//	case class FeedIncomingFeedResponseMessage(id: String, client: String, quote: String) extends TttsFeedMessage
}


