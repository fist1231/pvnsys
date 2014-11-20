package com.pvnsys.ttts.strategy.mq

import akka.actor.{ActorLogging, OneForOneStrategy, AllForOneStrategy}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.TttsStrategyMessage
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.stream.actor.ActorProducer
import akka.stream.actor.ActorProducer._
import com.pvnsys.ttts.strategy.Configuration
import java.util.Properties
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import spray.json.DefaultJsonProtocol
import spray.json.pimpString
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.mutable
import scala.collection.mutable.Map


object StrategyProducerActor {
  sealed trait StrategyMessage
  case object StopMessage extends StrategyMessage

}

object StrategyProducerActorJsonProtocol extends DefaultJsonProtocol {
  import TttsStrategyMessages._
  implicit val strategyPayloadFormat = jsonFormat10(StrategyPayload)
  implicit val requestStrategyFacadeTopicMessageFormat = jsonFormat6(RequestStrategyFacadeTopicMessage)

  implicit val feedPayloadFormat = jsonFormat10(FeedPayload)
  implicit val responseFeedServicesTopicMessageFormat = jsonFormat7(ResponseFeedServicesTopicMessage)
  implicit val requestStrategyServicesTopicMessageFormat = jsonFormat7(RequestStrategyServicesTopicMessage)
  implicit val responseFeedFacadeTopicMessageFormat = jsonFormat7(ResponseFeedFacadeTopicMessage)
}

class StrategyProducerActor extends ActorProducer[TttsStrategyMessage] with ActorLogging {
  import StrategyProducerActor._
  import TttsStrategyMessages._
  import StrategyProducerActorJsonProtocol._

    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("FeedActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
  
	override def receive = {

		case msg: RequestStrategyFacadeTopicMessage => 
			  log.debug(s"StrategyActor, Gettin RequestStrategyFacadeTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		case msg: RequestStrategyServicesTopicMessage => 
			  log.debug(s"StrategyActor, Gettin RequestStrategyServicesTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
		case msg: ResponseFeedServicesTopicMessage => 
			  log.debug(s"StrategyActor, Gettin ResponseFeedServicesTopicMessage: {} - {}", msg.client, msg.msgType)
		      if (isActive && totalDemand > 0) {
		        onNext(msg)
		      } else {
		        //requeue the message
		        //message ordering might not be preserved
		      }
			  
		case msg: ResponseFeedFacadeTopicMessage => 
			  log.debug(s"StrategyActor, Gettin ResponseFeedFacadeTopicMessage: {} - {}", msg.client, msg.msgType)
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
