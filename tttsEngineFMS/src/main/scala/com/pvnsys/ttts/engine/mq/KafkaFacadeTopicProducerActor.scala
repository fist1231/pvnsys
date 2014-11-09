package com.pvnsys.ttts.engine.mq

import akka.actor._
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.engine.Configuration
import com.pvnsys.ttts.engine.messages.TttsEngineMessages.ResponseEngineFacadeTopicMessage
import spray.json._


object KafkaFacadeTopicProducerActor {
}

object KafkaFacadeTopicProducerActorJsonProtocol extends DefaultJsonProtocol {
  implicit val responseEngineFacadeTopicMessageFormat = jsonFormat7(ResponseEngineFacadeTopicMessage)
}

/**
 * Producer of Kafka messages.
 * 
 * Initiates a new connection on a Connect message and returns it to the sender.
 * Takes care of closing the connections on system close. 
 */
class KafkaFacadeTopicProducerActor extends Actor with ActorLogging {

  import KafkaFacadeTopicProducerActor._
  import EngineActor._
  import KafkaFacadeTopicProducerActorJsonProtocol._
  
	
  override def receive = {
    /*
     * KafkaFacadeTopicProducerActor sends out only one message type: 
     * 1. ENGINE_RESP of ResponseEngineFacadeTopicMessage
     */ 
    case msg: ResponseEngineFacadeTopicMessage => {
      produceKafkaMsg(msg)
      self ! StopMessage
    }
    case StopMessage => {
      self ! PoisonPill
    }
    case msg => log.error(s"KafkaFacadeTopicProducerActor received unknown message $msg")
  }
  
  override def postStop() = {
  }
  
  
  def produceKafkaMsg(msg: ResponseEngineFacadeTopicMessage) = {
    log.debug("KafkaFacadeTopicProducerActor publishing message to Kafka Facade Topic: {}", msg)
	val props = new Properties()
	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer)
	props.put("serializer.class", Configuration.serializerClassProducer)

	val producer = new Producer[Integer, String](new ProducerConfig(props))
    val topic = Configuration.facadeTopic 

    // Convert RequestFacadeMessage back to JsValue
    val jsonStrMessage = msg.toJson.compactPrint
    // Send it to Kafka facadeTopic
   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));

    producer.close
  }
  
}