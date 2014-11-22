package com.pvnsys.ttts.engine.flow.sub

import akka.actor.{ActorRef, ActorRefFactory, OneForOneStrategy, AllForOneStrategy, Props}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.ActorRef
import com.pvnsys.ttts.engine.util.Utils
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy}
import akka.stream.actor.ActorSubscriberMessage.{OnNext, OnComplete, OnError}
import org.reactivestreams.Subscriber
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.OneByOneRequestStrategy


object ServicesEngineResponseSubscriberActor {
  import TttsEngineMessages._
//  def make(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = factory.actorOf(Props(new ServicesSubscriberActor(serviceId, kafkaServicesPublisher)))
//  def apply(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): Subscriber[TttsEngineMessage] = ActorSubscriber[TttsEngineMessage](make(factory, serviceId, kafkaServicesPublisher))
  def make(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = factory.actorOf(Props(new ServicesSubscriberActor(serviceId, kafkaServicesPublisher)))
  def apply(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = make(factory, serviceId, kafkaServicesPublisher)
}

private class ServicesEngineResponseSubscriberActor(serviceId: String, kafkaServicesPublisher: ActorRef) extends SubscriberActor {
  import TttsEngineMessages._
  
  private var inFlight = 0
  
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("ServicesEngineResponseSubscriberActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
//  override protected def requestStrategy = new WatermarkRequestStrategy(1, 1) 
  
//  override protected def requestStrategy = new MaxInFlightRequestStrategy(1) {
////	  override def batchSize = 1
//	  override def inFlightInternally = inFlight
//  }
  override protected def requestStrategy = OneByOneRequestStrategy
 
 
	override def receive = {

		case OnNext(mesg: TttsEngineMessage) => {
           val messageTraits = Utils.generateMessageTraits
		  
		   mesg match {
			case msg: ResponseEngineServicesTopicMessage => 
				  log.debug("ServicesEngineResponseSubscriberActor, Gettin ResponseEngineServicesTopicMessage: {}", msg)
				  kafkaServicesPublisher ! msg
			case _ => // Do nothing
		   } 
           
		   log.debug("############################### ServicesEngineResponseSubscriberActor OnNext, inFlight = {}", inFlight)
			  
		}	  

		case OnComplete => {
				  log.debug("******* ServicesEngineResponseSubscriberActor OnComplete, Gettin ResponseStrategyFacadeTopicMessage")
		  
		}
		case OnError(cause: Throwable) => {
				  log.debug("******* ServicesEngineResponseSubscriberActor OnError, Gettin ResponseStrategyFacadeTopicMessage: {}", cause.getMessage())
				  cause.printStackTrace()
		  
		}
		
		case z => log.error("ServicesEngineResponseSubscriberActor Received unknown message: {}", z)
	}
  
}
