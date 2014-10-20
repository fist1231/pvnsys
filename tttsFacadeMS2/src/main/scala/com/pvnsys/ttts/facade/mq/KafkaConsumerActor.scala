package com.pvnsys.ttts.facade.mq

import akka.actor._
import akka.util.ByteString
import kafka.consumer.ConsumerConfig
import kafka.consumer.ConsumerIterator
import kafka.consumer.KafkaStream
import kafka.javaapi.consumer.ConsumerConnector
import java.util.Properties
import joptsimple.OptionParser
import java.util.Random
import kafka.consumer.Consumer
import java.util.HashMap;
import scala.collection.mutable._
import scala.collection.JavaConversions._
import com.pvnsys.ttts.facade.feed.KafkaConsumerMessage
import com.pvnsys.ttts.facade.feed.KafkaNewMessage
import com.pvnsys.ttts.facade.feed.FeedActor
import org.java_websocket.WebSocket


object KafkaConsumerActor {
}

/**
 * This actor will register itself to consume messages from the RabbitMQ server. 
 * At the same time it will play the role of a <code>Producer</code> for our processing <code>Flow</code>.
 */
class KafkaConsumerActor extends Actor with ActorLogging {
  
	import KafkaConsumerActor._
    
	val props = new Properties()
    props.put("group.id", "group1")
    props.put("socket.buffer.size", "65536")
    props.put("fetch.size", "1048576")
    props.put("auto.commit", "true")
    props.put("autocommit.interval.ms", "10000")
    props.put("autooffset.reset", "smallest")
    props.put("zookeeper.connect", "127.0.0.1:2181")
    val config = new ConsumerConfig(props)
  
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
//          formatter.writeTo(message, System.out)
        	log.debug(s"************ Gettin message: $message")
        	self ! new KafkaNewMessage(message.toString)
        } catch {
          case e =>
            if (true) // skipMessageOnError = true|false
              log.error("~~~~ error processing message, skipping and resume consumption: " + e)
            else
              throw e
        }
      }
    } catch {
      case e => log.error("~~~~ error processing message, stop consuming: " + e)
    }
      
//    System.out.flush()
//    formatter.close()
//    connector.shutdown()
        
    

   override def receive = {
	    case KafkaNewMessage(messaga) => {
	      val rand = Seq.fill(5)(scala.util.Random.nextInt(100))
	      val msg = s"Dummy quote: $rand"
	      log.debug(s"#######*****########## Gettin message: $msg")
//	      val feedActor = context.actorOf(Props[FeedActor])
//	      feedActor ! KafkaConsumerMessage(messaga)
              if(Some(webSock).asInstanceOf[WebSocket].isOpen()) {
		        Some(webSock).asInstanceOf[WebSocket].send(messaga)
		      } else {
		        log.debug(s"#####*******##### Unregistering self")
//		        context.stop(self)
		      }

	      
	    }
	    case KafkaConsumerMessage(ws) => {
	      log.debug(s"##@@@@@@@### Trying to Kafka Consume message")
	      webSock = Option(ws)
	    }
	    case mmm => log.error(s"##@@@@@@@## Received unknown message $mmm")
	  }

    
  
    override def postStop() = {}
}
