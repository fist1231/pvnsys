package com.pvnsys.ttts.feed.mq

import akka.actor.{Actor, ActorRef, ActorLogging, Props, AllForOneStrategy}
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.{StartListeningFacadeTopicMessage, FacadeTopicMessage, RequestFeedFacadeTopicMessage, TttsFeedMessage}
//import akka.util.ByteString
import kafka.consumer.ConsumerConfig
//import kafka.consumer.ConsumerIterator
//import kafka.consumer.KafkaStream
//import kafka.javaapi.consumer.ConsumerConnector
import java.util.Properties
//import java.util.Random
import kafka.consumer.Consumer
//import java.util.HashMap
//import scala.collection.mutable._
import scala.collection.JavaConversions._
//import com.pvnsys.ttts.feed.KafkaConsumerMessage
//import com.pvnsys.ttts.feed.KafkaReceivedMessage
//import com.pvnsys.ttts.feed.KafkaNewMessage
//import org.java_websocket.WebSocket
//import java.net.InetSocketAddress
//import java.nio.ByteBuffer
//import kafka.message.Message
import com.pvnsys.ttts.feed.Configuration
import com.pvnsys.ttts.feed.FeedActor
//import com.pvnsys.ttts.feed.KafkaStartListeningMessage
//import org.reactivestreams.api.Producer
import akka.actor.SupervisorStrategy.{Restart, Stop}
import spray.json._


object KafkaFacadeTopicConsumerActorJsonProtocol extends DefaultJsonProtocol {
  implicit val facadeTopicMessageFormat = jsonFormat4(FacadeTopicMessage)
}

object KafkaFacadeTopicConsumerActor {
//  def props(address: InetSocketAddress, groupName: Option[String]) = Props(new KafkaConsumerActor(address, groupName))
  def props(toWhom: ActorRef) = Props(new KafkaFacadeTopicConsumerActor(toWhom))
}

/**
 * This actor will register itself to consume messages from the AkkaMQ server. 
 */
class KafkaFacadeTopicConsumerActor(toWhom: ActorRef) extends Actor with ActorLogging {
  
	import KafkaFacadeTopicConsumerActor._
	import FeedActor._
	import context._
	import KafkaFacadeTopicConsumerActorJsonProtocol._
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("KafkaFacadeTopicConsumerActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	
	private def startListening() = {
		
		val consumer = new DefaultKafkaConsumer {
		    override def handleDelivery(message: TttsFeedMessage) = {
		        toWhom ! message
		    }
		}
		register(consumer)
	}

	
	
	override def receive = {
		case StopMessage => {
			log.debug("******* KafkaConsumerActor StopMessage")
			//self ! PoisonPill
		}
		case StartListeningFacadeTopicMessage => {
			log.debug(s"******* Start Listening in KafkaConsumerActor")
//	        log.info("******* KafkaStartListeningMessage send self {}", self)
//			self ! KafkaReceivedMessage("uno", "dos")

			startListening()
		}

		case _ => log.error("******* KafkaConsumerActor Received unknown message")
	}
	
	
	private def register(consumer: DefaultKafkaConsumer): Unit = {

		val groupId = Configuration.feedGroupId
		val prps = new Properties()
		prps.put("group.id", groupId)
		prps.put("socket.buffer.size", Configuration.socketBufferSizeConsumer)
		prps.put("fetch.size", Configuration.fetchSizeConsumer)
		prps.put("auto.commit", Configuration.autoCommitConsumer)
		prps.put("autocommit.interval.ms", Configuration.autocommitIntervalConsumer)
		prps.put("autooffset.reset", Configuration.autooffsetResetConsumer)
		prps.put("zookeeper.connect", Configuration.zookeeperConnectionConsumer)
	
		val config = new ConsumerConfig(prps)
	    val connector = Consumer.create(config)
	    val topic = Configuration.facadeTopic 
	    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
	    val maxMessages = -1 //no limit 
	 
	    try {
		      stream map {arr =>
				    val mess = new String(arr.message, "UTF-8")
				    val msgJsonObj = mess.parseJson
			        val msgStr = msgJsonObj.prettyPrint
				    log.debug("***** KafkaFacadeTopicConsumerActor received JSON message from Kafka: {}", msgStr)
				    
				    val facadeTopicMessage = msgJsonObj.convertTo[FacadeTopicMessage]
				    matchRequest(facadeTopicMessage) match {
				      case Some(facadeTopicMessage) => consumer.handleDelivery(facadeTopicMessage)
				      case None => "Lets do nothing"
				    }
		      }
		} catch {
		  case e: Throwable => log.error("~~~~ error processing message, stop consuming: " + e)
		}
	  
	}
	
  private def matchRequest(message: FacadeTopicMessage): Option[FacadeTopicMessage] = message.msgType match {
  	  case "FEED_REQ" => Some(message)
  	  case "FEED_STOP_REQ" => Some(message)
  	  case _ => {
  	    log.debug("^^^^^ KafkaFacadeTopicConsumerActorJsonProtocol - not Feed Service request, skipping Kafka message") 
  	    None
  	  }
  }
	
   
	override def postStop() = {}
}
