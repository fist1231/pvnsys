package com.pvnsys.ttts.feed.messages

object TttsFeedMessages {
  
	val FEED_REQUEST_MESSAGE_TYPE = "FEED_REQ"
	val FEED_STOP_REQUEST_MESSAGE_TYPE = "FEED_STOP_REQ"
	val FEED_RESPONSE_MESSAGE_TYPE = "FEED_RSP"
  
	sealed trait TttsFeedMessage
	case object StartFeedServiceMessage extends TttsFeedMessage
	case object StopFeedServiceMessage extends TttsFeedMessage

	case object StartListeningFacadeTopicMessage extends TttsFeedMessage
	case object StartListeningServicesTopicMessage extends TttsFeedMessage
	
	case object StartKafkaServicesTopicConsumerMessage extends TttsFeedMessage

	case class FacadePayload(payload: String) extends TttsFeedMessage
	case class FeedPayload(datetime: String, ticker: String, open: Double, high: Double, low: Double, close: Double, volume: Long, wap: Double, size: Long, payload: String) extends TttsFeedMessage
	
	case class FacadeTopicMessage(id: String, msgType: String, client: String, payload: Option[FeedPayload], timestamp: String, sequenceNum: String) extends TttsFeedMessage
	case class RequestFeedFacadeTopicMessage(id: String, msgType: String, client: String, payload: Option[FeedPayload], timestamp: String, sequenceNum: String) extends TttsFeedMessage
	case class ResponseFeedFacadeTopicMessage(id: String, msgType: String, client: String, payload: Option[FeedPayload], timestamp: String, sequenceNum: String) extends TttsFeedMessage

	case class ServicesTopicMessage(id: String, msgType: String, client: String, payload: Option[FeedPayload], timestamp: String, sequenceNum: String, serviceId: String) extends TttsFeedMessage
	case class RequestFeedServicesTopicMessage(id: String, msgType: String, client: String, payload: Option[FeedPayload], timestamp: String, sequenceNum: String, serviceId: String) extends TttsFeedMessage
	case class ResponseFeedServicesTopicMessage(id: String, msgType: String, client: String, payload: Option[FeedPayload], timestamp: String, sequenceNum: String, serviceId: String) extends TttsFeedMessage

}


