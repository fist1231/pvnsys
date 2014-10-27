package com.pvnsys.ttts.feed.mq

import akka.actor._
import akka.util.ByteString
import kafka.consumer.ConsumerConfig
import kafka.consumer.ConsumerIterator
import kafka.consumer.KafkaStream
import kafka.javaapi.consumer.ConsumerConnector
import java.util.Properties
import java.util.Random
import kafka.consumer.Consumer
import java.util.HashMap
import scala.collection.mutable._
import scala.collection.JavaConversions._
import com.pvnsys.ttts.feed.KafkaConsumerMessage
import com.pvnsys.ttts.feed.KafkaReceivedMessage
import com.pvnsys.ttts.feed.KafkaNewMessage
import org.java_websocket.WebSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import kafka.message.Message
import com.pvnsys.ttts.feed.Configuration
import com.pvnsys.ttts.feed.FeedActor
import com.pvnsys.ttts.feed.KafkaStartListeningMessage
import org.reactivestreams.api.Producer


object KafkaConsumerActor {
//  def props(address: InetSocketAddress, groupName: Option[String]) = Props(new KafkaConsumerActor(address, groupName))
  def props(toWhom: ActorRef) = Props(new KafkaConsumerActor(toWhom))
}

/**
 * This actor will register itself to consume messages from the AkkaMQ server. 
 */
class KafkaConsumerActor(toWhom: ActorRef) extends Actor with ActorLogging {
  
	import KafkaConsumerActor._
	import FeedActor._
	import context._
	
	var groupId = "feed-ms-group-1"
	
	private def startListening() = {
		
		val consumer = new DefaultKafkaConsumer {
		    override def handleDelivery(key: String, mess: String) = {
		        toWhom ! KafkaReceivedMessage(key, mess)
		    }
		}
		register(consumer)
	}

	
	
	override def receive = {
		case StopMessage => {
			log.debug("******* KafkaConsumerActor StopMessage")
			//self ! PoisonPill
		}
		case KafkaStartListeningMessage => {
			log.debug(s"******* Start Listening in KafkaConsumerActor")
//	        log.info("******* KafkaStartListeningMessage send self {}", self)
			self ! KafkaReceivedMessage("uno", "dos")

			startListening()
		}

		case _ => log.error("******* KafkaConsumerActor Received unknown message")
	}
	
	
	private def register(consumer: DefaultKafkaConsumer): Unit = {

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
	    val topic = Configuration.topicConsumer
	    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
	    val maxMessages = -1 //no limit 
	 
	    try {
	      stream map {arr =>
			    val mess = new String(arr.message, "UTF-8")
			    val idx = mess.indexOf(" ==> ")
			    val key = mess.substring(0, idx)
			    consumer.handleDelivery(key, mess)
	      }
		} catch {
		  case e: Throwable => log.error("~~~~ error processing message, stop consuming: " + e)
		}
	  
	}
   
	override def postStop() = {}
}
