package com.pvnsys.ttts.strategy.generator

import akka.actor.{Actor, ActorLogging, Props, PoisonPill}
import java.net.InetSocketAddress
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.strategy.Configuration
import com.pvnsys.ttts.strategy.mq.StrategyActor
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.{TttsStrategyMessage, RequestStrategyFacadeTopicMessage, ResponseStrategyFacadeTopicMessage, RequestStrategyServicesTopicMessage, RequestFeedServicesTopicMessage}
import com.pvnsys.ttts.strategy.mq.{KafkaFacadeTopicProducerActor, KafkaServicesTopicProducerActor}
import scala.util.Random
import akka.actor.ActorRef
import scala.concurrent.duration._
import com.pvnsys.ttts.strategy.util.Utils

object StrategyExecutorActor {

  sealed trait StrategyExecutorMessage

  case class StartStrategyExecutorFacadeMessage(req: TttsStrategyMessage) extends StrategyExecutorMessage
  case class StartStrategyExecutorServicesMessage(req: TttsStrategyMessage) extends StrategyExecutorMessage
  case object StopStrategyExecutorMessage extends StrategyExecutorMessage
  
  case class StartPublishResultsMessage(msg: TttsStrategyMessage) extends StrategyExecutorMessage
  case object StopPublishResultsMessage extends StrategyExecutorMessage

  def props(serviceId: String) = Props(new StrategyExecutorActor(serviceId))
  
}

/**
 * Producer of Feed messages.
 * 
 */
class StrategyExecutorActor(serviceId: String) extends Actor with ActorLogging {

  import StrategyExecutorActor._
  import Utils._
  import TttsStrategyMessages._

  
  var isActive = false
	
  override def receive = {
    case req: RequestStrategyFacadeTopicMessage => {
      log.debug("StrategyExecutorActor received RequestStrategyFacadeTopicMessage: {}", req)
      isActive = true
	  req.msgType match {
		    case STRATEGY_REQUEST_MESSAGE_TYPE => startStrategyFeed(req)
		    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => stopStrategyFeed(req)
      }
    }

    case req: RequestStrategyServicesTopicMessage => {
      log.debug("StrategyExecutorActor Received RequestStrategyServicesTopicMessage: {}", req)
      isActive = true
	  req.msgType match {
		    case STRATEGY_REQUEST_MESSAGE_TYPE => startStrategyFeed(req)
		    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => stopStrategyFeed(req)
      }
    }

    case req: ResponseFeedFacadeTopicMessage => {
      log.debug("StrategyExecutorActor Received ResponseFeedFacadeTopicMessage: {}", req)
      isActive = true
      startFacadeStrategyProducer(req)
    }

    case req: ResponseFeedServicesTopicMessage => {
      log.debug("StrategyExecutorActor Received ResponseFeedServicesTopicMessage: {}", req)
      isActive = true
      startServicesStrategyProducer(req)
    }
    
    case StopStrategyExecutorMessage => {
      log.debug("StrategyExecutorActor Received StopStrategyExecutorMessage. Terminating feed")
      isActive = false
      context stop self
    }

//    case req: ResponseFeedServicesTopicMessage => {
//      log.debug("StrategyExecutorActor Received ResponseFeedServicesTopicMessage: {}", req)
//      isActive = true
//      startStrategyProducer(req)
//    }
    
    case msg => log.error(s"StrategyExecutorActor Received unknown message $msg")
    
    
  }
  
  override def postStop() = {
  }
  
  private def startFacadeStrategyProducer(msg: ResponseFeedFacadeTopicMessage) = {
    
    /*
     * Do Strategy processing, create ResponseStrategyFacadeTopicMessage and publish it to Kafka Facade Topic (reply to FacadeMS)
     * 
     */ 
    
    // 1. Do some fake Strategy processing here. Replace with real code.
    val fraction = msg.payload.toDouble - msg.payload.toDouble.intValue
    val signal = fraction match {
      case x if(x < 0.2) => "BUY"
      case x if(x > 0.8) => "SELL"
      case _ => "HOLD"
    }
    
    // 2. Create ResponseStrategyFacadeTopicMessage

    // Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
    val messageTraits = Utils.generateMessageTraits
    val message = ResponseStrategyFacadeTopicMessage(messageTraits._1, STRATEGY_RESPONSE_MESSAGE_TYPE, msg.client, msg.payload, messageTraits._2, msg.sequenceNum, signal)
    
    // 3. Publish results back to Facade Topic
    val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
    kafkaFacadeTopicProducerActor ! message
    
  }


  
  private def startServicesStrategyProducer(msg: ResponseFeedServicesTopicMessage) = {
    
    /*
     * Do Strategy processing, create ResponseFeedServicesTopicMessage and publish it to Kafka Facade Topic (reply to FacadeMS)
     * 
     */ 
    
    // 1. Do some fake Strategy processing here. Replace with real code.
    val fraction = msg.payload.toDouble - msg.payload.toDouble.intValue
    val signal = fraction match {
      case x if(x < 0.2) => "BUY"
      case x if(x > 0.8) => "SELL"
      case _ => "HOLD"
    }
    
    // 2. Create ResponseFeedServicesTopicMessage

    // Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
    val messageTraits = Utils.generateMessageTraits
    val message = ResponseStrategyServicesTopicMessage(messageTraits._1, STRATEGY_RESPONSE_MESSAGE_TYPE, msg.client, msg.payload, messageTraits._2, msg.sequenceNum, signal, serviceId)
    
    // 3. Publish results back to Services Topic
    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
    kafkaServicesTopicProducerActor ! message
    
  }
  
  
  private def startStrategyFeed(msg: TttsStrategyMessage) = {
    
    /*
     * Create FeedRequestServiceTopicMessage and publish it to Kafka Services Topic (request feed from FeedMS)
     * 
     */ 
    getQuotesFeed(msg)
  }
  
    private def stopStrategyFeed(msg: TttsStrategyMessage) = {
      // Send FEED_STOP_REQ message to services topic here
        // Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
        val messageTraits = Utils.generateMessageTraits
        // Sending one and only FEED_REQ message to Services topic, thus sequenceNum is hardcoded "0"
        val feedRequestMessage = RequestFeedServicesTopicMessage(messageTraits._1, FEED_STOP_REQUEST_MESSAGE_TYPE, msg.asInstanceOf[RequestStrategyFacadeTopicMessage].client, msg.asInstanceOf[RequestStrategyFacadeTopicMessage].payload, messageTraits._2, "0", serviceId)
        log.debug("******* StrategyExecutorActor publishing FEED_STOP_REQUEST to KafkaServicesTopicProducerActor: {}", feedRequestMessage)
    
        // Publishing message to Services Topic
        val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
        kafkaServicesTopicProducerActor ! feedRequestMessage
   }
  
  
  private def getQuotesFeed(msg: TttsStrategyMessage) = {
        // Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
        val messageTraits = Utils.generateMessageTraits
        // Sending one and only FEED_REQ message to Services topic, thus sequenceNum is hardcoded "0"
        val feedRequestMessage = RequestFeedServicesTopicMessage(messageTraits._1, FEED_REQUEST_MESSAGE_TYPE, msg.asInstanceOf[RequestStrategyFacadeTopicMessage].client, msg.asInstanceOf[RequestStrategyFacadeTopicMessage].payload, messageTraits._2 , "0", serviceId)
    
        log.debug("StrategyExecutorActor publishing FEED_REQUEST to KafkaServicesTopicProducerActor: {}", feedRequestMessage)
        // Publishing message to Services Topic
        val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
        kafkaServicesTopicProducerActor ! feedRequestMessage
  }
  
  
//  private def publishResults(msg: TttsStrategyMessage) = {
//	val publishStrategyResultsActor = context.actorOf(Props(classOf[PublishStrategyResultsActor]))
//	publishStrategyResultsActor ! StartPublishResultsMessage(msg)
//  }
  
  
}



//class PublishStrategyResultsActor extends Actor with ActorLogging {
//	import StrategyExecutorActor._
//    import TttsStrategyMessages._
//    
//	var counter = 0
//	
//	override def receive = {
//		case StartPublishResultsMessage(msg) => 
//
//		    msg match {
//				case msg: RequestStrategyFacadeTopicMessage => {
//				  	// Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
//			        val messageTraits = Utils.generateMessageTraits
//			        log.debug(s"PublishStrategyResultsActor, Gettin message: {}", msg)
//		  		    counter += 1
//			    	val fakeQuote = "%.2f".format(Random.nextFloat() + 77)
//				    val fakeMessage = ResponseStrategyFacadeTopicMessage(messageTraits._1, STRATEGY_RESPONSE_MESSAGE_TYPE, msg.client , s"$fakeQuote", messageTraits._2, s"$counter", "HOLD")
//				    val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
//				    kafkaFacadeTopicProducerActor ! fakeMessage
//				}
//				case msg: RequestStrategyServicesTopicMessage => {
////			    	val fakeQuote = "%.2f".format(Random.nextFloat() + 55)
////				    val fakeMessage = ResponseFeedServicesTopicMessage(messageTraits._1, FEED_RESPONSE_MESSAGE_TYPE, msg.client , s"$fakeQuote", messageTraits._2, s"$counter")
////				    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
////				    kafkaServicesTopicProducerActor ! fakeMessage
//				}
//				case _ =>
//		    }
//		  
//	    case StopPublishResultsMessage =>
//		  log.debug("PublisStrategyResultsActor Cancel")
//	      context.stop(self)
//		case _ => log.error("PublishStrategyResultsActor Received unknown message")
//	}
//  
//}
