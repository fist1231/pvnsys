package com.pvnsys.ttts.engine.mq

import akka.actor._
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.engine.Configuration
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import spray.json._


object KafkaFacadeTopicProducerActor {
  sealed trait KafkaFacadeTopicProducerActorMessage
  case object StopMessage extends KafkaFacadeTopicProducerActorMessage
}

object KafkaFacadeTopicProducerActorJsonProtocol extends DefaultJsonProtocol {
  import TttsEngineMessages._
  implicit val enginePayloadFormat = jsonFormat15(EnginePayload)
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
  import KafkaFacadeTopicProducerActorJsonProtocol._
  import TttsEngineMessages._
  
  
	val props = new Properties();
	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer);
	props.put("serializer.class", Configuration.serializerClassProducer);
//	props.put("message.send.max.retries", "7");
//	props.put("retry.backoff.ms", "1000");

	val producer = new Producer[Integer, String](new ProducerConfig(props));
	
  
  override def receive = {
    /*
     * KafkaFacadeTopicProducerActor sends out only one message type: 
     * 1. ENGINE_RESP of ResponseEngineFacadeTopicMessage
     */ 
    case msg: ResponseEngineFacadeTopicMessage => {
      produceKafkaMsg(msg)
    }
    case StopMessage => {
      context stop self
    }
    case msg => log.error(s"KafkaFacadeTopicProducerActor received unknown message $msg")
  }
  
  override def postStop() = {
  }
  
  
  def produceKafkaMsg(msg: ResponseEngineFacadeTopicMessage) = {
//	val props = new Properties()
//	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer)
//	props.put("serializer.class", Configuration.serializerClassProducer)
//	props.put("message.send.max.retries", "7");
//	props.put("retry.backoff.ms", "1000");
//
//	val producer = new Producer[Integer, String](new ProducerConfig(props))
    val topic = Configuration.facadeTopic 

    // Convert RequestFacadeMessage back to JsValue
    val jsonStrMessage = msg.toJson.compactPrint
    // Send it to Kafka facadeTopic
    log.info("Facade Producer sent {}", msg)
   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));

//    producer.close
  }
  
}