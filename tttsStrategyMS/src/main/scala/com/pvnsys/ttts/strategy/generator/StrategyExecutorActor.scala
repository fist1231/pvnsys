package com.pvnsys.ttts.strategy.generator

import akka.actor.{Actor, ActorLogging, Props, PoisonPill}
import java.net.InetSocketAddress
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.strategy.Configuration
import com.pvnsys.ttts.strategy.mq.StrategyActor
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.{TttsStrategyMessage, RequestStrategyFacadeTopicMessage, ResponseStrategyFacadeTopicMessage, RequestStrategyServicesTopicMessage}
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
  
}

/**
 * Producer of Feed messages.
 * 
 */
class StrategyExecutorActor extends Actor with ActorLogging {

  import StrategyExecutorActor._
  import Utils._
  
  var isActive = false
	
  override def receive = {
    case req: RequestStrategyFacadeTopicMessage => {
      log.debug("StrategyExecutorActor Received RequestFeedFacadeTopicMessage: {}", req)
      isActive = true
      startStrategy(req)
    }

    case req: RequestStrategyServicesTopicMessage => {
      log.debug("StrategyExecutorActor Received RequestFeedServicesTopicMessage: {}", req)
      isActive = true
      startStrategy(req)
    }
    
    case StopStrategyExecutorMessage => {
      log.debug("StrategyExecutorActor Received StopFeedGeneratorMessage. Terminating feed")
      isActive = false
      self ! PoisonPill
    }
    
    case msg => log.error(s"StrategyExecutorActor Received unknown message $msg")
    
    
  }
  
  override def postStop() = {
  }
  
  private def startStrategy(msg: TttsStrategyMessage) = {
    
    // Real IB API call will be added here. For now all is fake
    // 1. Get feed
    // 2. Process feed
    
    // 3. Publish results back to Facade Topic
    
    
    publishResults(msg)
  }
  
  
  private def publishResults(msg: TttsStrategyMessage) = {
	val publishStrategyResultsActor = context.actorOf(Props(classOf[PublishStrategyResultsActor]))
	publishStrategyResultsActor ! StartPublishResultsMessage(msg)
  }
  
  
}



class PublishStrategyResultsActor extends Actor with ActorLogging {
	import StrategyExecutorActor._
    import TttsStrategyMessages._
    
	var counter = 0
	
	override def receive = {
		case StartPublishResultsMessage(msg) => 

		    msg match {
				case msg: RequestStrategyFacadeTopicMessage => {
				  	// Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
			        val messageTraits = Utils.generateMessageTraits
			        log.debug(s"PublishStrategyResultsActor, Gettin message: {}", msg)
		  		    counter += 1
			    	val fakeQuote = "%.2f".format(Random.nextFloat() + 77)
				    val fakeMessage = ResponseStrategyFacadeTopicMessage(messageTraits._1, STRATEGY_RESPONSE_MESSAGE_TYPE, msg.client , s"$fakeQuote", messageTraits._2, s"$counter", "HOLD")
				    val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
				    kafkaFacadeTopicProducerActor ! fakeMessage
				}
				case msg: RequestStrategyServicesTopicMessage => {
//			    	val fakeQuote = "%.2f".format(Random.nextFloat() + 55)
//				    val fakeMessage = ResponseFeedServicesTopicMessage(messageTraits._1, FEED_RESPONSE_MESSAGE_TYPE, msg.client , s"$fakeQuote", messageTraits._2, s"$counter")
//				    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
//				    kafkaServicesTopicProducerActor ! fakeMessage
				}
				case _ =>
		    }
		  
	    case StopPublishResultsMessage =>
		  log.debug("PublisStrategyResultsActor Cancel")
	      context.stop(self)
		case _ => log.error("PublishStrategyResultsActor Received unknown message")
	}
  
}
