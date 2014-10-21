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
    
	   override def receive = {
	    case KafkaNewMessage(messaga) => 
	      log.debug(s"111#######*****########## Gettin message: $messaga")
	      val rand = Seq.fill(5)(scala.util.Random.nextInt(100))
	      val msg = s"Dummy quote: $rand"
	      log.debug(s"222#######*****########## Gettin message: $msg")
//	      val feedActor = context.actorOf(Props[FeedActor])
//	      feedActor ! KafkaConsumerMessage(messaga)
//              if(Some(webSock).asInstanceOf[WebSocket].isOpen()) {
//		        Some(webSock).asInstanceOf[WebSocket].send(messaga)
//		      } else {
//		        log.debug(s"#####*******##### Unregistering self")
////		        context.stop(self)
//		      }

	    case KafkaConsumerMessage(ws: WebSocket) => 
	      log.debug(s"##@@@@@@@### Trying to Kafka Consume message")
	      startConsumation(ws)
//	      webSock = Option(ws)
	    
	    case mmm => log.error(s"##@@@@@@@## Received unknown message $mmm")
	  }

	
	
	def startConsumation(ws: WebSocket) = {
		val prps = new Properties()
	    prps.put("group.id", "group1")
	    prps.put("socket.buffer.size", "65536")
	    prps.put("fetch.size", "1048576")
	    prps.put("auto.commit", "true")
	    prps.put("autocommit.interval.ms", "10000")
	    prps.put("autooffset.reset", "smallest")
	    prps.put("zookeeper.connect", "127.0.0.1:2181")
	    val config = new ConsumerConfig(prps)
	  
	    val connector = Consumer.create(config)
	   
	    val topic = "test"
	
	    log.debug(s"************ Gettin Topic")
	    
	    val topicCountMap = new HashMap[String, Integer]();
	    topicCountMap.put(topic, new Integer(1));
	
	    log.debug(s"************ Gettin Stream")
	    var stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
	    
	    val maxMessages = -1 //no limit 
	    
	    var webSock: Option[WebSocket] = None
	    
	    log.debug(s"************ Gettin Iterator")
	    
	    val iter =
	      if(maxMessages >= 0)
	        stream.slice(0, maxMessages)
	      else
	        stream
	 
	    try {
	      for(message <- iter) {
	        try {
	          
			    val arr = message.message
			    val mess = new String(arr, "UTF-8")
 
	          
	//          formatter.writeTo(message, System.out)
	        	log.debug(s"************ Gettin message: $message.toString")
//	        	context.self ! KafkaNewMessage(message.toString)
	        	
	    val feedPushActor = context.actorOf(Props[FeedPushActor])
	    feedPushActor ! KafkaReceivedMessage(ws, mess)
	        	
	        	
	        	log.debug(s"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
	        } catch {
	          case e: Throwable =>
	            if (false) { // skipMessageOnError = true|false
	              log.error("~~~~ error processing message, skipping and resume consumption: " + e)
	            }
	            else {
	              log.error("~~~~ ******* error processing message, failing " + e)
	              throw e
	            }
	        }
	      }
	    } catch {
	      case e: Throwable => log.error("~~~~ error processing message, stop consuming: " + e)
	    }
	
	//    System.out.flush()
	//    formatter.close()
	//    connector.shutdown()
	        
	}

    override def postStop() = {}
}

class FeedPushActor extends Actor with ActorLogging {
  def receive = {
    case KafkaReceivedMessage(ws, messg) => {
      val rand = Seq.fill(5)(scala.util.Random.nextInt(100))
      val msg = s"Dummy quote: $rand"
      log.debug(s"###*******^^^^###### Sending message: $messg")
      if(ws.isOpen()) {
        ws.send(messg)
      } else {
        log.debug(s"**** Unregistering self")
        context.stop(self)
      }
    }
  }
  
}

