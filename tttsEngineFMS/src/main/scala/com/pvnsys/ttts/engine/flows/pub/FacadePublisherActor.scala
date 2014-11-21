package com.pvnsys.ttts.engine.flows.pub

import akka.actor.{ActorRef, ActorRefFactory, OneForOneStrategy, AllForOneStrategy, Props}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import org.reactivestreams.Publisher

object FacadePublisherActor {
  import TttsEngineMessages._
//  def make(factory: ActorRefFactory): ActorRef = factory.actorOf(Props(new FacadePublisherActor))
//  def apply(factory: ActorRefFactory): Publisher[TttsEngineMessage] = ActorPublisher[TttsEngineMessage](make(factory))
  def make(factory: ActorRefFactory): ActorRef = factory.actorOf(Props(new FacadePublisherActor))
  def apply(factory: ActorRefFactory): ActorRef = make(factory)
}

// This class processes messages from KafkaFacadeTopicConsumerActor
private class FacadePublisherActor extends PublisherActor {
  import PublisherActor._
  import TttsEngineMessages._

    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("FacadePublisherActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
  
	override def receive = {

		case msg: RequestEngineFacadeTopicMessage => 
			  log.debug(s"FacadePublisherActor, Gettin RequestEngineFacadeTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		case StopMessage => {
			log.debug("FacadePublisherActor StopMessage")
		}
	    case Request(elements) =>
	      // nothing to do - we're waiting for the messages to come from Kafka
			log.debug("FacadePublisherActor Request received")
	    case Cancel =>
		  log.debug("FacadePublisherActor Cancel request received")
	      context.stop(self)
		case _ => log.error("FacadePublisherActor Received unknown message")
	}
  
}
