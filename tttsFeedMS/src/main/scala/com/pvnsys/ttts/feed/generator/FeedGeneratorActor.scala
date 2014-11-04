package com.pvnsys.ttts.feed.generator

import akka.actor.{Actor, ActorLogging, Props, PoisonPill}
import java.net.InetSocketAddress
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.feed.Configuration
import com.pvnsys.ttts.feed.mq.FeedActor
import com.pvnsys.ttts.feed.messages.TttsFeedMessages
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.{RequestFeedFacadeTopicMessage, ResponseFeedFacadeTopicMessage}
import com.pvnsys.ttts.feed.mq.KafkaFacadeTopicProducerActor
import scala.util.Random
import akka.actor.ActorRef
import scala.concurrent.duration._
import com.pvnsys.ttts.feed.util.Utils

object FeedGeneratorActor {

  sealed trait FeedGeneratorMessage

  case class StartFeedGeneratorMessage(req: RequestFeedFacadeTopicMessage) extends FeedGeneratorMessage
  case object StopFeedGeneratorMessage extends FeedGeneratorMessage
  
  case class StartFakeFeedGeneratorMessage(msg: RequestFeedFacadeTopicMessage) extends FeedGeneratorMessage
  case object StopFakeFeedGeneratorMessage extends FeedGeneratorMessage
  
}

/**
 * Producer of Feed messages.
 * 
 */
class FeedGeneratorActor extends Actor with ActorLogging {

  import FeedGeneratorActor._
  import Utils._
  
  var isActive = false
	
  override def receive = {
    case req: RequestFeedFacadeTopicMessage => {
      log.debug("FeedGeneratorActor Received RequestFeedFacadeTopicMessage: {}", req)
      isActive = true
      startFeed(req)
    }

    case StopFeedGeneratorMessage => {
      log.debug("FeedGeneratorActor Received StopFeedGeneratorMessage. Terminating feed")
      isActive = false
      self ! PoisonPill
    }
    
    case msg => log.error(s"FeedGeneratorActor Received unknown message $msg")
    
    
  }
  
  override def postStop() = {
  }
  
  private def startFeed(req: RequestFeedFacadeTopicMessage) = {
    
    // Real IB API call will be added here. For now all is fake
    getFakeFeed(req)
  }
  
  private def getFakeFeed(msg: RequestFeedFacadeTopicMessage) = {
	val fakeFeedActor = context.actorOf(Props(classOf[FakeFeedActor]))
	// Simple scheduler that every second sends StartFakeFeedGeneratorMessage to fakeFeedActor with sender specified as 'self'
    context.system.scheduler.schedule(0.seconds, 1.second, fakeFeedActor, StartFakeFeedGeneratorMessage(msg))(context.system.dispatcher, self)
  }
  
}



class FakeFeedActor extends Actor with ActorLogging {
	import FeedGeneratorActor._
    import TttsFeedMessages._
    
	var counter = 0
	
	override def receive = {
		case StartFakeFeedGeneratorMessage(msg) => 

	        // Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
	        val messageTraits = Utils.generateMessageTraits
		  
	        log.debug(s"FakeFeedActor, Gettin message: {}", msg)
  		    counter += 1
	    	val fakeQuote = "%.2f".format(Random.nextFloat() + 22)
		    val fakeMessage = ResponseFeedFacadeTopicMessage(messageTraits._1, FEED_RESPONSE_MESSAGE_TYPE, msg.client , s"$fakeQuote", messageTraits._2, s"$counter")
		    val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
		    kafkaFacadeTopicProducerActor ! fakeMessage
		
	    case StopFakeFeedGeneratorMessage =>
		  log.debug("FakeFeedActor Cancel")
	      context.stop(self)
		case _ => log.error("FakeFeedActor Received unknown message")
	}
  
}
