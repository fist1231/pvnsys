package com.pvnsys.ttts.feed

import akka.actor.{ActorLogging, OneForOneStrategy, AllForOneStrategy}
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.FacadeTopicMessage
import akka.actor.SupervisorStrategy.{Restart, Stop}
//import scala.collection._
//import org.java_websocket.WebSocket
//import scala.concurrent.duration._
//import scala.concurrent.duration.TimeUnit
//import scala.concurrent.ExecutionContext
//import com.pvnsys.ttts.feed.mq.KafkaProducerActor
//import com.pvnsys.ttts.feed.mq.KafkaConsumerActor
//import java.net.InetSocketAddress
//import akka.dispatch.Foreach
import akka.stream.actor.ActorProducer
import akka.stream.actor.ActorProducer._


object FeedActor {
  sealed trait FeedMessage
  case object StopMessage extends FeedMessage

}

//case class CustomException(smth:String)  extends Exception



class FeedActor extends ActorProducer[FacadeTopicMessage] with ActorLogging {
  import FeedActor._

    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("@@@@@@@@@@@@@@@ FeedActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
  
	override def receive = {
		case msg: FacadeTopicMessage => 
			  log.debug(s"xoxoxoxoxoxoxo FeedActor, Gettin message: {} - {}", msg.client, msg.msgType)
//			  throw new CustomException("WTFWTFWTFWTF????????????")
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		
		case StopMessage => {
			log.debug("^^^^^ FeedActor StopMessage")
		}
	    case Request(elements) =>
	      // nothing to do - we're waiting for the messages to come from RabbitMQ
			log.debug("^^^^^ FeedActor Request received")
	    case Cancel =>
		  log.debug("^^^^^ FeedActor Cancel")
	      context.stop(self)
		case _ => log.error("^^^^^ FeedActor Received unknown message")
	}
  
}
