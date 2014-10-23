package com.pvnsys.ttts.facade.mq

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
import com.pvnsys.ttts.facade.feed.KafkaConsumerMessage
import com.pvnsys.ttts.facade.feed.KafkaReceivedMessage
import com.pvnsys.ttts.facade.feed.KafkaNewMessage
import org.java_websocket.WebSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import kafka.message.Message
import com.pvnsys.ttts.facade.Configuration
import com.pvnsys.ttts.facade.feed.FeedActor


object KafkaConsumerActor {
  case object Connect
  def props(address: InetSocketAddress) = Props(new KafkaConsumerActor(address))
}

/**
 * This actor will register itself to consume messages from the RabbitMQ server. 
 * At the same time it will play the role of a <code>Producer</code> for our processing <code>Flow</code>.
 */
class KafkaConsumerActor(address: InetSocketAddress) extends Actor with ActorLogging {
  
	import KafkaConsumerActor._
	import FeedActor._
    
//	var client :WebSocket = _
	
	   override def receive = {
	    case KafkaNewMessage(messaga) => 
	      log.debug(s"!!!!!!!!!!!!!!! Rolling KafkaConsumerActor, Gettin message: $messaga")
	      val rand = Seq.fill(5)(scala.util.Random.nextInt(100))
	      val msg = s"Dummy quote: $rand"
//	      log.debug(s"222#######*****########## Gettin message: $msg")
//	      val feedActor = context.actorOf(Props[FeedActor])
//	      feedActor ! KafkaConsumerMessage(messaga)
//              if(Some(webSock).asInstanceOf[WebSocket].isOpen()) {
//		        Some(webSock).asInstanceOf[WebSocket].send(messaga)
//		      } else {
//		        log.debug(s"#####*******##### Unregistering self")
////		        context.stop(self)
//		      }

	    case KafkaConsumerMessage(ws) => {
//	      startConsumation(ws)
	      val client = ws
	      if(client != null) {
		      val addrId = client.getRemoteSocketAddress().toString()
		      log.debug(s"!!!!!!!!!!!!!!! KafkaConsumerActor, Gettin WebSocket: $addrId")
	      } else {
	        self ! PoisonPill
	      }
	      startConsumation(client)
	    }

	    case StopMessage => {
          self ! PoisonPill
        }
	    
	    case mmm => log.error(s"^^^^^ KafkaConsumerActor Received unknown message $mmm")


	  }

	
	
	def startConsumation(client: WebSocket) = {
	  
	  val did = scala.util.Random.nextInt(100).toString
	  val suff = Configuration.groupIdConsumer
	  val groupId = s"GROUP_ID_$did-$suff"
	  
	  log.debug(s"oooooooooooooooooooooooooooo KafkaConsumerActor, GroupID is: $groupId")
	  
	  
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
	
//	    val topicCountMap = new HashMap[String, Integer]();
//	    topicCountMap.put(topic, new Integer(1));
	
	    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
//	    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get(0)
	    
	    val maxMessages = -1 //no limit 
	    
	    
//	    val iter =
//	      if(maxMessages >= 0)
//	        stream.slice(0, maxMessages)
//	      else
//	        stream
	 
	    try {
	      for(message <- stream) {
	        try {
	          
			    val arr = message.message
			    val mess = new String(arr, "UTF-8")
			    val feedPushActor = context.actorOf(Props(classOf[FeedPushActor]))
			    
	      val addrId = client.getRemoteSocketAddress().toString()
	      log.debug(s"xxxxxxxxxxxxxxxxxxx KafkaConsumerActor, Gettin WebSocket: $addrId")
			    
			    
			    feedPushActor ! KafkaReceivedMessage(client, mess)
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
	    } catch {
	      case e: Throwable => log.error("~~~~ error processing message, stop consuming: " + e)
	    }
	    
//	    feedPushActor ! Stop
	
	//    System.out.flush(
	//    formatter.close()
	    connector.shutdown()
	    
//	    client.close()
	    
	}

    override def postStop() = {}
}

class FeedPushActor extends Actor with ActorLogging {
	import KafkaConsumerActor._
	import FeedActor._
  
  
  def receive = {
    case KafkaReceivedMessage(ws, messg) => {
      
      
//      val rand = Seq.fill(5)(scala.util.Random.nextInt(100))
//      val msg = s"Dummy quote: $rand"
    	  
      if(null != ws) { //ws.isOpen()) {
    	  if(ws.isOpen()) {
    		  log.info(s"^^^^^ FeedPush Actor Websocket is OPEN !!")
    	  } else {
    		  log.info(s"^^^^^ FeedPush Actor Websocket is CLOSED  :((")
    	  }
    	log.info(s"^^^^^ FeedPush Actor sending message: $messg")
    	
        ws.send(messg)
      } else {
        log.error(s"^^^^^ FeedPushActor is not unregistering self due to WebSocket closure. Skipping the message $messg")
        context.stop(self)
      }
    }
    case StopMessage => {
	  self ! PoisonPill
    }
    
  }
  
}

