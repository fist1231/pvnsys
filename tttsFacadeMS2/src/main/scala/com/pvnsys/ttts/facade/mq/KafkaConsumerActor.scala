package com.pvnsys.ttts.facade.mq

import akka.actor.{Props, Actor, ActorLogging, PoisonPill}
import kafka.consumer.ConsumerConfig
import kafka.javaapi.consumer.ConsumerConnector
import java.util.Properties
import kafka.consumer.Consumer
import scala.collection.JavaConversions._
import java.net.InetSocketAddress
import com.pvnsys.ttts.facade.Configuration
import com.pvnsys.ttts.facade.feed.FeedActor
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages
import spray.json._
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.{ResponseFacadeMessage, ResponseStrategyFacadeMessage, ResponseEngineFacadeMessage}

object KafkaConsumerActor {
  sealed trait FacadeConsumerMessage
  case object StopMessage extends FacadeConsumerMessage

  def props(address: InetSocketAddress) = Props(new KafkaConsumerActor(address))
}

object KafkaConsumerActorJsonProtocol extends DefaultJsonProtocol {
  implicit val responseFacadeMessageFormat = jsonFormat6(ResponseFacadeMessage)
  implicit val responseStrategyFacadeMessageFormat = jsonFormat7(ResponseStrategyFacadeMessage)
  implicit val responseEngineFacadeMessageFormat = jsonFormat7(ResponseEngineFacadeMessage)
}

/**
 * This actor will register itself to consume messages from the RabbitMQ server. 
 * At the same time it will play the role of a <code>Producer</code> for our processing <code>Flow</code>.
 */
class KafkaConsumerActor(address: InetSocketAddress) extends Actor with ActorLogging {
  
	import KafkaConsumerActor._
	import KafkaConsumerActorJsonProtocol._
	import TttsFacadeMessages._
	
	val did = scala.util.Random.nextInt(100).toString
	val suff = Configuration.groupIdConsumer
//	val groupId = s"GROUP_ID_$did-$suff"
	val groupId = Configuration.facadeGroupId
    val topic = Configuration.facadeTopic
	  
//	log.debug(s"oooooooooooooooooooooooooooo KafkaConsumerActor, GroupID is: $groupId")
	  
	val prps = new Properties()
	prps.put("group.id", groupId)
	prps.put("socket.buffer.size", Configuration.socketBufferSizeConsumer)
	prps.put("fetch.size", Configuration.fetchSizeConsumer)
	prps.put("auto.commit", Configuration.autoCommitConsumer)
	prps.put("autocommit.interval.ms", Configuration.autocommitIntervalConsumer)
	prps.put("autooffset.reset", Configuration.autooffsetResetConsumer)
	prps.put("zookeeper.connect", Configuration.zookeeperConnectionConsumer)
    val config = new ConsumerConfig(prps)
  
    val connector = Consumer.create(config)
    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
    val maxMessages = -1 //no limit 
 
    try {
      val it = stream.iterator()
      while(it.hasNext) {
        try {
		    val arr = it.next.message
		    val mess = new String(arr, "UTF-8")
		    val msgJsonObj = mess.parseJson
	        val msgStr = msgJsonObj.compactPrint
	        
	        if(msgStr.contains(FEED_RESPONSE_MESSAGE_TYPE)) {
	        	val responseFacadeMessage = msgJsonObj.convertTo[ResponseFacadeMessage]
			    val feedPushActor = context.actorOf(Props(classOf[FeedPushActor]))
			    feedPushActor ! responseFacadeMessage
			    feedPushActor ! StopMessage
		    }
	        if(msgStr.contains(STRATEGY_RESPONSE_MESSAGE_TYPE)) {
	        	val responseFacadeMessage = msgJsonObj.convertTo[ResponseStrategyFacadeMessage]
			    val feedPushActor = context.actorOf(Props(classOf[FeedPushActor]))
			    feedPushActor ! responseFacadeMessage
			    feedPushActor ! StopMessage
		    }
	        if(msgStr.contains(ENGINE_RESPONSE_MESSAGE_TYPE)) {
	        	val responseEngineMessage = msgJsonObj.convertTo[ResponseEngineFacadeMessage]
			    val feedPushActor = context.actorOf(Props(classOf[FeedPushActor]))
			    feedPushActor ! responseEngineMessage
			    feedPushActor ! StopMessage
		    }
		    
//		    val responseFacadeMessage = msgJsonObj.convertTo[ResponseFacadeMessage]
//	        log.debug("KafkaConsumerActor received message from Facade Topic: {}", responseFacadeMessage)
//		    matchRequest(responseFacadeMessage) match {
//		      case Some(responseFacadeMessage) => {
//			    val feedPushActor = context.actorOf(Props(classOf[FeedPushActor]))
//			    feedPushActor ! responseFacadeMessage
//			    feedPushActor ! StopMessage
//		      }
//		      case None => "Lets do nothing"
//		    }
		    
	    } catch {
	      case e: Throwable =>
	        if (false) { // skipMessageOnError = true|false
	          log.error("Error processing message, skipping and resume consumption: " + e)
	        }
	        else {
	          log.error("Error processing message, failing " + e)
	          throw e
	        }
	    }
	    
	  }
	  for(message <- stream) {
	  }
	} catch {
	  case e: Throwable => log.error("Error processing message, stop consuming: " + e)
	}
	    
   override def receive = {
//    case KafkaNewMessage(msg) => 
//      log.debug(s"!!!!!!!!!!!!!!! Rolling KafkaConsumerActor, Gettin message: $msg")
//	  val rand = Seq.fill(5)(scala.util.Random.nextInt(100))
//	  val msg = s"Dummy quote: $rand"
	
	case StopMessage => {
	  self ! PoisonPill
	}
	case mmm => log.error("KafkaConsumerActor Received unknown message: {}", mmm)
  }
	
  override def postStop() = {}
  
  private def matchRequest(message: ResponseFacadeMessage): Option[ResponseFacadeMessage] = message.msgType match {
  	  case FEED_RESPONSE_MESSAGE_TYPE => Some(message)
  	  case STRATEGY_RESPONSE_MESSAGE_TYPE => Some(message)
  	  case ENGINE_RESPONSE_MESSAGE_TYPE => Some(message)
  	  case _ => {
  	    log.info("KafkaConsumerActor - not Facade MQ Response, skipping Kafka message") 
  	    None
  	  }
  }
  
  
}


class FeedPushActor extends Actor with ActorLogging {
	import KafkaConsumerActor._
	import FeedActor._
  
  def receive = {
    case msg: ResponseFacadeMessage => {
	      context.actorSelection("/user/feed") ! msg
	      self ! StopMessage
    }
    case msg: ResponseStrategyFacadeMessage => {
	      context.actorSelection("/user/strategy") ! msg
	      self ! StopMessage
    }
    case msg: ResponseEngineFacadeMessage => {
	      context.actorSelection("/user/engine") ! msg
	      self ! StopMessage
    }
    case StopMessage => {
	  self ! PoisonPill
    }
    
  }
  
}

