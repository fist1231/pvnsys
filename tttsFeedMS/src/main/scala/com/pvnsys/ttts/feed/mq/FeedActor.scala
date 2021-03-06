package com.pvnsys.ttts.feed.mq

import akka.actor.{ActorLogging, OneForOneStrategy, AllForOneStrategy}
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.{TttsFeedMessage, FacadeTopicMessage, ServicesTopicMessage}
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.stream.actor.ActorProducer
import akka.stream.actor.ActorProducer._

object FeedActor {
  sealed trait FeedMessage
  case object StopMessage extends FeedMessage

}

class FeedActor extends ActorProducer[TttsFeedMessage] with ActorLogging {
  import FeedActor._

    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("FeedActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
	override def receive = {
		case msg: FacadeTopicMessage => 
			  log.debug(s"FeedActor, Gettin FacadeTopicMessage: {} - {}", msg.client, msg.msgType)
//			  throw new CustomException("WTFWTFWTFWTF????????????")
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		
		case msg: ServicesTopicMessage => 
			  log.debug(s"FeedActor, Gettin ServicesTopicMessage: {} - {}", msg.client, msg.msgType)
//			  throw new CustomException("WTFWTFWTFWTF????????????")
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		case StopMessage => {
			log.debug("FeedActor StopMessage")
		}
	    case Request(elements) =>
	      // nothing to do - we're waiting for the messages to come from RabbitMQ
			log.debug("FeedActor Request received")
	    case Cancel =>
		  log.debug("FeedActor Cancel request received")
	      context.stop(self)
		case _ => log.error("FeedActor Received unknown message")
	}
  
}
