package com.pvnsys.ttts.strategy.flows.pub

import akka.actor.{ActorRef, ActorRefFactory, OneForOneStrategy, AllForOneStrategy, Props}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import org.reactivestreams.Publisher

object ServicesPublisherActor {
  import TttsStrategyMessages._
//  def make(factory: ActorRefFactory): ActorRef = factory.actorOf(Props(new ServicesPublisherActor))
//  def apply(factory: ActorRefFactory): Publisher[TttsStrategyMessage] = ActorPublisher[TttsStrategyMessage](make(factory))
  def make(factory: ActorRefFactory): ActorRef = factory.actorOf(Props(new ServicesPublisherActor))
  def apply(factory: ActorRefFactory): ActorRef = make(factory)
}

// This class processes messages from KafkaServicesTopicConsumerActor
private class ServicesPublisherActor extends PublisherActor {
  import PublisherActor._
  import TttsStrategyMessages._

    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("ServicesPublisherActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
  
	override def receive = {

//		case msg: RequestStrategyFacadeTopicMessage => 
//			  log.debug(s"ServicesPublisherActor, Gettin RequestStrategyFacadeTopicMessage: {} - {}", msg.client, msg.msgType)
//		      if (isActive && totalDemand > 0) {
//		        onNext(msg)
//		      } else {
//		        //requeue the message
//		        //message ordering might not be preserved
//		      }
		case msg: RequestStrategyServicesTopicMessage => 
			  log.debug(s"ServicesPublisherActor, Gettin RequestStrategyServicesTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		case msg: ResponseFeedServicesTopicMessage => 
			  log.debug(s"ServicesPublisherActor, Gettin ResponseFeedServicesTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
			  
		case msg: ResponseFeedFacadeTopicMessage => 
			  log.debug(s"ServicesPublisherActor, Gettin ResponseFeedFacadeTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
	
	
		case StopMessage => {
			log.debug("ServicesPublisherActor StopMessage")
		}
	    case Request(elements) =>
	      // nothing to do - we're waiting for the messages to come from Kafka
			log.debug("ServicesPublisherActor Request received")
	    case Cancel =>
		  log.debug("ServicesPublisherActor Cancel request received")
	      context.stop(self)
		case _ => log.error("ServicesPublisherActor Received unknown message")
	}
  
}
