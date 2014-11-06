package com.pvnsys.ttts.strategy.mq

import akka.actor.{Actor, ActorRef, ActorLogging, Props, AllForOneStrategy}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.{StartListeningServicesTopicMessage, ServicesTopicMessage, RequestStrategyServicesTopicMessage, TttsStrategyMessage, ResponseFeedServicesTopicMessage}
import kafka.consumer.ConsumerConfig
import java.util.Properties
import kafka.consumer.Consumer
import scala.collection.JavaConversions._
import com.pvnsys.ttts.strategy.Configuration
import akka.actor.SupervisorStrategy.{Restart, Stop}
import spray.json._


object KafkaServicesTopicConsumerActorJsonProtocol extends DefaultJsonProtocol {
  implicit val servicesTopicMessageFormat = jsonFormat7(ServicesTopicMessage)
  implicit val responseFeedServicesTopicMessageFormat = jsonFormat6(ResponseFeedServicesTopicMessage)
  implicit val requestStrategyServicesTopicMessageFormat = jsonFormat6(RequestStrategyServicesTopicMessage)
}

object KafkaServicesTopicConsumerActor {
//  def props(address: InetSocketAddress, groupName: Option[String]) = Props(new KafkaConsumerActor(address, groupName))
  def props(toWhom: ActorRef) = Props(new KafkaServicesTopicConsumerActor(toWhom))
}

/**
 * This actor will register itself to consume messages from the AkkaMQ server. 
 */
class KafkaServicesTopicConsumerActor(toWhom: ActorRef) extends Actor with ActorLogging {
  
	import KafkaServicesTopicConsumerActor._
	import StrategyActor._
	import KafkaServicesTopicConsumerActorJsonProtocol._
	import TttsStrategyMessages._
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("KafkaServicesTopicConsumerActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	private def startListening() = {
		
		val consumer = new DefaultKafkaConsumer {
		    override def handleDelivery(message: TttsStrategyMessage) = {
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

			        if(msgStr.contains(FEED_RESPONSE_MESSAGE_TYPE)) {
			        	val responseServicesMessage = msgJsonObj.convertTo[ResponseFeedServicesTopicMessage]
			        	log.debug("Strategy KafkaServicesTopicConsumerActor received ResponseFeedServicesTopicMessage from Kafka Services Topic: {}", responseServicesMessage)
			        	consumer.handleDelivery(responseServicesMessage)
			        }
			        if(msgStr.contains(STRATEGY_REQUEST_MESSAGE_TYPE)) {
			        	val requestServicesMessage = msgJsonObj.convertTo[RequestStrategyServicesTopicMessage]
			        	log.debug("Strategy KafkaServicesTopicConsumerActor received RequestStrategyServicesTopicMessage from Kafka Services Topic: {}", requestServicesMessage)
			        	consumer.handleDelivery(requestServicesMessage)

				    }
				    
			        
			        
//				    val servicesTopicMessage = msgJsonObj.convertTo[ServicesTopicMessage]
//				    log.debug("KafkaServicesTopicConsumerActor received message from Kafka Services Topic: {}", servicesTopicMessage)
//				    matchRequest(servicesTopicMessage) match {
//				      case Some(servicesTopicMessage) => consumer.handleDelivery(servicesTopicMessage)
//				      case None => "Lets do nothing"
//				    }
		      }
		} catch {
		  case e: Throwable => log.error("Error processing message, stop consuming: " + e)
		}
	  
	}
	
//  private def matchRequest(message: ServicesTopicMessage): Option[ServicesTopicMessage] = message.msgType match {
//  	  case STRATEGY_REQUEST_MESSAGE_TYPE => Some(message)
//  	  case FEED_RESPONSE_MESSAGE_TYPE => Some(message)
////  	  case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => Some(message)
//  	  case _ => {
//  	    log.debug("KafkaServicesTopicConsumerActorJsonProtocol - not Strategy Service request, skipping Kafka message") 
//  	    None
//  	  }
//  }
	
   
	override def postStop() = {}
}
