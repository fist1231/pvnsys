package com.pvnsys.ttts.feed.flows

import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Duct
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.RequestFeedFacadeTopicMessage

/**
 * Wraps the action of publishing to a Kafka facade topic MQ and exposes it as a Flow processing.
 * 
 */
class FeedKafkaPublisher {

  val flow: Duct[RequestFeedFacadeTopicMessage, Unit] = 
    Duct[RequestFeedFacadeTopicMessage] foreach { 
      msg => startFeedStream(msg) 
    }
  
  def startFeedStream(msg: RequestFeedFacadeTopicMessage) = {
  }
  
}