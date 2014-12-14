package com.pvnsys.ttts.feed.mq

import akka.actor._
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.feed.Configuration
import com.pvnsys.ttts.feed.messages.TttsFeedMessages
import spray.json._


object KafkaFacadeTopicProducerActor {
}

object KafkaFacadeTopicProducerActorJsonProtocol extends DefaultJsonProtocol {
  import TttsFeedMessages._
  implicit val feedPayloadFormat = jsonFormat10(FeedPayload)
  implicit val responseFeedFacadeTopicMessageFormat = jsonFormat6(ResponseFeedFacadeTopicMessage)
}

/**
 * Producer of Kafka messages.
 * 
 * Initiates a new connection on a Connect message and returns it to the sender.
 * Takes care of closing the connections on system close. 
 */
class KafkaFacadeTopicProducerActor extends Actor with ActorLogging {

  import KafkaFacadeTopicProducerActor._
  import FeedActor._
  import KafkaFacadeTopicProducerActorJsonProtocol._
  import TttsFeedMessages._

	val props = new Properties();
	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer);
	props.put("serializer.class", Configuration.serializerClassProducer);
//	props.put("message.send.max.retries", "7");
//	props.put("retry.backoff.ms", "1000");

	val producer = new Producer[Integer, String](new ProducerConfig(props));
  
  override def receive = {
    case msg: ResponseFeedFacadeTopicMessage => {
      produceKafkaMsg(msg)
//      self ! StopMessage
    }
    case StopMessage => {
      self ! PoisonPill
    }
    case msg => log.error(s"Received unknown message $msg")
  }
  
  override def postStop() = {
  }
  
  
  def produceKafkaMsg(msg: ResponseFeedFacadeTopicMessage) = {
    log.info("Facade Producer sent {}", msg)
//	val props = new Properties();
//	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer);
//	props.put("serializer.class", Configuration.serializerClassProducer);
//	props.put("message.send.max.retries", "7");
//	props.put("retry.backoff.ms", "1000");
//
//	val producer = new Producer[Integer, String](new ProducerConfig(props));
    val topic = Configuration.facadeTopic 

    // Convert RequestFacadeMessage back to JsValue
    val jsonStrMessage = msg.toJson.compactPrint
    // Send it to Kafka facadeTopic
   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));

//    producer.close
  }
  
}