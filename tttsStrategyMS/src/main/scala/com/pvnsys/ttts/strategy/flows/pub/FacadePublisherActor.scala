package com.pvnsys.ttts.strategy.flows.pub

import akka.actor.{ActorRef, ActorRefFactory, OneForOneStrategy, AllForOneStrategy, Props}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import org.reactivestreams.Publisher

object FacadePublisherActor {
  import TttsStrategyMessages._
//  def make(factory: ActorRefFactory): ActorRef = factory.actorOf(Props(new FacadePublisherActor))
//  def apply(factory: ActorRefFactory): Publisher[TttsStrategyMessage] = ActorPublisher[TttsStrategyMessage](make(factory))
  def make(factory: ActorRefFactory): ActorRef = factory.actorOf(Props(new FacadePublisherActor))
  def apply(factory: ActorRefFactory): ActorRef = make(factory)
}

// This class processes messages from KafkaFacadeTopicConsumerActor
private class FacadePublisherActor extends PublisherActor {
  import PublisherActor._
  import TttsStrategyMessages._

    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("FacadePublisherActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
  
	override def receive = {

		case msg: RequestStrategyFacadeTopicMessage => 
			  log.debug(s"FacadePublisherActor, Gettin RequestStrategyFacadeTopicMessage: {} - {}", msg.client, msg.msgType)
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
