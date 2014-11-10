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
import com.pvnsys.ttts.strategy.impl.FakeStrategy

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
      startStrategy(req)
    }

    case req: ResponseFeedServicesTopicMessage => {
      log.debug("StrategyExecutorActor Received ResponseFeedServicesTopicMessage: {}", req)
      isActive = true
      startStrategy(req)
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
  
  
  private def startStrategy(msg: TttsStrategyMessage) = {
    
    /*
     * Do Strategy processing, create ResponseStrategyFacadeTopicMessage and publish it to Kafka Facade Topic (reply to FacadeMS)
     * 
     */ 
    
    // Put a real strategy call here
    val message = new FakeStrategy().process(msg)
    
    // Publish results back to Facade Topic
    val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
    kafkaFacadeTopicProducerActor ! message
    
  }


  
//  private def startServicesStrategyProducer(msg: ResponseFeedServicesTopicMessage) = {
//    
//    /*
//     * Do Strategy processing, create ResponseFeedServicesTopicMessage and publish it to Kafka Facade Topic (reply to FacadeMS)
//     * 
//     */ 
//    
//    // Put a real strategy call here
//    val message = new FakeStrategy().process(msg)
//    
//    // 3. Publish results back to Services Topic
//    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
//    kafkaServicesTopicProducerActor ! message
//    
//  }
  
  
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
        
        msg match {
          case x: RequestStrategyFacadeTopicMessage => {
		        // Sending one and only FEED_REQ message to Services topic, thus sequenceNum is hardcoded "0"
		        val feedRequestMessage = RequestFeedServicesTopicMessage(messageTraits._1, FEED_REQUEST_MESSAGE_TYPE, msg.asInstanceOf[RequestStrategyFacadeTopicMessage].client, msg.asInstanceOf[RequestStrategyFacadeTopicMessage].payload, messageTraits._2 , "0", serviceId)
		    
		        log.debug("StrategyExecutorActor publishing FEED_REQUEST to KafkaServicesTopicProducerActor: {}", feedRequestMessage)
		        // Publishing message to Services Topic
		        val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
		        kafkaServicesTopicProducerActor ! feedRequestMessage
            
          }
          case x: RequestStrategyServicesTopicMessage => {
		        // Sending one and only FEED_REQ message to Services topic, thus sequenceNum is hardcoded "0"
		        val feedRequestMessage = RequestFeedServicesTopicMessage(messageTraits._1, FEED_REQUEST_MESSAGE_TYPE, msg.asInstanceOf[RequestStrategyServicesTopicMessage].client, msg.asInstanceOf[RequestStrategyServicesTopicMessage].payload, messageTraits._2 , "0", serviceId)
		    
		        log.debug("StrategyExecutorActor publishing FEED_REQUEST to KafkaServicesTopicProducerActor: {}", feedRequestMessage)
		        // Publishing message to Services Topic
		        val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
		        kafkaServicesTopicProducerActor ! feedRequestMessage
            
          }
          case _ => "Do nothing"
        }
        
  }
  
  
  override def postStop() = {
  }
    
}
