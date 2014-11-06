package com.pvnsys.ttts.strategy.mq

import akka.actor._
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.strategy.Configuration
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.RequestFeedServicesTopicMessage
import spray.json._


object KafkaServicesTopicProducerActor {
}

object KafkaServicesTopicProducerActorJsonProtocol extends DefaultJsonProtocol {
  implicit val requestFeedServicesTopicMessageFormat = jsonFormat6(RequestFeedServicesTopicMessage)
}

/**
 * Producer of Kafka messages.
 * 
 * Initiates a new connection on a Connect message and returns it to the sender.
 * Takes care of closing the connections on system close. 
 */
class KafkaServicesTopicProducerActor extends Actor with ActorLogging {

  import KafkaServicesTopicProducerActor._
  import StrategyActor._
  import KafkaServicesTopicProducerActorJsonProtocol._
  
	
  override def receive = {
    /*
     * KafkaServicesTopicProducerActor sends out only two message types: 
     * 1. FEED_REQ of RequestFeedServicesTopicMessage
     * 2. FEED_STOP_REQ of RequestFeedServicesTopicMessage
     */ 
    case msg: RequestFeedServicesTopicMessage => {
      produceKafkaMsg(msg)
      self ! StopMessage
    }
    case StopMessage => {
      self ! PoisonPill
    }
    case msg => log.error(s"KafkaServicesTopicProducerActor received unknown message $msg")
  }
  
  override def postStop() = {
  }
  
  
  def produceKafkaMsg(msg: RequestFeedServicesTopicMessage) = {
    log.debug("KafkaServicesTopicProducerActor publishing message to Kafka Services Topic: {}", msg)
	val props = new Properties()
	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer)
	props.put("serializer.class", Configuration.serializerClassProducer)

	val producer = new Producer[Integer, String](new ProducerConfig(props))
    val topic = Configuration.servicesTopic 

    // Convert RequestServicesMessage back to JsValue
    val jsonStrMessage = msg.toJson.compactPrint
//    log.debug("KafkaServicesTopicProducerActor converted message to JSON: {}", jsonStrMessage)
    // Send it to Kafka Services Topic
   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));

    producer.close
  }
  
}