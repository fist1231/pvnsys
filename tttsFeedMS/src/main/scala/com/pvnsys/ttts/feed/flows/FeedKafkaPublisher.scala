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
    
    // Creating Producer Actor to post outgoing message to facade topic MQ. Creating new Actor for every message
//    val kafkaProducerActor = system.actorOf(KafkaProducerActor.props(new InetSocketAddress("127.0.0.1", 5672)))
//    kafkaProducerActor ! msg
//    kafkaProducerActor ! FacadeOutgoingMessage(wid)
//	kafkaProducerActor ! FeedActorStopMessage

  }
  
}