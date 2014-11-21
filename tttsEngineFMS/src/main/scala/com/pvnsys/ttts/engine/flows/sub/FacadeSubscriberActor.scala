package com.pvnsys.ttts.engine.flow.sub

import akka.actor.{ActorRef, ActorRefFactory, OneForOneStrategy, AllForOneStrategy, Props}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.ActorRef
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy}
import akka.stream.actor.ActorSubscriberMessage.OnNext
import org.reactivestreams.Subscriber
import akka.stream.actor.OneByOneRequestStrategy


object FacadeSubscriberActor {
  import TttsEngineMessages._
  def make(factory: ActorRefFactory, serviceId: String, kafkaFacadePublisher: ActorRef): ActorRef = factory.actorOf(Props(new FacadeSubscriberActor(serviceId, kafkaFacadePublisher)))
  def apply(factory: ActorRefFactory, serviceId: String, kafkaFacadePublisher: ActorRef): ActorRef = make(factory, serviceId, kafkaFacadePublisher)
}


private class FacadeSubscriberActor(serviceId: String, kafkaFacadePublisher: ActorRef) extends SubscriberActor {
  import TttsEngineMessages._
  
//  private var inFlight = 0
  
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("FacadeSubscriberActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
//  override protected def requestStrategy = new WatermarkRequestStrategy(1, 1) 
  
//  override protected def requestStrategy = new MaxInFlightRequestStrategy(2) {
//	  override def batchSize = 1
//	  override def inFlightInternally = inFlight
//  }
 
  override protected def requestStrategy = OneByOneRequestStrategy
 
  
	override def receive = {

		case OnNext(mesg: TttsEngineMessage) => {

		  mesg match {
			case msg: ResponseEngineFacadeTopicMessage => 
				  log.debug("FacadeSubscriberActor, Gettin ResponseEngineFacadeTopicMessage: {}", msg)
				  kafkaFacadePublisher ! msg
//				  inFlight += 1
			case _ => // Do nothing
		   } 
		  
		   log.debug("############################### FacadeSubscriberActor OnNext, msg = {}", mesg)
			  
		}	  

//		case ProducerConfirmationMessage => {
////		  inFlight -= 1	
//		  log.debug("############################### FacadeSubscriberActor ProducerConfirmationMessage")
//		}
		
		case _ => log.error("FacadeSubscriberActor Received unknown message")
	}
  
}
