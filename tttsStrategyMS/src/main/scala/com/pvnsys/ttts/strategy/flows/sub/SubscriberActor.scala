package com.pvnsys.ttts.strategy.flow.sub

import akka.actor.{ActorLogging, OneForOneStrategy, AllForOneStrategy, Props}
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
import akka.actor.ActorRef
import com.pvnsys.ttts.strategy.util.Utils
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy}
import akka.stream.actor.ActorSubscriberMessage.OnNext


object SubscriberActor {
//  def props(publisher: ActorRef, serviceId: String) = Props(new SubscriberActor(publisher, serviceId))
  sealed trait StrategyMessage
  case object StopMessage extends StrategyMessage

}

//object SubscriberActorJsonProtocol extends DefaultJsonProtocol {
//  import TttsStrategyMessages._
//  implicit val strategyPayloadFormat = jsonFormat10(StrategyPayload)
//  implicit val requestStrategyFacadeTopicMessageFormat = jsonFormat6(RequestStrategyFacadeTopicMessage)
//
//  implicit val feedPayloadFormat = jsonFormat10(FeedPayload)
//  implicit val responseFeedServicesTopicMessageFormat = jsonFormat7(ResponseFeedServicesTopicMessage)
//  implicit val requestStrategyServicesTopicMessageFormat = jsonFormat7(RequestStrategyServicesTopicMessage)
//  implicit val responseFeedFacadeTopicMessageFormat = jsonFormat7(ResponseFeedFacadeTopicMessage)
//}

abstract class SubscriberActor extends ActorSubscriber with ActorLogging {
  import SubscriberActor._
  import TttsStrategyMessages._
//  import SubscriberActorJsonProtocol._

  
  private var inFlight = 0
  
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("SubscriberActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
//  override protected def requestStrategy = new WatermarkRequestStrategy(1, 1) 
  
//  override protected def requestStrategy = new MaxInFlightRequestStrategy(1) {
//	  override def batchSize = 1
//	  override def inFlightInternally = inFlight
//  }
 
 
  
//	override def receive = {
//
//		case OnNext(mesg: TttsStrategyMessage) => {
//           val messageTraits = Utils.generateMessageTraits
//		  
//		   mesg match {
//			case msg: RequestStrategyFacadeTopicMessage => 
//				log.debug("SubscriberActor, Gettin RequestStrategyFacadeTopicMessage: {}", msg)
//				  val feedRequestMessage = msg.msgType match {
//					    case STRATEGY_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2 , "0", serviceId)
//					    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_STOP_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2, "0", serviceId)
//			      }
//				  publisher ! feedRequestMessage
//				  
//			case msg: RequestStrategyServicesTopicMessage => 
//				  log.debug("SubscriberActor, Gettin RequestStrategyServicesTopicMessage: {}", msg)
//				  val feedRequestMessage = msg.msgType match {
//					    case STRATEGY_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2 , "0", serviceId)
//					    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_STOP_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2, "0", serviceId)
//			      }
//				  publisher ! feedRequestMessage
//				  
//			case msg: ResponseFeedServicesTopicMessage => 
//				  log.debug("SubscriberActor, Gettin ResponseFeedServicesTopicMessage: {}", msg)
//				  publisher ! msg
//			case msg: ResponseFeedFacadeTopicMessage => 
//				  log.debug("SubscriberActor, Gettin ResponseFeedFacadeTopicMessage: {}", msg)
//				  publisher ! msg
//			case msg: ResponseStrategyFacadeTopicMessage => 
//				  log.debug("SubscriberActor, Gettin ResponseStrategyFacadeTopicMessage: {}", msg)
//				  publisher ! msg
//				  inFlight += 1
//			case msg: ResponseStrategyServicesTopicMessage => 
//				  log.debug("SubscriberActor, Gettin ResponseStrategyServicesTopicMessage: {}", msg)
//				  publisher ! msg
//			case _ => publisher ! mesg
//		   } 
//           
//		   log.debug("############################### SubscriberActor, inFlight = {}", inFlight)
//			  
//		}	  
//
//		case ProducerConfirmationMessage => {
//		  inFlight -= 1	
//		  log.debug("############################### SubscriberActor, inFlight = {}", inFlight)
//		}
//		
//		case _ => log.error("SubscriberActor Received unknown message")
//	}
  
}
