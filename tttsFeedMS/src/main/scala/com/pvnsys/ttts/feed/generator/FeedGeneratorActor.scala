package com.pvnsys.ttts.feed.generator

import akka.actor.{Actor, ActorLogging, Props, PoisonPill}
import java.net.InetSocketAddress
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.feed.Configuration
import com.pvnsys.ttts.feed.mq.FeedActor
import com.pvnsys.ttts.feed.messages.TttsFeedMessages
import com.pvnsys.ttts.feed.mq.{KafkaFacadeTopicProducerActor, KafkaServicesTopicProducerActor}
import scala.util.Random
import akka.actor.ActorRef
import scala.concurrent.duration._
import com.pvnsys.ttts.feed.util.Utils
import scala.io.Source
import com.pvnsys.ttts.feed.impl.FakeFeed

object FeedGeneratorActor {
  
  import TttsFeedMessages._

  sealed trait FeedGeneratorMessage
  case class StartFeedGeneratorFacadeMessage(req: TttsFeedMessage) extends FeedGeneratorMessage
  case class StartFeedGeneratorServicesMessage(req: TttsFeedMessage) extends FeedGeneratorMessage
  case object StopFeedGeneratorMessage extends FeedGeneratorMessage
  case class StartFakeFeedGeneratorMessage(msg: TttsFeedMessage, initSize: Int) extends FeedGeneratorMessage
  case object StopFakeFeedGeneratorMessage extends FeedGeneratorMessage
}

/**
 * Producer of Feed messages.
 * 
 */
class FeedGeneratorActor extends Actor with ActorLogging {

  import FeedGeneratorActor._
  import Utils._
  import TttsFeedMessages._
  
  var isActive = false
//  var count = 0
	
  override def receive = {
    case req: RequestFeedFacadeTopicMessage => {
      log.debug("FeedGeneratorActor Received RequestFeedFacadeTopicMessage: {}", req)
      isActive = true
      startFeed(req)
    }

    case req: RequestFeedServicesTopicMessage => {
      log.debug("FeedGeneratorActor Received RequestFeedServicesTopicMessage: {}", req)
      isActive = true
      startFeed(req)
    }
    
    case StopFeedGeneratorMessage => {
      log.debug("FeedGeneratorActor Received StopFeedGeneratorMessage. Terminating feed")
      isActive = false
      context stop self
    }
    
    case msg => log.error(s"FeedGeneratorActor Received unknown message $msg")
    
    
  }
  
  override def postStop() = {
  }
  
  private def startFeed(req: TttsFeedMessage) = {
    
    // Real IB or other API call will be added here. For now all is fake
    getFakeFeed(req)
  }
  
  private def getFakeFeed(msg: TttsFeedMessage) = {
	val fakeFeedActor = context.actorOf(Props(classOf[FakeFeedActor]))
	// Simple scheduler that every second sends StartFakeFeedGeneratorMessage to fakeFeedActor with sender specified as 'self'
	
	  val filename = "in/quotes.csv"
  	  val initSize = Source.fromFile(filename).getLines.length
	 
    context.system.scheduler.schedule(0.seconds, 100.milliseconds, fakeFeedActor, StartFakeFeedGeneratorMessage(msg, initSize))(context.system.dispatcher, self)
  }
  
//  private def increment(i: Int) = {
//    count += 1
//    count
//  }
  
}



class FakeFeedActor extends Actor with ActorLogging {
	import FeedGeneratorActor._
    import TttsFeedMessages._
    
	var counter = 0
	
	override def receive = {
		case StartFakeFeedGeneratorMessage(msg, initSize) => 

	        log.debug(s"FakeFeedActor, Gettin message: {}", msg)

	        counter += 1
		    // Put a real strategy call here
		    val message = new FakeFeed().process(msg, initSize, counter)
		    
		    message match {
				case x: ResponseFeedFacadeTopicMessage => {
				    val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
				    kafkaFacadeTopicProducerActor ! x
				}
				case x: ResponseFeedServicesTopicMessage => {
				    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
				    kafkaServicesTopicProducerActor ! x
				}
				case _ => 
		    }
		    
		
	    case StopFakeFeedGeneratorMessage =>
		  log.debug("FakeFeedActor Cancel")
	      context stop self
		case _ => log.error("FakeFeedActor Received unknown message")
	}
	
  
}
