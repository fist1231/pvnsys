package com.pvnsys.ttts.strategy.flow.sub

import akka.actor.{ActorRef, ActorRefFactory, OneForOneStrategy, AllForOneStrategy, Props}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.ActorRef
import com.pvnsys.ttts.strategy.util.Utils
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy}
import akka.stream.actor.ActorSubscriberMessage.OnNext
import org.reactivestreams.Subscriber
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.OneByOneRequestStrategy


object ServicesSubscriberActor {
//  def props(serviceId: String, kafkaServicesPublisher: ActorRef) = Props(new ServicesSubscriberActor(serviceId, kafkaServicesPublisher))
//  sealed trait StrategyMessage
//  case object StopMessage extends StrategyMessage
  import TttsStrategyMessages._

  def make(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = factory.actorOf(Props(new ServicesSubscriberActor(serviceId, kafkaServicesPublisher)))
  def apply(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): Subscriber[TttsStrategyMessage] = ActorSubscriber[TttsStrategyMessage](make(factory, serviceId, kafkaServicesPublisher))
  
}

private class ServicesSubscriberActor(serviceId: String, kafkaServicesPublisher: ActorRef) extends SubscriberActor {
//  import ServicesSubscriberActor._
  import TttsStrategyMessages._
//  import FacadeSubscriberActorJsonProtocol._

  
  private var inFlight = 0
  
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("ServicesSubscriberActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
//  override protected def requestStrategy = new WatermarkRequestStrategy(1, 1) 
  
//  override protected def requestStrategy = new MaxInFlightRequestStrategy(1) {
//	  override def batchSize = 1
//	  override def inFlightInternally = inFlight
//  }
  override protected def requestStrategy = OneByOneRequestStrategy
 
 
	override def receive = {

		case OnNext(mesg: TttsStrategyMessage) => {
           val messageTraits = Utils.generateMessageTraits
		  
		   mesg match {
			case msg: RequestStrategyFacadeTopicMessage => 
				log.debug("ServicesSubscriberActor, Gettin RequestStrategyFacadeTopicMessage: {}", msg)
				  val feedRequestMessage = msg.msgType match {
					    case STRATEGY_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2 , "0", serviceId)
					    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_STOP_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2, "0", serviceId)
			      }
				  kafkaServicesPublisher ! feedRequestMessage
				  
			case msg: RequestStrategyServicesTopicMessage => 
				  log.debug("ServicesSubscriberActor, Gettin RequestStrategyServicesTopicMessage: {}", msg)
				  val feedRequestMessage = msg.msgType match {
					    case STRATEGY_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2 , "0", serviceId)
					    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_STOP_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2, "0", serviceId)
			      }
				  kafkaServicesPublisher ! feedRequestMessage
				  
			case msg: ResponseStrategyServicesTopicMessage => 
				  log.debug("ServicesSubscriberActor, Gettin ResponseStrategyServicesTopicMessage: {}", msg)
				  kafkaServicesPublisher ! msg
				  inFlight += 1
			case _ => // Do nothing
		   } 
           
		   log.debug("############################### ServicesSubscriberActor, inFlight = {}", inFlight)
			  
		}	  

		case ProducerConfirmationMessage => {
		  inFlight -= 1	
		  log.debug("############################### ServicesSubscriberActor, inFlight = {}", inFlight)
		}
		
		case _ => log.error("ServicesSubscriberActor Received unknown message")
	}
  
}
