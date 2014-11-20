package com.pvnsys.ttts.strategy.flows.pub

import akka.actor.{ActorLogging, OneForOneStrategy, AllForOneStrategy}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.TttsStrategyMessage
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import com.pvnsys.ttts.strategy.Configuration
import java.util.Properties
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import spray.json.DefaultJsonProtocol
import spray.json.pimpString
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.mutable
import scala.collection.mutable.Map
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}

object PublisherActor {
  sealed trait StrategyMessage
  case object StopMessage extends StrategyMessage

}

//object PublisherActorJsonProtocol extends DefaultJsonProtocol {
//  import TttsStrategyMessages._
//  implicit val strategyPayloadFormat = jsonFormat10(StrategyPayload)
//  implicit val requestStrategyFacadeTopicMessageFormat = jsonFormat6(RequestStrategyFacadeTopicMessage)
//
//  implicit val feedPayloadFormat = jsonFormat10(FeedPayload)
//  implicit val responseFeedServicesTopicMessageFormat = jsonFormat7(ResponseFeedServicesTopicMessage)
//  implicit val requestStrategyServicesTopicMessageFormat = jsonFormat7(RequestStrategyServicesTopicMessage)
//  implicit val responseFeedFacadeTopicMessageFormat = jsonFormat7(ResponseFeedFacadeTopicMessage)
//}

abstract class PublisherActor extends ActorPublisher[TttsStrategyMessage] with ActorLogging {
  import PublisherActor._
  import TttsStrategyMessages._
//  import PublisherActorJsonProtocol._

    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("PublisherActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
  
//	override def receive = {
//
//		case msg: RequestStrategyFacadeTopicMessage => 
//			  log.debug(s"PublisherActor, Gettin RequestStrategyFacadeTopicMessage: {} - {}", msg.client, msg.msgType)
//		      if (isActive && totalDemand > 0) {
//		        onNext(msg)
//		      } else {
//		        //requeue the message
//		        //message ordering might not be preserved
//		      }
//		case msg: RequestStrategyServicesTopicMessage => 
//			  log.debug(s"PublisherActor, Gettin RequestStrategyServicesTopicMessage: {} - {}", msg.client, msg.msgType)
//		      if (isActive && totalDemand > 0) {
//		        onNext(msg)
//		      } else {
//		        //requeue the message
//		        //message ordering might not be preserved
//		      }
//		case msg: ResponseFeedServicesTopicMessage => 
//			  log.debug(s"PublisherActor, Gettin ResponseFeedServicesTopicMessage: {} - {}", msg.client, msg.msgType)
//		      if (isActive && totalDemand > 0) {
//		        onNext(msg)
//		      } else {
//		        //requeue the message
//		        //message ordering might not be preserved
//		      }
//			  
//		case msg: ResponseFeedFacadeTopicMessage => 
//			  log.debug(s"PublisherActor, Gettin ResponseFeedFacadeTopicMessage: {} - {}", msg.client, msg.msgType)
//		      if (isActive && totalDemand > 0) {
//		        onNext(msg)
//		      } else {
//		        //requeue the message
//		        //message ordering might not be preserved
//		      }
//	
//	
//		case StopMessage => {
//			log.debug("PublisherActor StopMessage")
//		}
//	    case Request(elements) =>
//	      // nothing to do - we're waiting for the messages to come from Kafka
//			log.debug("PublisherActor Request received")
//	    case Cancel =>
//		  log.debug("PublisherActor Cancel request received")
//	      context.stop(self)
//		case _ => log.error("PublisherActor Received unknown message")
//	}
  
}
