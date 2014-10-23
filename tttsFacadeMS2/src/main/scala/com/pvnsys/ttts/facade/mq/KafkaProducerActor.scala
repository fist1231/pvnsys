package com.pvnsys.ttts.facade.mq

import akka.actor._
//import com.rabbitmq.client.Connection
//import com.rabbitmq.client.ConnectionFactory
import java.net.InetSocketAddress
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.facade.feed.KafkaProducerMessage
import com.pvnsys.ttts.facade.Configuration
import com.pvnsys.ttts.facade.feed.FeedActor


object KafkaProducerActor {
  def props(address: InetSocketAddress) = Props(new KafkaProducerActor(address))
}

/**
 * Producer of Kafka messages.
 * 
 * Initiates a new connection on a Connect message and returns it to the sender.
 * Takes care of closing the connections on system close. 
 */
class KafkaProducerActor(address: InetSocketAddress) extends Actor with ActorLogging {

  import KafkaProducerActor._
  import FeedActor._
  
	
  override def receive = {
    case KafkaProducerMessage(socketId) => {
      produceKafkaMsg(socketId)
    }

    case StopMessage => {
      self ! PoisonPill
    }
    
    case msg => log.error(s"+++++ Received unknown message $msg")
    
    
  }
  
  override def postStop() = {
  }
  
  def produceKafkaMsg(sid: String) = {
	val props = new Properties();
	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer);
	props.put("serializer.class", Configuration.serializerClassProducer);

	val producer = new Producer[Integer, String](new ProducerConfig(props));
    val topic = Configuration.topicProducer
    var messageNo = 1
    while(messageNo < 6) {
    	val messageStr = s"$sid ==> KAFKA Message: $messageNo"
    	log.info(s"###### KafkaProducerActor sending message $messageStr")
    	producer.send(new KeyedMessage[Integer, String](topic, messageStr));
    	messageNo += 1
    	Thread.sleep(1000)
  	}
    producer.close
  }
  
}