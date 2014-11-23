package com.pvnsys.ttts.feed.mq

import akka.actor._
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.feed.Configuration
import com.pvnsys.ttts.feed.messages.TttsFeedMessages
import spray.json._

object KafkaServicesTopicProducerActor {
}

object KafkaServicesTopicProducerActorJsonProtocol extends DefaultJsonProtocol {
  import TttsFeedMessages._
  implicit val feedPayloadFormat = jsonFormat10(FeedPayload)
  implicit val responseFeedServicesTopicMessageFormat = jsonFormat7(ResponseFeedServicesTopicMessage)
}

/**
 * Producer of Kafka messages.
 * 
 * Initiates a new connection on a Connect message and returns it to the sender.
 * Takes care of closing the connections on system close. 
 */
class KafkaServicesTopicProducerActor extends Actor with ActorLogging {

  import KafkaServicesTopicProducerActor._
  import FeedActor._
  import KafkaServicesTopicProducerActorJsonProtocol._
  import TttsFeedMessages._

	val props = new Properties();
	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer);
	props.put("serializer.class", Configuration.serializerClassProducer);
	props.put("message.send.max.retries", "7");
	props.put("retry.backoff.ms", "1000");

	val producer = new Producer[Integer, String](new ProducerConfig(props));
  
  override def receive = {
    case msg: ResponseFeedServicesTopicMessage => {
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
  
  
  def produceKafkaMsg(msg: ResponseFeedServicesTopicMessage) = {
    log.info("Services Producer sent {}", msg)
//	val props = new Properties()
//	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer)
//	props.put("serializer.class", Configuration.serializerClassProducer)
//	props.put("message.send.max.retries", "7");
//	props.put("retry.backoff.ms", "1000");
//
//	val producer = new Producer[Integer, String](new ProducerConfig(props))
    val topic = Configuration.servicesTopic 

    // Convert RequestFacadeMessage back to JsValue
    val jsonStrMessage = msg.toJson.compactPrint
    // Send it to Kafka Services Topic
   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));

//    producer.close
  }
  
}