package com.pvnsys.ttts.engine.generator

import akka.actor.{Actor, ActorLogging, Props, PoisonPill}
import java.net.InetSocketAddress
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.engine.Configuration
import com.pvnsys.ttts.engine.mq.EngineActor
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import com.pvnsys.ttts.engine.mq.{KafkaFacadeTopicProducerActor, KafkaServicesTopicProducerActor}
import scala.util.Random
import akka.actor.ActorRef
import scala.concurrent.duration._
import com.pvnsys.ttts.engine.util.Utils
import com.pvnsys.ttts.engine.impl.SimulatorEngineActor
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object EngineExecutorActor {

  import TttsEngineMessages._
  sealed trait EngineExecutorMessage

  case class StartEngineExecutorFacadeMessage(req: TttsEngineMessage) extends EngineExecutorMessage
  case class StartEngineExecutorServicesMessage(req: TttsEngineMessage) extends EngineExecutorMessage
  case object StopEngineExecutorMessage extends EngineExecutorMessage
  
  case class StartPublishResultsMessage(msg: TttsEngineMessage) extends EngineExecutorMessage
  case object StopPublishResultsMessage extends EngineExecutorMessage

  def props(serviceId: String) = Props(new EngineExecutorActor(serviceId))
  
}

/**
 * Producer of Feed messages.
 * 
 */
class EngineExecutorActor(serviceId: String) extends Actor with ActorLogging {

  import EngineExecutorActor._
  import Utils._
  import TttsEngineMessages._
  import SimulatorEngineActor._

  
  var isActive = false
  var isInTrade = false
	
  override def receive = {
    case req: RequestEngineFacadeTopicMessage => {
      log.debug("EngineExecutorActor received RequestEngineFacadeTopicMessage: {}", req)
      isActive = true
	  req.msgType match {
		    case ENGINE_REQUEST_MESSAGE_TYPE => startEngineFeed(req)
		    case ENGINE_STOP_REQUEST_MESSAGE_TYPE => stopEngineFeed(req)
      }
    }

    case req: RequestEngineServicesTopicMessage => {
      log.debug("EngineExecutorActor Received RequestEngineServicesTopicMessage: {}", req)
      isActive = true
	  req.msgType match {
		    case ENGINE_REQUEST_MESSAGE_TYPE => startEngineFeed(req)
		    case ENGINE_STOP_REQUEST_MESSAGE_TYPE => stopEngineFeed(req)
      }
    }

    case req: ResponseStrategyFacadeTopicMessage => {
      log.debug("EngineExecutorActor Received ResponseStrategyFacadeTopicMessage: {}", req)
      isActive = true
      startEngine(req)
    }

    case req: ResponseStrategyServicesTopicMessage => {
      log.debug("EngineExecutorActor Received ResponseStrategyServicesTopicMessage: {}", req)
      isActive = true
      startEngine(req)
    }
    
//    case req: SimulatorEngineResponseMessage => {
//      log.debug("EngineExecutorActor Received SimulatorEngineResponseMessage.")
//      processEngineResponse(req.message)
//    }

//    case req: ResponseFeedServicesTopicMessage => {
//      log.debug("EngineExecutorActor Received ResponseFeedServicesTopicMessage: {}", req)
//      isActive = true
//      startEngineProducer(req)
//    }
    

    case StopEngineExecutorMessage => {
      log.debug("EngineExecutorActor Received StopEngineExecutorMessage. Terminating feed")
      isActive = false
      context stop self
    }
    
    case msg => log.error(s"EngineExecutorActor Received unknown message $msg")
    
    
  }
  
  
  private def startEngine(msg: TttsEngineMessage) = {
    
    /*
     * Do Engine processing, create ResponseEngineFacadeTopicMessage and publish it to Kafka Facade Topic (reply to FacadeMS)
     * 
     */ 
	  implicit val timeout = Timeout(2 seconds)
    
	  val engineActor = context.actorOf(Props(classOf[SimulatorEngineActor]))
	  (engineActor ? StartSimulatorEngineMessage(msg, serviceId)).mapTo[TttsEngineMessage] map {msg =>
//	    val msg = result.message
	    processEngineResponse(msg)
	  }
    
    // Put a real engine call here
//    val engineResponseMessage = new SimulatorEngine().process(context, msg, serviceId)
//    
//    engineResponseMessage match {
//      case x: ResponseEngineFacadeTopicMessage => {
//	    // Publish results back to Facade Topic
//	    val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
//	    kafkaFacadeTopicProducerActor ! x 
//      }
//      case x: ResponseEngineServicesTopicMessage => {
//	    // Publish results back to Facade Topic
//	    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
//	    kafkaServicesTopicProducerActor ! x 
//      }
//      case _ => "Do nothing"
//    }

    
  }


  private def processEngineResponse(msg: TttsEngineMessage) = {
    
    msg match {
      case x: ResponseEngineFacadeTopicMessage => {
	    // Publish results back to Facade Topic
	    val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
	    kafkaFacadeTopicProducerActor ! x 
      }
      case x: ResponseEngineServicesTopicMessage => {
	    // Publish results back to Facade Topic
	    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
	    kafkaServicesTopicProducerActor ! x 
      }
      case _ => "Do nothing"
    }

    
  }
  
  
//  private def startServicesEngineProducer(msg: ResponseFeedServicesTopicMessage) = {
//    
//    /*
//     * Do Engine processing, create ResponseFeedServicesTopicMessage and publish it to Kafka Facade Topic (reply to FacadeMS)
//     * 
//     */ 
//    
//    // Put a real engine call here
//    val message = new FakeEngine().process(msg)
//    
//    // 3. Publish results back to Services Topic
//    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
//    kafkaServicesTopicProducerActor ! message
//    
//  }
  
  
  private def startEngineFeed(msg: TttsEngineMessage) = {
    
    /*
     * Create FeedRequestServiceTopicMessage and publish it to Kafka Services Topic (request feed from FeedMS)
     * 
     */ 
    getStrategyFeed(msg)
  }
  
    private def stopEngineFeed(msg: TttsEngineMessage) = {
      // Send FEED_STOP_REQ message to services topic here
        // Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
        val messageTraits = Utils.generateMessageTraits
        // Sending one and only STRATEGY_REQ message to Services topic, thus sequenceNum is hardcoded "0"
        val strategyRequestMessage = RequestStrategyServicesTopicMessage(messageTraits._1, STRATEGY_STOP_REQUEST_MESSAGE_TYPE, msg.asInstanceOf[RequestEngineFacadeTopicMessage].client, msg.asInstanceOf[RequestEngineFacadeTopicMessage].payload, messageTraits._2, "0", serviceId)
        log.debug("******* EngineExecutorActor publishing STRATEGY_STOP_REQUEST to KafkaServicesTopicProducerActor: {}", strategyRequestMessage)
    
        // Publishing message to Services Topic
        val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
        kafkaServicesTopicProducerActor ! strategyRequestMessage
   }
  
  
  private def getStrategyFeed(msg: TttsEngineMessage) = {
        // Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
        val messageTraits = Utils.generateMessageTraits
        // Sending one and only STRATEGY_REQ message to Services topic, thus sequenceNum is hardcoded "0"
        val strategyRequestMessage = RequestStrategyServicesTopicMessage(messageTraits._1, STRATEGY_REQUEST_MESSAGE_TYPE, msg.asInstanceOf[RequestEngineFacadeTopicMessage].client, msg.asInstanceOf[RequestEngineFacadeTopicMessage].payload, messageTraits._2 , "0", serviceId)
    
        log.debug("EngineExecutorActor publishing STRATEGY_REQUEST to KafkaServicesTopicProducerActor: {}", strategyRequestMessage)
        // Publishing message to Services Topic
        val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
        kafkaServicesTopicProducerActor ! strategyRequestMessage
  }
  
  
  override def postStop() = {
  }
    
}
