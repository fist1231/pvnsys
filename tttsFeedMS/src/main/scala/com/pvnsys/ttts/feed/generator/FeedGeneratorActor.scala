package com.pvnsys.ttts.feed.generator

import akka.actor.{Actor, ActorLogging, Props, PoisonPill}
import java.net.InetSocketAddress
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
//import com.pvnsys.ttts.feed.KafkaProducerMessage
import com.pvnsys.ttts.feed.Configuration
import com.pvnsys.ttts.feed.FeedActor
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.{RequestFeedFacadeTopicMessage, ResponseFeedFacadeTopicMessage}
import com.pvnsys.ttts.feed.mq.KafkaFacadeTopicProducerActor
import scala.util.Random


object FeedGeneratorActor {
  sealed trait FeedGeneratorMessage
  case class StartFeedGeneratorMessage(req: RequestFeedFacadeTopicMessage) extends FeedGeneratorMessage
  case object StopFeedGeneratorMessage extends FeedGeneratorMessage
}

/**
 * Producer of Feed messages.
 * 
 */
class FeedGeneratorActor extends Actor with ActorLogging {

  import FeedGeneratorActor._
	
  override def receive = {
    case req: RequestFeedFacadeTopicMessage => {
      log.debug("+++++ FeedGeneratorActor Received RequestFeedFacadeTopicMessage: {}", req)
      startFeed(req)
    }

    case StopFeedGeneratorMessage => {
      self ! PoisonPill
    }
    
    case msg => log.error(s"+++++ Received unknown message $msg")
    
    
  }
  
  override def postStop() = {
  }
  
  private def startFeed(req: RequestFeedFacadeTopicMessage) = {
    
    // Real IB API call will be added here. For now all is fake
    
    
    getFakeFeed(req)
  }
  
  private def getFakeFeed(msg: RequestFeedFacadeTopicMessage) = {
    var messageNo = 1
    while(true) { //(messageNo <= 10) {
    	val fakeQuote = "%.2f".format(Random.nextFloat()+22)
	    val fakeMessage = ResponseFeedFacadeTopicMessage(s"$messageNo", "FEED_RSP", msg.asInstanceOf[RequestFeedFacadeTopicMessage].client, s"$fakeQuote" )
	    val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
	    kafkaFacadeTopicProducerActor ! fakeMessage
    	Thread.sleep(1000)
    	messageNo += 1
    }    
    
  }
  
  
}