package com.pvnsys.ttts.engine.mq

import akka.actor.{Actor, ActorRef, ActorLogging, Props, AllForOneStrategy}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import com.pvnsys.ttts.engine.messages.TttsEngineMessages.{StartListeningServicesTopicMessage, ServicesTopicMessage, RequestEngineServicesTopicMessage, RequestStrategyServicesTopicMessage, TttsEngineMessage, ResponseStrategyServicesTopicMessage, ResponseStrategyFacadeTopicMessage}
import kafka.consumer.ConsumerConfig
import java.util.Properties
import kafka.consumer.Consumer
import scala.collection.JavaConversions._
import com.pvnsys.ttts.engine.Configuration
import akka.actor.SupervisorStrategy.{Restart, Stop}
import spray.json._
import scala.collection.mutable
import scala.collection.mutable.Map


object KafkaServicesTopicConsumerActor {
//  def props(address: InetSocketAddress, groupName: Option[String]) = Props(new KafkaConsumerActor(address, groupName))
  def props(processorActorRef: ActorRef, serviceId: String) = Props(new KafkaServicesTopicConsumerActor(processorActorRef, serviceId))

//  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
//    def genString(o: AnyRef): String = o.getClass.getName
//    override def getClazz(o: AnyRef): Class[_] = o.getClass
//  }
}


object KafkaServicesTopicConsumerActorJsonProtocol extends DefaultJsonProtocol {
  implicit val responseStrategyServicesTopicMessageFormat = jsonFormat8(ResponseStrategyServicesTopicMessage)
  implicit val requestEngineServicesTopicMessageFormat = jsonFormat7(RequestEngineServicesTopicMessage)
  implicit val responseStrategyFacadeTopicMessageFormat = jsonFormat8(ResponseStrategyFacadeTopicMessage)
}

/**
 * This actor will register itself to consume messages from the Kafka server. 
 */
class KafkaServicesTopicConsumerActor(processorActorRef: ActorRef, serviceId: String) extends Actor with ActorLogging {
  
	import KafkaServicesTopicConsumerActor._
	import EngineActor._
	import KafkaServicesTopicConsumerActorJsonProtocol._
	import TttsEngineMessages._
	
//	override val log = Logging(context.system, this)	
    
	override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("KafkaServicesTopicConsumerActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	private def startListening() = {
		
		val consumer = new DefaultKafkaConsumer {
		    override def handleDelivery(message: TttsEngineMessage) = {
		        processorActorRef ! message
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
//			log.debug(template, arg1, arg2, arg3, arg4)
			startListening()
		}
		case m => log.error("KafkaServicesTopicConsumerActor Received unknown message: {}", m)
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

	    val clients = mutable.Map[String, TttsEngineMessage]()
	    
	    try {
		      stream map {arr =>
				    val mess = new String(arr.message, "UTF-8")
				    val msgJsonObj = mess.parseJson
			        val msgStr = msgJsonObj.compactPrint


				    log.debug("KafkaServicesTopicConsumerActor register received JSON msg from Kafka Services Topic: {}", msgStr)
				    
			        
					/*
					 * KafkaGenericConsumerActor listens for three message types from Services Topic: 
					 * 1. ENGINE_REQUEST_MESSAGE_TYPE of RequestEngineServicesTopicMessage
					 * 2. ENGINE_STOP_REQUEST_MESSAGE_TYPE of RequestEngineServicesTopicMessage
					 * 3. STRATEGY_RESPONSE_MESSAGE_TYPE of ResponseStrategyServicesTopicMessage
					 */ 
			        msgStr match {
				      case result if(msgStr.contains(ENGINE_REQUEST_MESSAGE_TYPE)) => {
				        // 1. Convert string JSON message to correspondent message type
				        val requestEngineServicesTopicMessage = msgJsonObj.convertTo[RequestEngineServicesTopicMessage]
				        // 2. Register client id and topic to the map (WebSocket address -> topic)
					    clients += (requestEngineServicesTopicMessage.client -> requestEngineServicesTopicMessage)
					    // 3. Log and handle delivery
					    log.debug("KafkaServicesTopicConsumerActor received ENGINE_REQUEST_MESSAGE_TYPE from Kafka Services Topic: {}", requestEngineServicesTopicMessage)
					    consumer.handleDelivery(requestEngineServicesTopicMessage)
				      }
				      case result if(msgStr.contains(ENGINE_STOP_REQUEST_MESSAGE_TYPE)) => {
				        // 1. Convert string JSON message to correspondent message type
				        val requestEngineServicesTopicMessage = msgJsonObj.convertTo[RequestEngineServicesTopicMessage]
				        // 2. Unregister client id from clients Map
					    clients -= requestEngineServicesTopicMessage.client
					    // 3. Log and handle delivery
					    log.debug("KafkaServicesTopicConsumerActor received ENGINE_STOP_REQUEST_MESSAGE_TYPE from Kafka Services Topic: {}", requestEngineServicesTopicMessage)
					    consumer.handleDelivery(requestEngineServicesTopicMessage)
				      }
				      case result if(msgStr.contains(STRATEGY_RESPONSE_MESSAGE_TYPE)) => {
			        	val responseServicesMessage = msgJsonObj.convertTo[ResponseStrategyServicesTopicMessage]
			        	log.debug("KafkaServicesTopicConsumerActor received STRATEGY_RESPONSE_MESSAGE_TYPE from Kafka Services Topic: {}", responseServicesMessage)
		        	    // Only process STRATEGY_RSP messages intended to this Service instance
		        		if(responseServicesMessage.serviceId equals serviceId) {
				        	// If message is in registered clients map, it was initiated from services topic. Otherwise, request came from Facade Topic 
				        	if(clients contains responseServicesMessage.client) {
		        				consumer.handleDelivery(responseServicesMessage)
		        			} else {
		        			    // This STRATEGY_RESP response intended for the FacadeMS
		        				val responseServicesMessage = msgJsonObj.convertTo[ResponseStrategyFacadeTopicMessage]
		        				consumer.handleDelivery(responseServicesMessage)
		        			}
			        	}
				      }
				      case _ => log.debug("KafkaServicesTopicConsumerActor skipping UNKNOWN message type from Kafka Services Topic: {}", msgStr)
				    }
		      }
		} catch {
		  case e: Throwable => log.error("KafkaServicesTopicConsumerActor Error processing message, stop consuming: " + e)
		}
	  
	}
	
	override def postStop() = {}
}
