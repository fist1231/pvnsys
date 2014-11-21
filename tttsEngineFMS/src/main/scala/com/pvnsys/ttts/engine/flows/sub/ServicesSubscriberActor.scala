package com.pvnsys.ttts.engine.flow.sub

import akka.actor.{ActorRef, ActorRefFactory, OneForOneStrategy, AllForOneStrategy, Props}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.ActorRef
import com.pvnsys.ttts.engine.util.Utils
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy}
import akka.stream.actor.ActorSubscriberMessage.OnNext
import org.reactivestreams.Subscriber
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.OneByOneRequestStrategy


object ServicesSubscriberActor {
  import TttsEngineMessages._
//  def make(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = factory.actorOf(Props(new ServicesSubscriberActor(serviceId, kafkaServicesPublisher)))
//  def apply(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): Subscriber[TttsEngineMessage] = ActorSubscriber[TttsEngineMessage](make(factory, serviceId, kafkaServicesPublisher))
  def make(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = factory.actorOf(Props(new ServicesSubscriberActor(serviceId, kafkaServicesPublisher)))
  def apply(factory: ActorRefFactory, serviceId: String, kafkaServicesPublisher: ActorRef): ActorRef = make(factory, serviceId, kafkaServicesPublisher)
}

private class ServicesSubscriberActor(serviceId: String, kafkaServicesPublisher: ActorRef) extends SubscriberActor {
  import TttsEngineMessages._
  
  private var inFlight = 0
  
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("ServicesSubscriberActor Unexpected failure: {}", e.getMessage)
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
			case msg: RequestEngineFacadeTopicMessage => 
				log.debug("ServicesSubscriberActor, Gettin RequestEngineFacadeTopicMessage: {}", msg)
				  val strategyRequestMessage = msg.msgType match {
					    case ENGINE_REQUEST_MESSAGE_TYPE => RequestStrategyServicesTopicMessage(messageTraits._1, STRATEGY_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2 , "0", serviceId)
					    case ENGINE_STOP_REQUEST_MESSAGE_TYPE => RequestStrategyServicesTopicMessage(messageTraits._1, STRATEGY_STOP_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2, "0", serviceId)
			      }
				  kafkaServicesPublisher ! strategyRequestMessage
				  
			case msg: RequestEngineServicesTopicMessage => 
				  log.debug("ServicesSubscriberActor, Gettin RequestEngineServicesTopicMessage: {}", msg)
				  val strategyRequestMessage = msg.msgType match {
					    case ENGINE_REQUEST_MESSAGE_TYPE => RequestStrategyServicesTopicMessage(messageTraits._1, STRATEGY_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2 , "0", serviceId)
					    case ENGINE_STOP_REQUEST_MESSAGE_TYPE => RequestStrategyServicesTopicMessage(messageTraits._1, STRATEGY_STOP_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2, "0", serviceId)
			      }
				  kafkaServicesPublisher ! strategyRequestMessage
				  
			case msg: ResponseEngineServicesTopicMessage => 
				  log.debug("ServicesSubscriberActor, Gettin ResponseEngineServicesTopicMessage: {}", msg)
				  kafkaServicesPublisher ! msg
//				  inFlight += 1
			case _ => // Do nothing
		   } 
           
		   log.debug("############################### ServicesSubscriberActor OnNext, inFlight = {}", inFlight)
			  
		}	  

//		case ProducerConfirmationMessage => {
////		  inFlight -= 1	
//		  log.debug("############################### ServicesSubscriberActor ProducerConfirmationMessage, inFlight = {}", inFlight)
//		}
		
		case _ => log.error("ServicesSubscriberActor Received unknown message")
	}
  
}
