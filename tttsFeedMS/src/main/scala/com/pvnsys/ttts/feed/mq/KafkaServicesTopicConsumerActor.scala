package com.pvnsys.ttts.feed.mq

import akka.actor.{Actor, ActorRef, ActorLogging, Props, AllForOneStrategy}
import com.pvnsys.ttts.feed.messages.TttsFeedMessages
import kafka.consumer.ConsumerConfig
import java.util.Properties
import kafka.consumer.Consumer
import scala.collection.JavaConversions._
import com.pvnsys.ttts.feed.Configuration
import akka.actor.SupervisorStrategy.{Restart, Stop}
import spray.json._


object KafkaServicesTopicConsumerActor {
  def props(toWhom: ActorRef) = Props(new KafkaServicesTopicConsumerActor(toWhom))
}

object KafkaServicesTopicConsumerActorJsonProtocol extends DefaultJsonProtocol {
  import TttsFeedMessages._
  implicit val feedPayloadFormat = jsonFormat10(FeedPayload)
  implicit val servicesTopicMessageFormat = jsonFormat7(ServicesTopicMessage)
}


/**
 * This actor will register itself to consume messages from the AkkaMQ server. 
 */
class KafkaServicesTopicConsumerActor(toWhom: ActorRef) extends Actor with ActorLogging {
  
	import KafkaServicesTopicConsumerActor._
	import FeedActor._
	import KafkaServicesTopicConsumerActorJsonProtocol._
	import TttsFeedMessages._
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("KafkaServicesTopicConsumerActor Unexpected failure: {}", e.getMessage)
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
			log.debug("KafkaServicesTopicConsumerActor StopMessage")
		}
		case StartListeningServicesTopicMessage => {
			log.debug(s"Start Listening in KafkaServicesTopicConsumerActor")
			startListening()
		}
		case _ => log.error("KafkaServicesTopicConsumerActor Received unknown message")
	}
	
	
	private def register(consumer: DefaultKafkaConsumer): Unit = {

	    val topic = Configuration.servicesTopic 
		val groupId = Configuration.servicesGroupId 

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
		      stream map {arr =>
				    val mess = new String(arr.message, "UTF-8")
				    val msgJsonObj = mess.parseJson
			        val msgStr = msgJsonObj.compactPrint
				    
				    val servicesTopicMessage = msgJsonObj.convertTo[ServicesTopicMessage]
				    matchRequest(servicesTopicMessage) match {
				      case Some(servicesTopicMessage) => {
				    	log.info("Services Consumer got {}", servicesTopicMessage)
				        consumer.handleDelivery(servicesTopicMessage)
				      }
				      case None => "Lets do nothing"
				    }
		      }
		} catch {
		  case e: Throwable => log.error("Error processing message, stop consuming: " + e)
		}
	  
	}
	
  private def matchRequest(message: ServicesTopicMessage): Option[ServicesTopicMessage] = message.msgType match {
  	  case FEED_REQUEST_MESSAGE_TYPE => Some(message)
  	  case FEED_STOP_REQUEST_MESSAGE_TYPE => Some(message)
  	  case _ => {
//  	    log.debug("KafkaServicesTopicConsumerActor - not Feed Service request, skipping Kafka message") 
  	    None
  	  }
  }
	
   
	override def postStop() = {}
}
