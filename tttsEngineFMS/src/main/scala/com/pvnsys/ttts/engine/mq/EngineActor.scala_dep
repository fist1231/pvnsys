package com.pvnsys.ttts.engine.mq

import akka.actor.{ActorLogging, OneForOneStrategy, AllForOneStrategy}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages.TttsEngineMessage
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.stream.actor.ActorProducer
import akka.stream.actor.ActorProducer._


object EngineActor {
  sealed trait EngineMessage
  case object StopMessage extends EngineMessage
}


class EngineActor extends ActorProducer[TttsEngineMessage] with ActorLogging {
  import EngineActor._
  import TttsEngineMessages._

    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("EngineActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
  
	override def receive = {

		case msg: RequestEngineFacadeTopicMessage => 
			  log.debug(s"EngineActor, Gettin RequestEngineServicesTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		case msg: RequestEngineServicesTopicMessage => 
			  log.debug(s"EngineActor, Gettin RequestEngineServicesTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		case msg: ResponseStrategyServicesTopicMessage => 
			  log.debug(s"EngineActor, Gettin ResponseFeedServicesTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		case msg: ResponseStrategyFacadeTopicMessage => 
			  log.debug(s"EngineActor, Gettin ResponseStrategyFacadeTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
	
	
		case StopMessage => {
			log.debug("EngineActor StopMessage")
		}
	    case Request(elements) =>
	      // nothing to do - we're waiting for the messages to come from Kafka
			log.debug("EngineActor Request received")
	    case Cancel =>
		  log.debug("EngineActor Cancel request received")
	      context.stop(self)
		case _ => log.error("EngineActor Received unknown message")
	}
  
}
