package com.pvnsys.ttts.feed.generator

import akka.actor.{Actor, ActorLogging, Props, PoisonPill}
import java.net.InetSocketAddress
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.feed.Configuration
import com.pvnsys.ttts.feed.mq.FeedActor
import com.pvnsys.ttts.feed.messages.TttsFeedMessages
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.{TttsFeedMessage, RequestFeedFacadeTopicMessage, ResponseFeedFacadeTopicMessage, RequestFeedServicesTopicMessage}
import com.pvnsys.ttts.feed.mq.{KafkaFacadeTopicProducerActor, KafkaServicesTopicProducerActor}
import scala.util.Random
import akka.actor.ActorRef
import scala.concurrent.duration._
import com.pvnsys.ttts.feed.util.Utils
import scala.io.Source

object FeedGeneratorActor {

  sealed trait FeedGeneratorMessage

  case class StartFeedGeneratorFacadeMessage(req: TttsFeedMessage) extends FeedGeneratorMessage
  case class StartFeedGeneratorServicesMessage(req: TttsFeedMessage) extends FeedGeneratorMessage
  case object StopFeedGeneratorMessage extends FeedGeneratorMessage
  
  case class StartFakeFeedGeneratorMessage(msg: TttsFeedMessage, initSize: Int, count: Int) extends FeedGeneratorMessage
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
  var count = 0
	
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
      self ! PoisonPill
    }
    
    case msg => log.error(s"FeedGeneratorActor Received unknown message $msg")
    
    
  }
  
  override def postStop() = {
  }
  
  private def startFeed(req: TttsFeedMessage) = {
    
    // Real IB API call will be added here. For now all is fake
    getFakeFeed(req)
  }
  
  private def getFakeFeed(msg: TttsFeedMessage) = {
	val fakeFeedActor = context.actorOf(Props(classOf[FakeFeedActor]))
	// Simple scheduler that every second sends StartFakeFeedGeneratorMessage to fakeFeedActor with sender specified as 'self'
	
	  val filename = "in/quotes.csv"
  	  val initSize = Source.fromFile(filename).getLines.length
	 
    context.system.scheduler.schedule(0.seconds, 1.second, fakeFeedActor, StartFakeFeedGeneratorMessage(msg, initSize, increment(count)))(context.system.dispatcher, self)
  }
  private def increment(i: Int) = {
    count += 1
    count
  }
  
}



class FakeFeedActor extends Actor with ActorLogging {
	import FeedGeneratorActor._
    import TttsFeedMessages._
    
	var counter = 0
	
	override def receive = {
		case StartFakeFeedGeneratorMessage(mess, initSize, count) => 

	        // Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
	        val messageTraits = Utils.generateMessageTraits
	        log.debug(s"FakeFeedActor, Gettin message: {}", mess)
//  		    counter += 1
		    
		    mess match {
				case msg: RequestFeedFacadeTopicMessage => {
//			    	val fakeQuote = "%.2f".format(Random.nextDouble() + 22)
				  counter += 1
			    	val fakeQuote = getQuoteFromFile(initSize, counter)
				    val fakeMessage = ResponseFeedFacadeTopicMessage(messageTraits._1, FEED_RESPONSE_MESSAGE_TYPE, msg.client , s"$fakeQuote", messageTraits._2, s"$counter")
				    val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
				    kafkaFacadeTopicProducerActor ! fakeMessage
				}
				case msg: RequestFeedServicesTopicMessage => {
				  counter += 1
//			    	val fakeQuote = "%.2f".format(Random.nextDouble() + 55)
			    	val fakeQuote = getQuoteFromFile(initSize, counter)
				    val fakeMessage = ResponseFeedServicesTopicMessage(messageTraits._1, FEED_RESPONSE_MESSAGE_TYPE, msg.client , s"$fakeQuote", messageTraits._2, s"$counter", msg.serviceId)
				    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
				    kafkaServicesTopicProducerActor ! fakeMessage
				}
				case _ =>
		    }
		    
		
	    case StopFakeFeedGeneratorMessage =>
		  log.debug("FakeFeedActor Cancel")
	      context.stop(self)
		case _ => log.error("FakeFeedActor Received unknown message")
	}
	
  
	// returns close price from file one at a time. When reaches the EOF, restarts from the beginning. Skips first 'headers' line
	final def getQuoteFromFile(z: Int, count: Int) = {
		  val filename = "in/quotes.csv"
		  val iter = Source.fromFile(filename).getLines
          var n = count%z
          if(n != 0) {
			  val line = iter.drop(n).next.toString.split(",")
//			  println(line)
//			  print(s"date: ${line(0)}    ")
//			  print(s"open: ${line(1)}    ")
//			  print(s"high: ${line(2)}    ")
//			  print(s"low: ${line(3)}     ")
//			  print(s"close: ${line(4)}   ")
//			  println(s"vol: ${line(5)}")
			  val is = iter.size
			  val closePrice = line(4)
//			  println(is)
//		      Thread.sleep(1000)
		      is match {
		        case 0 => Source.fromFile(filename).reset
		        case _ => 
		      }
	//	      n += 1
              closePrice
          }
    
  }
  
}
