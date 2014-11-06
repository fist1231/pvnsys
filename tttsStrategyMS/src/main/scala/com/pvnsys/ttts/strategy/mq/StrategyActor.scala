package com.pvnsys.ttts.strategy.mq

import akka.actor.{ActorLogging, OneForOneStrategy, AllForOneStrategy}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.{TttsStrategyMessage, FacadeTopicMessage, ServicesTopicMessage}
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.stream.actor.ActorProducer
import akka.stream.actor.ActorProducer._


object StrategyActor {
  sealed trait StrategyMessage
  case object StopMessage extends StrategyMessage

}


class StrategyActor extends ActorProducer[TttsStrategyMessage] with ActorLogging {
  import StrategyActor._

    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("FeedActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
  
	override def receive = {
		case msg: FacadeTopicMessage => 
			  log.debug(s"StrategyActor, Gettin FacadeTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		
		case msg: ServicesTopicMessage => 
			  log.debug(s"StrategyActor, Gettin ServicesTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		case StopMessage => {
			log.debug("StrategyActor StopMessage")
		}
	    case Request(elements) =>
	      // nothing to do - we're waiting for the messages to come from Kafka
			log.debug("StrategyActor Request received")
	    case Cancel =>
		  log.debug("StrategyActor Cancel request received")
	      context.stop(self)
		case _ => log.error("StrategyActor Received unknown message")
	}
  
}
