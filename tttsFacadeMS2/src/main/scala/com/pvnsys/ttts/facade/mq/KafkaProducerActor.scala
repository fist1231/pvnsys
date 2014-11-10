package com.pvnsys.ttts.facade.mq

import akka.actor.{Props, Actor, ActorLogging, PoisonPill}
import java.net.InetSocketAddress
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import java.util.Properties
import com.pvnsys.ttts.facade.Configuration
import com.pvnsys.ttts.facade.feed.FeedActor
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.{RequestFacadeMessage, ResponseFacadeMessage, RequestStrategyFacadeMessage, RequestEngineFacadeMessage}
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.TttsFacadeMessage
import spray.json._
import scala.util.Random
import com.pvnsys.ttts.facade.util.Utils


object KafkaProducerActor {
  sealed trait FacadeProducerMessage
  case object StopMessage extends FacadeProducerMessage

  // Not using Address at the moment, reserved for a future use
  def props(address: InetSocketAddress) = Props(new KafkaProducerActor(address))
}

object KafkaProducerActorJsonProtocol extends DefaultJsonProtocol {
  implicit val requestFacadeMessageFormat = jsonFormat6(RequestFacadeMessage)
  implicit val responseFacadeMessageForman = jsonFormat6(ResponseFacadeMessage)
  implicit val requestStrategyFacadeMessageFormat = jsonFormat6(RequestStrategyFacadeMessage)
  implicit val requestEngineFacadeMessageFormat = jsonFormat6(RequestEngineFacadeMessage)
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
  import Utils._
	
  override def receive = {

  	case msg: RequestFacadeMessage => {
      log.debug("KafkaProducerActor received RequestFacadeMessage: {}", msg)
      produceKafkaMsg(msg)
      self ! StopMessage
    }

  	case msg: RequestStrategyFacadeMessage => {
      log.debug("KafkaProducerActor received RequestStrategyFacadeMessage: {}", msg)
      produceKafkaMsg(msg)
      self ! StopMessage
    }

  	case msg: RequestEngineFacadeMessage => {
      log.debug("KafkaProducerActor received RequestEngineFacadeMessage: {}", msg)
      produceKafkaMsg(msg)
      self ! StopMessage
    }

  	case StopMessage => {
      self ! PoisonPill
    }
    case msg => log.error(s"KafkaProducerActor Received unknown message $msg")

  }
  
  override def postStop() = {
  }
  
  def produceKafkaMsg(msg: TttsFacadeMessage) = {
	val props = new Properties();
	props.put("metadata.broker.list", Configuration.metadataBrokerListProducer);
	props.put("serializer.class", Configuration.serializerClassProducer);

	val producer = new Producer[Integer, String](new ProducerConfig(props));
    val topic = Configuration.facadeTopic 

    // Convert RequestFacadeMessage back to JsValue
    //val jsonMessage = msg.asInstanceOf[RequestFacadeMessage].toJson.compactPrint
    msg match {
      case x:RequestFacadeMessage => {
		    val jsonStrMessage = x.toJson.compactPrint
		    // Send it to Kafka facadeTopic
		    log.info("Facade Producersent {}", x)
		   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));
      }
      case x:RequestStrategyFacadeMessage => {
		    val jsonStrMessage = x.toJson.compactPrint
		    // Send it to Kafka facadeTopic
		    log.info("Facade Producersent {}", x)
		   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));
      }
      case x:RequestEngineFacadeMessage => {
		    val jsonStrMessage = x.toJson.compactPrint
		    // Send it to Kafka facadeTopic
		    log.info("Facade Producersent {}", x)
		   	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));
      }
      case _ =>
    }

  
/*
 *    For testing purposes only, we will convert RequestFacadeMessage message to ResponseFacadeMessage one
 *    and receive it in KafkaConsumerActor.
*/     
//    emulateFeedServiseResponse(msg, producer, topic)
    
    producer.close
  }
  
//  private def emulateFeedServiseResponse(msg: RequestFacadeMessage, producer: Producer[Integer, String], topic: String) = {
//    // Emulating Quotes feed stream.
//
//    val messageTraits = Utils.generateMessageTraits
//    var messageNo = 1
//    while(messageNo <= 10) {
//    	val fakeQuote = "%.2f".format(Random.nextFloat()+7)
//	    val fakeMessage = ResponseFacadeMessage(messageTraits._1, msg.asInstanceOf[RequestFacadeMessage].msgType, msg.asInstanceOf[RequestFacadeMessage].client, s"$fakeQuote", messageTraits._2, s"$messageNo")
//	    val jsonStrMessage = fakeMessage.toJson.compactPrint
////	    log.debug("###### KafkaProducerActor converted FacadeOutgoingFeedRequestMessage to JSON: {}", jsonStrMessage)
//    	producer.send(new KeyedMessage[Integer, String](topic, jsonStrMessage));
//    	Thread.sleep(500)
//    	messageNo += 1
//    }    
//  }
  
  
}