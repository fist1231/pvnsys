package com.pvnsys.ttts.strategy.mq

import akka.actor._
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.strategy.Configuration
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import spray.json._


object KafkaServicesTopicProducerActor {
  sealed trait KafkaServicesTopicProducerActorMessage
  case object StopMessage extends KafkaServicesTopicProducerActorMessage
  
}

object KafkaServicesTopicProducerActorJsonProtocol extends DefaultJsonProtocol {
  import TttsStrategyMessages._
  implicit val feedPayloadFormat = jsonFormat10(FeedPayload)
  implicit val strategyPayloadFormat = jsonFormat13(StrategyPayload)
  implicit val requestFeedServicesTopicMessageFormat = jsonFormat7(RequestFeedServicesTopicMessage)
  implicit val responseStrategyServicesTopicMessageFormat = jsonFormat8(ResponseStrategyServicesTopicMessage)
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
  import TttsStrategyMessages._

	val props = new Properties();
	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer);
	props.put("serializer.class", Configuration.serializerClassProducer);
//	props.put("message.send.max.retries", "7");
//	props.put("retry.backoff.ms", "1000");

	val producer = new Producer[Integer, String](new ProducerConfig(props));
  
  
  override def receive = {
    /*
     * KafkaServicesTopicProducerActor sends out only two message types: 
     * 1. FEED_REQ of RequestFeedServicesTopicMessage
     * 2. FEED_STOP_REQ of RequestFeedServicesTopicMessage
     * 3. STRATEGY_RSP of ResponseStrategyServicesTopicMessage
     */ 
    case msg: RequestFeedServicesTopicMessage => {
      val client = sender
      produceKafkaMsg(msg, client)
//      sender ! ProducerConfirmationMessage
//      self ! StopMessage
    }
    case msg: ResponseStrategyServicesTopicMessage => {
      val client = sender
      produceKafkaMsg(msg, client)
//      self ! StopMessage
    }
    case StopMessage => {
//      self ! PoisonPill
      context stop self 
    }
    case msg => log.error(s"KafkaServicesTopicProducerActor received unknown message $msg")
  }
  
  override def postStop() = {
  }
  
  
  def produceKafkaMsg(msg: TttsStrategyMessage, client: ActorRef) = {
//	val props = new Properties()
//	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer)
//	props.put("serializer.class", Configuration.serializerClassProducer)
//	props.put("message.send.max.retries", "7");
//	props.put("retry.backoff.ms", "1000");
//
//	val producer = new Producer[Integer, String](new ProducerConfig(props))
    val topic = Configuration.servicesTopic 

    msg match {
      case x: RequestFeedServicesTopicMessage => {
	    // Convert RequestServicesMessage back to JsValue
	    val jsonStrMessage = x.asInstanceOf[RequestFeedServicesTopicMessage].toJson.compactPrint
	//    log.debug("KafkaServicesTopicProducerActor converted message to JSON: {}", jsonStrMessage)
	    // Send it to Kafka Services Topic
        log.info("Services Producer sent {}", x)
	   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));
      }
      case x: ResponseStrategyServicesTopicMessage => {
	    // Convert ResponseStrategyServicesTopicMessage back to JsValue
	    val jsonStrMessage = x.asInstanceOf[ResponseStrategyServicesTopicMessage].toJson.compactPrint
	//    log.debug("KafkaServicesTopicProducerActor converted message to JSON: {}", jsonStrMessage)
	    // Send it to Kafka Services Topic
        log.info("Services Producer sent {}", x)
	   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));
//	    client ! ProducerConfirmationMessage
	    
      }
      case _ => "Do nothing"
    }
    

//    producer.close
  }
  
}