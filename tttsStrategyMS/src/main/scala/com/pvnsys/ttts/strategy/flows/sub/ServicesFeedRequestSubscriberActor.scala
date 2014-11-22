package com.pvnsys.ttts.strategy.flow.sub

import akka.actor.{ActorRef, ActorRefFactory, OneForOneStrategy, AllForOneStrategy, Props}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.ActorRef
import com.pvnsys.ttts.strategy.util.Utils
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy}
import akka.stream.actor.ActorSubscriberMessage.{OnNext, OnComplete, OnError}
import org.reactivestreams.Subscriber
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.OneByOneRequestStrategy


object ServicesFeedRequestSubscriberActor {
  import TttsStrategyMessages._
//  def make(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = factory.actorOf(Props(new ServicesSubscriberActor(serviceId, kafkaServicesPublisher)))
//  def apply(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): Subscriber[TttsStrategyMessage] = ActorSubscriber[TttsStrategyMessage](make(factory, serviceId, kafkaServicesPublisher))
  def make(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = factory.actorOf(Props(new ServicesSubscriberActor(serviceId, kafkaServicesPublisher)))
  def apply(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = make(factory, serviceId, kafkaServicesPublisher)
}

private class ServicesFeedRequestSubscriberActor(serviceId: String, kafkaServicesPublisher: ActorRef) extends SubscriberActor {
  import TttsStrategyMessages._
  
  private var inFlight = 0
  
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("ServicesFeedRequestSubscriberActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
//  override protected def requestStrategy = new WatermarkRequestStrategy(1, 1) 
  
//  override protected def requestStrategy = new MaxInFlightRequestStrategy(1) {
////	  override def batchSize = 1
//	  override def inFlightInternally = inFlight
//  }
  override protected def requestStrategy = OneByOneRequestStrategy
 
 
	override def receive = {

		case OnNext(mesg: TttsStrategyMessage) => {
           val messageTraits = Utils.generateMessageTraits
		  
		   mesg match {
			case msg: RequestStrategyServicesTopicMessage => 
				  log.debug("ServicesFeedRequestSubscriberActor, Gettin RequestStrategyServicesTopicMessage: {}", msg)
				  val feedRequestMessage = msg.msgType match {
					    case STRATEGY_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2 , "0", serviceId)
					    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_STOP_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2, "0", serviceId)
			      }
				  kafkaServicesPublisher ! feedRequestMessage
			case _ => // Do nothing
		   } 
           
		   log.debug("############################### ServicesSubscriberActor OnNext, inFlight = {}", inFlight)
			  
		}	  
		case OnComplete => {
				  log.debug("******* ServicesFeedRequestSubscriberActor OnComplete, Gettin ResponseStrategyFacadeTopicMessage")
		  
		}
		case OnError(cause: Throwable) => {
				  log.debug("******* ServicesFeedRequestSubscriberActor OnError, Gettin ResponseStrategyFacadeTopicMessage: {}", cause.getMessage())
				  cause.printStackTrace()
		  
		}

		case ProducerConfirmationMessage => {
//		  inFlight -= 1	
		  log.debug("############################### ServicesFeedRequestSubscriberActor ProducerConfirmationMessage, inFlight = {}", inFlight)
		}
		
		case z => log.error("ServicesFeedRequestSubscriberActor Received unknown message: {}", z)
	}
  
}
