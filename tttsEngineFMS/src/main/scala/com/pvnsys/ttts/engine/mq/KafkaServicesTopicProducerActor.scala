package com.pvnsys.ttts.engine.mq

import akka.actor._
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.engine.Configuration
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import spray.json._


object KafkaServicesTopicProducerActor {
  sealed trait KafkaServicesTopicProducerActorMessage
  case object StopMessage extends KafkaServicesTopicProducerActorMessage
}

object KafkaServicesTopicProducerActorJsonProtocol extends DefaultJsonProtocol {
  import TttsEngineMessages._
  implicit val strategyPayloadFormat = jsonFormat13(StrategyPayload)
  implicit val enginePayloadFormat = jsonFormat15(EnginePayload)
  implicit val requestStrategyServicesTopicMessageFormat = jsonFormat7(RequestStrategyServicesTopicMessage)
  implicit val responseEngineServicesTopicMessageFormat = jsonFormat8(ResponseEngineServicesTopicMessage)
}

/**
 * Producer of Kafka messages.
 * 
 * Initiates a new connection on a Connect message and returns it to the sender.
 * Takes care of closing the connections on system close. 
 */
class KafkaServicesTopicProducerActor extends Actor with ActorLogging {

  import KafkaServicesTopicProducerActor._
  import KafkaServicesTopicProducerActorJsonProtocol._
  import TttsEngineMessages._

	val props = new Properties()
	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer)
	props.put("serializer.class", Configuration.serializerClassProducer)
//	props.put("message.send.max.retries", "7");
//	props.put("retry.backoff.ms", "1000");

	val producer = new Producer[Integer, String](new ProducerConfig(props))
  
  
  override def receive = {
    /*
     * KafkaServicesTopicProducerActor sends out only two message types: 
     * 1. STRATEGY_REQ of RequestStrategyServicesTopicMessage
     * 2. STRATEGY_STOP_REQ of RequestStrategyServicesTopicMessage
     * 3. ENGINE_RSP of ResponseEngineServicesTopicMessage
     */ 
    case msg: RequestStrategyServicesTopicMessage => {
      produceKafkaMsg(msg)
    }
    case msg: ResponseEngineServicesTopicMessage => {
      produceKafkaMsg(msg)
    }
    case StopMessage => {
      context stop self
    }
    case msg => log.error(s"KafkaServicesTopicProducerActor received unknown message $msg")
  }
  
  override def postStop() = {
  }
  
  
  def produceKafkaMsg(msg: TttsEngineMessage) = {
//	val props = new Properties()
//	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer)
//	props.put("serializer.class", Configuration.serializerClassProducer)
//	props.put("message.send.max.retries", "7");
//	props.put("retry.backoff.ms", "1000");
//
//	val producer = new Producer[Integer, String](new ProducerConfig(props))
    val topic = Configuration.servicesTopic 

    msg match {
      case x: RequestStrategyServicesTopicMessage => {
	    // Convert RequestServicesMessage back to JsValue
	    val jsonStrMessage = x.asInstanceOf[RequestStrategyServicesTopicMessage].toJson.compactPrint
	    // Send it to Kafka Services Topic
	    log.info("Services Producer sent {}", msg)
	   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));
      }
      case x: ResponseEngineServicesTopicMessage => {
	    // Convert RequestServicesMessage back to JsValue
	    val jsonStrMessage = x.asInstanceOf[ResponseEngineServicesTopicMessage].toJson.compactPrint
	    // Send it to Kafka Services Topic
	    log.info("Services Producer sent {}", msg)
	   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));
      }
      case _ => "Do nothing"
	}
    

//    producer.close
  }
  
}