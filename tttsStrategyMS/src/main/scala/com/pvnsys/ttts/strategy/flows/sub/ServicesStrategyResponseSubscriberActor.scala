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


object ServicesStrategyResponseSubscriberActor {
  import TttsStrategyMessages._
//  def make(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = factory.actorOf(Props(new ServicesSubscriberActor(serviceId, kafkaServicesPublisher)))
//  def apply(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): Subscriber[TttsStrategyMessage] = ActorSubscriber[TttsStrategyMessage](make(factory, serviceId, kafkaServicesPublisher))
  def make(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = factory.actorOf(Props(new ServicesSubscriberActor(serviceId, kafkaServicesPublisher)))
  def apply(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = make(factory, serviceId, kafkaServicesPublisher)
}

private class ServicesStrategyResponseSubscriberActor(serviceId: String, kafkaServicesPublisher: ActorRef) extends SubscriberActor {
  import TttsStrategyMessages._
  
  private var inFlight = 0
  
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("ServicesStrategyResponseSubscriberActor Unexpected failure: {}", e.getMessage)
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
			case msg: ResponseStrategyServicesTopicMessage => 
				  log.debug("ServicesStrategyResponseSubscriberActor, Gettin ResponseStrategyServicesTopicMessage: {}", msg)
				  kafkaServicesPublisher ! msg
//				  inFlight += 1
			case _ => // Do nothing
		   } 
           
		   log.debug("############################### ServicesStrategyResponseSubscriberActor OnNext, inFlight = {}", inFlight)
			  
		}	  
		case OnComplete => {
				  log.debug("******* ServicesStrategyResponseSubscriberActor OnComplete, Gettin ResponseStrategyFacadeTopicMessage")
		  
		}
		case OnError(cause: Throwable) => {
				  log.debug("******* ServicesStrategyResponseSubscriberActor OnError, Gettin ResponseStrategyFacadeTopicMessage: {}", cause.getMessage())
				  cause.printStackTrace()
		  
		}

		case ProducerConfirmationMessage => {
//		  inFlight -= 1	
		  log.debug("############################### ServicesStrategyResponseSubscriberActor ProducerConfirmationMessage, inFlight = {}", inFlight)
		}
		
		case z => log.error("ServicesStrategyResponseSubscriberActor Received unknown message: {}", z)
	}
  
}
