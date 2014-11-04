package com.pvnsys.ttts.strategy.mq

import akka.actor._
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.strategy.Configuration
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.ResponseStrategyServicesTopicMessage
import spray.json._


object KafkaServicesTopicProducerActor {
}

object KafkaServicesTopicProducerActorJsonProtocol extends DefaultJsonProtocol {
  implicit val responseStrategyServicesTopicMessageFormat = jsonFormat7(ResponseStrategyServicesTopicMessage)
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
    case msg: ResponseStrategyServicesTopicMessage => {
      produceKafkaMsg(msg)
      self ! StopMessage
    }
    case StopMessage => {
      self ! PoisonPill
    }
    case msg => log.error(s"Received unknown message $msg")
  }
  
  override def postStop() = {
  }
  
  
  def produceKafkaMsg(msg: ResponseStrategyServicesTopicMessage) = {
    log.debug("KafkaServicesTopicProducerActor publishing message to Kafka Facade Topic: {}", msg)
	val props = new Properties()
	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer)
	props.put("serializer.class", Configuration.serializerClassProducer)

	val producer = new Producer[Integer, String](new ProducerConfig(props))
    val topic = Configuration.servicesTopic 

    // Convert RequestFacadeMessage back to JsValue
    val jsonStrMessage = msg.toJson.compactPrint
    // Send it to Kafka Services Topic
   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));

    producer.close
  }
  
}