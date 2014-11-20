package com.pvnsys.ttts.strategy.flow.sub

import akka.actor.{ActorRef, ActorRefFactory, OneForOneStrategy, AllForOneStrategy, Props}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.ActorRef
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy}
import akka.stream.actor.ActorSubscriberMessage.OnNext
import org.reactivestreams.Subscriber


object FacadeSubscriberActor {
  
  import TttsStrategyMessages._
  
//  def props(serviceId: String, kafkaFacadePublisher: ActorRef) = Props(new FacadeSubscriberActor(serviceId, kafkaFacadePublisher))
  
  def make(factory: ActorRefFactory, serviceId: String, kafkaFacadePublisher: ActorRef): ActorRef = factory.actorOf(Props(new FacadeSubscriberActor(serviceId, kafkaFacadePublisher)))
  def apply(factory: ActorRefFactory, serviceId: String, kafkaFacadePublisher: ActorRef): Subscriber[TttsStrategyMessage] = ActorSubscriber[TttsStrategyMessage](make(factory, serviceId, kafkaFacadePublisher))
  
//  sealed trait StrategyMessage
//  case object StopMessage extends StrategyMessage

}


private class FacadeSubscriberActor(serviceId: String, kafkaFacadePublisher: ActorRef) extends SubscriberActor {
  import TttsStrategyMessages._
  
  private var inFlight = 0
  
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("FacadeSubscriberActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
//  override protected def requestStrategy = new WatermarkRequestStrategy(1, 1) 
  
  override protected def requestStrategy = new MaxInFlightRequestStrategy(1) {
	  override def batchSize = 1
	  override def inFlightInternally = inFlight
  }
 
 
  
	override def receive = {

		case OnNext(mesg: TttsStrategyMessage) => {

		  mesg match {
			case msg: ResponseStrategyFacadeTopicMessage => 
				  log.debug("FacadeSubscriberActor, Gettin ResponseStrategyFacadeTopicMessage: {}", msg)
				  kafkaFacadePublisher ! msg
				  inFlight += 1

			case _ => // Do nothing
		   } 
           
		   log.debug("############################### FacadeSubscriberActor, inFlight = {}", inFlight)
			  
		}	  

		case ProducerConfirmationMessage => {
		  inFlight -= 1	
		  log.debug("############################### FacadeSubscriberActor, inFlight = {}", inFlight)
		}
		
		case _ => log.error("FacadeSubscriberActor Received unknown message")
	}
  
}
