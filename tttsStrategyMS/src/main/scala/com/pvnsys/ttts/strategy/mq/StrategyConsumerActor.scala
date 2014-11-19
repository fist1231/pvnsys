package com.pvnsys.ttts.strategy.mq

import akka.actor.{ActorLogging, OneForOneStrategy, AllForOneStrategy, Props}
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
import akka.stream.actor.ActorConsumer
import akka.actor.ActorRef
import com.pvnsys.ttts.strategy.util.Utils



object StrategyConsumerActor {
  def props(publisher: ActorRef, serviceId: String) = Props(new StrategyConsumerActor(publisher, serviceId))
  sealed trait StrategyMessage
  case object StopMessage extends StrategyMessage

}

object StrategyConsumerActorJsonProtocol extends DefaultJsonProtocol {
  import TttsStrategyMessages._
  implicit val strategyPayloadFormat = jsonFormat10(StrategyPayload)
  implicit val requestStrategyFacadeTopicMessageFormat = jsonFormat6(RequestStrategyFacadeTopicMessage)

  implicit val feedPayloadFormat = jsonFormat10(FeedPayload)
  implicit val responseFeedServicesTopicMessageFormat = jsonFormat7(ResponseFeedServicesTopicMessage)
  implicit val requestStrategyServicesTopicMessageFormat = jsonFormat7(RequestStrategyServicesTopicMessage)
  implicit val responseFeedFacadeTopicMessageFormat = jsonFormat7(ResponseFeedFacadeTopicMessage)
}

class StrategyConsumerActor(publisher: ActorRef, serviceId: String) extends ActorConsumer with ActorLogging {
  import StrategyConsumerActor._
  import TttsStrategyMessages._
  import StrategyConsumerActorJsonProtocol._
  import ActorConsumer._

  
  private var inFlight = 0
  
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("StrategyConsumerActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
//  override protected def requestStrategy = new WatermarkRequestStrategy(1, 1) 
  
  override protected def requestStrategy = new MaxInFlightRequestStrategy(1) {
	  override def batchSize = 1
	  override def inFlightInternally = inFlight
  }
 
 
  
	override def receive = {

		case OnNext(mesg: TttsStrategyMessage) => {
           val messageTraits = Utils.generateMessageTraits
		  
		   mesg match {
			case msg: RequestStrategyFacadeTopicMessage => 
				log.debug("StrategyConsumerActor, Gettin RequestStrategyFacadeTopicMessage: {}", msg)
				  val feedRequestMessage = msg.msgType match {
					    case STRATEGY_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2 , "0", serviceId)
					    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_STOP_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2, "0", serviceId)
			      }
				  publisher ! feedRequestMessage
				  
			case msg: RequestStrategyServicesTopicMessage => 
				  log.debug("StrategyConsumerActor, Gettin RequestStrategyServicesTopicMessage: {}", msg)
				  val feedRequestMessage = msg.msgType match {
					    case STRATEGY_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2 , "0", serviceId)
					    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => RequestFeedServicesTopicMessage(messageTraits._1, FEED_STOP_REQUEST_MESSAGE_TYPE, msg.client, None, messageTraits._2, "0", serviceId)
			      }
				  publisher ! feedRequestMessage
				  
			case msg: ResponseFeedServicesTopicMessage => 
				  log.debug("StrategyConsumerActor, Gettin ResponseFeedServicesTopicMessage: {}", msg)
				  publisher ! msg
			case msg: ResponseFeedFacadeTopicMessage => 
				  log.debug("StrategyConsumerActor, Gettin ResponseFeedFacadeTopicMessage: {}", msg)
				  publisher ! msg
			case msg: ResponseStrategyFacadeTopicMessage => 
				  log.debug("StrategyConsumerActor, Gettin ResponseStrategyFacadeTopicMessage: {}", msg)
				  publisher ! msg
				  inFlight += 1
			case msg: ResponseStrategyServicesTopicMessage => 
				  log.debug("StrategyConsumerActor, Gettin ResponseStrategyServicesTopicMessage: {}", msg)
				  publisher ! msg
			case _ => publisher ! mesg
		   } 
           
		   log.debug("############################### StrategyConsumerActor, inFlight = {}", inFlight)
			  
		}	  

		case ProducerConfirmationMessage => {
		  inFlight -= 1	
		  log.debug("############################### StrategyConsumerActor, inFlight = {}", inFlight)
		}
		
//		case StopMessage => {
//			log.debug("StrategyActor StopMessage")
//		}
//	    case Request(elements) =>
//	      // nothing to do - we're waiting for the messages to come from Kafka
//			log.debug("StrategyActor Request received")
//	    case Cancel =>
//		  log.debug("StrategyActor Cancel request received")
//	      context.stop(self)
		case _ => log.error("StrategyConsumerActor Received unknown message")
	}
  
}
