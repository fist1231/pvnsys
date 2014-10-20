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

object KafkaProducerActor {

  case object Connect
  
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
  
	val props = new Properties();
	props.put("serializer.class", "kafka.serializer.StringEncoder");
	// server.properties file
	props.put("metadata.broker.list", "localhost:9092");
	
	val producer = new Producer[Integer, String](new ProducerConfig(props));
	val topic = "test"

  override def receive = {
    case KafkaProducerMessage() => {
      log.debug("+++++++++ Creatiing Kafka Message")
      produceKafkaMsg()
      log.info(s"Connected to Kafka server on localhost:9092")
    }
    case msg => log.error(s"+++++ Received unknown message $msg")
  }
  
  override def postStop() = {
//    connections foreach { conn => 
//      log.info("Closing connection")
//      conn.close() 
    }
  
  def produceKafkaMsg() = {
    var messageNo = 1
    while(messageNo < 10) {
    	val messageStr = s"+++++ KAFKA Message: $messageNo"
    	producer.send(new KeyedMessage[Integer, String](topic, messageStr));
    	messageNo += 1
    	Thread.sleep(1000)
  	}
  }
  
}