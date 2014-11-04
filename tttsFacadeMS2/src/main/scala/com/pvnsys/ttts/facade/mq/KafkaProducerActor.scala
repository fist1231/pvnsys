package com.pvnsys.ttts.facade.mq

import akka.actor.{Props, Actor, ActorLogging, PoisonPill}
import java.net.InetSocketAddress
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.facade.Configuration
import com.pvnsys.ttts.facade.feed.FeedActor
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.{RequestFacadeMessage, ResponseFacadeMessage}
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.TttsFacadeMessage
import spray.json._
//import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.FacadeOutgoingFeedRequestMessage
//import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.FacadeIncomingFeedResponseMessage
//import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.FacadeIncomingFeedResponseMessage
import scala.util.Random


object KafkaProducerActor {
  sealed trait FacadeProducerMessage
  case object StopMessage extends FacadeProducerMessage

  // Not using Address at the moment, reserved for a future use
  def props(address: InetSocketAddress) = Props(new KafkaProducerActor(address))
}

object KafkaProducerActorJsonProtocol extends DefaultJsonProtocol {
  implicit val requestFacadeMessageFormat = jsonFormat4(RequestFacadeMessage)
  implicit val responseFacadeMessageForman = jsonFormat4(ResponseFacadeMessage)
}

/**
 * Producer of Kafka messages.
 * 
 * Initiates a new connection on a Connect message and returns it to the sender.
 * Takes care of closing the connections on system close. 
 */
class KafkaProducerActor(address: InetSocketAddress) extends Actor with ActorLogging {

  import KafkaProducerActor._
  import KafkaProducerActorJsonProtocol._
	
  override def receive = {
//    case FacadeOutgoingMessage(socketId) => {
//      produceKafkaMsg(socketId)
//      self ! StopMessage
//    }
    
    case msg: RequestFacadeMessage => {

      log.debug("###### KafkaProducerActor received RequestFacadeMessage: {}", msg)

      produceKafkaMsg(msg)
      self ! StopMessage
    }

    case StopMessage => {
      self ! PoisonPill
    }
    
    case msg => log.error(s"+++++ Received unknown message $msg")
    
    
  }
  
  override def postStop() = {
  }
  
//  def produceKafkaMsg(sid: String) = {
//	val props = new Properties();
//	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer);
//	props.put("serializer.class", Configuration.serializerClassProducer);
//
//	val producer = new Producer[Integer, String](new ProducerConfig(props));
//    val topic = Configuration.facadeTopic 
//    var messageNo = 1
//   	producer.send(new KeyedMessage[Integer, String](topic, sid));
////    while(messageNo <= 100) {
////    	val messageStr = s"$sid ==> KAFKA Message: $messageNo"
////    	log.info(s"###### KafkaProducerActor sending message $messageStr")
////    	producer.send(new KeyedMessage[Integer, String](topic, messageStr));
////    	messageNo += 1
////    	Thread.sleep(100)
////  	}
//    producer.close
//  }

  def produceKafkaMsg(msg: RequestFacadeMessage) = {
	val props = new Properties();
	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer);
	props.put("serializer.class", Configuration.serializerClassProducer);

	val producer = new Producer[Integer, String](new ProducerConfig(props));
    val topic = Configuration.facadeTopic 

    // Convert RequestFacadeMessage back to JsValue
    //val jsonMessage = msg.asInstanceOf[RequestFacadeMessage].toJson.compactPrint
    val jsonStrMessage = msg.toJson.compactPrint
    // Send it to Kafka facadeTopic
   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));

  
/*
 *    For testing purposes only, we will convert RequestFacadeMessage message to ResponseFacadeMessage one
 *    and receive it in KafkaConsumerActor.
*/     
//    emulateFeedServiseResponse(msg, producer, topic)
    
    producer.close
  }
  
  private def emulateFeedServiseResponse(msg: RequestFacadeMessage, producer: Producer[Integer, String], topic: String) = {
    // Emulating Quotes feed stream.
    var messageNo = 1
    while(messageNo <= 10) {
    	val fakeQuote = "%.2f".format(Random.nextFloat()+7)
	    val fakeMessage = ResponseFacadeMessage(msg.asInstanceOf[RequestFacadeMessage].id, msg.asInstanceOf[RequestFacadeMessage].msgType, msg.asInstanceOf[RequestFacadeMessage].client, s"$fakeQuote" )
	    val jsonStrMessage = fakeMessage.toJson.compactPrint
//	    log.debug("###### KafkaProducerActor converted FacadeOutgoingFeedRequestMessage to JSON: {}", jsonStrMessage)
    	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));
    	Thread.sleep(500)
    	messageNo += 1
    }    
  }
  
  
}