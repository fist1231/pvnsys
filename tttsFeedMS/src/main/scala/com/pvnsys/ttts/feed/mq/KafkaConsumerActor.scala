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


object KafkaConsumerActor {
  def props(address: InetSocketAddress, groupName: Option[String]) = Props(new KafkaConsumerActor(address, groupName))
}

/**
 * This actor will register itself to consume messages from the RabbitMQ server. 
 * At the same time it will play the role of a <code>Producer</code> for our processing <code>Flow</code>.
 */
class KafkaConsumerActor(address: InetSocketAddress, groupName: Option[String]) extends Actor with ActorLogging {
  
	import KafkaConsumerActor._
	import FeedActor._
	
	val did = scala.util.Random.nextInt(100).toString
	val suff = Configuration.groupIdConsumer
	
	var groupId = "feed-ms-group-1"
	groupName match {
	  case Some(str) => {
	    	groupId = str
	  }
	  case None => //log.debug("@@@@@@@@@@@@@@@@@ no such key {}", key)
	}

	
	log.info(s"oooooooooooooooooooooooooooo KafkaConsumer GroupID parameter: $groupName")
	
	log.info(s"oooooooooooooooooooooooooooo Starting KafkaConsumer with GroupID: $groupId")
	
	  
//	log.debug(s"oooooooooooooooooooooooooooo KafkaConsumerActor, GroupID is: $groupId")
	  
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
	      val it = stream.iterator()
	      while(it.hasNext) {
	        try {
			    val arr = it.next.message
			    val mess = new String(arr, "UTF-8")
		    val idx = mess.indexOf(" ==> ")
		    val key = mess.substring(0, idx)
//		    log.debug(s"mmmmmmmmmmmmmmmmmmmmmmm KafkaConsumerActor, the key is: $key")
		    
		    val feedPushActor = context.actorOf(Props(classOf[FeedPushActor]))
		    feedPushActor ! KafkaReceivedMessage(key, mess)
		    feedPushActor ! StopMessage
	    } catch {
	      case e: Throwable =>
	        if (false) { // skipMessageOnError = true|false
	          log.error("~~~~ error processing message, skipping and resume consumption: " + e)
	        }
	        else {
	          log.error("~~~~ error processing message, failing " + e)
	          throw e
	        }
	    }
	    
	  }
	  for(message <- stream) {
	  }
	} catch {
	  case e: Throwable => log.error("~~~~ error processing message, stop consuming: " + e)
	}
	    
   override def receive = {
    case KafkaNewMessage(messaga) => 
      log.debug(s"!!!!!!!!!!!!!!! Rolling KafkaConsumerActor, Gettin message: $messaga")
	  val rand = Seq.fill(5)(scala.util.Random.nextInt(100))
	  val msg = s"Dummy quote: $rand"
	
	case StopMessage => {
	  self ! PoisonPill
	}
	case mmm => log.error(s"^^^^^ KafkaConsumerActor Received unknown message $mmm")
  }
	
  override def postStop() = {}
}

class FeedPushActor extends Actor with ActorLogging {
	import KafkaConsumerActor._
	import FeedActor._
  
  def receive = {
    case KafkaReceivedMessage(key, msg) => {
//	      log.debug(s"***************************** KafkaConsumerActor received KafkaReceivedMessage: $msg")
//	      context.actorSelection("/user/feed") ! KafkaReceivedMessage(key, msg)
    	log.info("*************** KafkaConsumerActor in TTTSFeedMS got the message: {} for the key {}", msg, key)
    
    }
    case StopMessage => {
	  self ! PoisonPill
    }
    
  }
  
}

