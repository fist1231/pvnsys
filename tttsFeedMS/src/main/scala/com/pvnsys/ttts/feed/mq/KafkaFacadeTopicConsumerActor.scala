package com.pvnsys.ttts.feed.mq

import akka.actor.{Actor, ActorRef, ActorLogging, Props, AllForOneStrategy}
import com.pvnsys.ttts.feed.messages.TttsFeedMessages
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.{StartListeningFacadeTopicMessage, FacadeTopicMessage, RequestFeedFacadeTopicMessage, TttsFeedMessage}
import kafka.consumer.ConsumerConfig
import java.util.Properties
import kafka.consumer.Consumer
import scala.collection.JavaConversions._
import com.pvnsys.ttts.feed.Configuration
import akka.actor.SupervisorStrategy.{Restart, Stop}
import spray.json._


object KafkaFacadeTopicConsumerActorJsonProtocol extends DefaultJsonProtocol {
  implicit val facadeTopicMessageFormat = jsonFormat6(FacadeTopicMessage)
}

object KafkaFacadeTopicConsumerActor {
//  def props(address: InetSocketAddress, groupName: Option[String]) = Props(new KafkaConsumerActor(address, groupName))
  def props(toWhom: ActorRef) = Props(new KafkaFacadeTopicConsumerActor(toWhom))
}

/**
 * This actor will register itself to consume messages from the AkkaMQ server. 
 */
class KafkaFacadeTopicConsumerActor(toWhom: ActorRef) extends Actor with ActorLogging {
  
	import KafkaFacadeTopicConsumerActor._
	import FeedActor._
	import context._
	import KafkaFacadeTopicConsumerActorJsonProtocol._
	import TttsFeedMessages._
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("KafkaFacadeTopicConsumerActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	
	private def startListening() = {
		
		val consumer = new DefaultKafkaConsumer {
		    override def handleDelivery(message: TttsFeedMessage) = {
		        toWhom ! message
		    }
		}
		register(consumer)
	}

	
	
	override def receive = {
		case StopMessage => {
			log.debug("KafkaFacadeTopicConsumerActor StopMessage")
			//self ! PoisonPill
		}
		case StartListeningFacadeTopicMessage => {
			log.debug(s"Start Listening in KafkaFacadeTopicConsumerActor")

			startListening()
		}

		case _ => log.error("KafkaFacadeTopicConsumerActor Received unknown message")
	}
	
	
	private def register(consumer: DefaultKafkaConsumer): Unit = {

		val groupId = Configuration.facadeGroupId
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
	    val topic = Configuration.facadeTopic 
	    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
	    val maxMessages = -1 //no limit 
	 
	    try {
		      stream map {arr =>
				    val mess = new String(arr.message, "UTF-8")
				    val msgJsonObj = mess.parseJson
			        val msgStr = msgJsonObj.compactPrint
				    
				    val facadeTopicMessage = msgJsonObj.convertTo[FacadeTopicMessage]
				    log.debug("KafkaFacadeTopicConsumerActor received message from Kafka Facade Topic: {}", facadeTopicMessage)
				    matchRequest(facadeTopicMessage) match {
				      case Some(facadeTopicMessage) => consumer.handleDelivery(facadeTopicMessage)
				      case None => "Lets do nothing"
				    }
		      }
		} catch {
		  case e: Throwable => log.error("Error processing message, stop consuming: " + e)
		}
	  
	}
	
	private def matchRequest(message: FacadeTopicMessage): Option[FacadeTopicMessage] = message.msgType match {
		case FEED_REQUEST_MESSAGE_TYPE => Some(message)
		case FEED_STOP_REQUEST_MESSAGE_TYPE => Some(message)
		case _ => {
			log.debug("KafkaFacadeTopicConsumerActorJsonProtocol - not Feed Service request, skipping Kafka message") 
			None
		}
	}
	
   
	override def postStop() = {}
}
