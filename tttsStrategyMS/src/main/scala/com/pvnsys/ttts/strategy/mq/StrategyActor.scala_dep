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


object StrategyActor {
  sealed trait StrategyMessage
  case object StopMessage extends StrategyMessage

}

object StrategyActorJsonProtocol extends DefaultJsonProtocol {
  import TttsStrategyMessages._
  implicit val strategyPayloadFormat = jsonFormat10(StrategyPayload)
  implicit val requestStrategyFacadeTopicMessageFormat = jsonFormat6(RequestStrategyFacadeTopicMessage)

  implicit val feedPayloadFormat = jsonFormat10(FeedPayload)
  implicit val responseFeedServicesTopicMessageFormat = jsonFormat7(ResponseFeedServicesTopicMessage)
  implicit val requestStrategyServicesTopicMessageFormat = jsonFormat7(RequestStrategyServicesTopicMessage)
  implicit val responseFeedFacadeTopicMessageFormat = jsonFormat7(ResponseFeedFacadeTopicMessage)
}

class StrategyActor extends ActorProducer[TttsStrategyMessage] with ActorLogging {
  import StrategyActor._
  import TttsStrategyMessages._
  import StrategyActorJsonProtocol._

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
			  
//		case Request(elements) if isActive => {
//		  
//			    val groupId = Configuration.facadeGroupId
//				val prps = new Properties()
//				prps.put("group.id", groupId)
//				prps.put("socket.buffer.size", Configuration.socketBufferSizeConsumer)
//				prps.put("fetch.size", Configuration.fetchSizeConsumer)
//				prps.put("auto.commit", Configuration.autoCommitConsumer)
//				prps.put("autocommit.interval.ms", Configuration.autocommitIntervalConsumer)
//				prps.put("autooffset.reset", Configuration.autooffsetResetConsumer)
//				prps.put("zookeeper.connect", Configuration.zookeeperConnectionConsumer)
//			
//				val config = new ConsumerConfig(prps)
//			    val connector = Consumer.create(config)
//			    val topic = Configuration.facadeTopic 
//			    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
//			    val maxMessages = -1 //no limit 
//			 
//			    try {
//		//		      stream map {arr =>
//		//				    val mess = new String(arr.message, "UTF-8")
//		//				    val msgJsonObj = mess.parseJson
//		//			        val msgStr = msgJsonObj.compactPrint
//		//				    
//		//				    val requestStrategyFacadeTopicMessage = msgJsonObj.convertTo[RequestStrategyFacadeTopicMessage]
//		//				    matchRequest(requestStrategyFacadeTopicMessage) match {
//		//				      case Some(facadeTopicMessage) => {
//		//				    	log.info("Facade Consumer got {}", requestStrategyFacadeTopicMessage)
//		//				        consumer.handleDelivery(requestStrategyFacadeTopicMessage)
//		//				      }
//		//				      case None => "Do nothing"
//		//				    }
//		//	    	  }
//
//			    	val clients = mutable.Map[String, TttsStrategyMessage]()
//			      
//					val mess = new String(stream.iterator().next().message, "UTF-8") 
//				    val msgJsonObj = mess.parseJson
//			        val msgStr = msgJsonObj.compactPrint
//			        val message = msgStr match {
//				      case result if(msgStr.contains(STRATEGY_REQUEST_MESSAGE_TYPE)) => {
//				        val requestStrategyServicesTopicMessage = msgJsonObj.convertTo[RequestStrategyServicesTopicMessage]
//					    clients += (requestStrategyServicesTopicMessage.client -> requestStrategyServicesTopicMessage)
//					    requestStrategyServicesTopicMessage
//				      }
//				      case result if(msgStr.contains(STRATEGY_STOP_REQUEST_MESSAGE_TYPE)) => {
//				        // 1. Convert string JSON message to correspondent message type
//				        val requestStrategyServicesTopicMessage = msgJsonObj.convertTo[RequestStrategyServicesTopicMessage]
//					    clients -= requestStrategyServicesTopicMessage.client
//					    requestStrategyServicesTopicMessage
//				      }
//				      case result if(msgStr.contains(FEED_RESPONSE_MESSAGE_TYPE )) => {
//			        	val responseServicesMessage = msgJsonObj.convertTo[ResponseFeedServicesTopicMessage]
//		        	    // Only process FEED_RSP messages intended to this Service instance
//		        		if(responseServicesMessage.serviceId equals serviceId) {
//				        	// If message is in registered clients map, it was initiated from services topic. Otherwise, request came from Facade Topic 
//		        			log.info("Services Consumer got {}", responseServicesMessage)
//				        	if(clients contains responseServicesMessage.client) {
//				        	    // Here we need to sub serviceId that came from FeedMS to serviceId of the Microservice that requested StrategyMS at the beginning
//				        	    val callerServiceId = clients(responseServicesMessage.client).asInstanceOf[RequestStrategyServicesTopicMessage].serviceId
//				        	    log.debug("!!!!!!!!!!!!!! reassigning FEED serviceId {} to original serviceID {}", responseServicesMessage.serviceId, callerServiceId)
//				        	    val reassignedResponseServiceMessage = ResponseFeedServicesTopicMessage(responseServicesMessage.id, responseServicesMessage.msgType, responseServicesMessage.client, responseServicesMessage.payload, responseServicesMessage.timestamp, responseServicesMessage.sequenceNum, callerServiceId)
//				        	    reassignedResponseServiceMessage
//		        			} else {
//		        				val responseServicesMessage = msgJsonObj.convertTo[ResponseFeedFacadeTopicMessage]
//		        				responseServicesMessage
//		        			}
//			        	}
//				      }
//					}
////				    val requestStrategyFacadeTopicMessage = msgJsonObj.convertTo[RequestStrategyFacadeTopicMessage]
//					onNext(message)
//				} catch {
//				  case e: Throwable => log.error("KafkaFacadeTopicConsumerActor Error processing message, stop consuming: " + e)
//				}
//	      }
			  
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
