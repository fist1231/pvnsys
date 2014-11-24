package com.pvnsys.ttts.engine.mq

import akka.actor.{Actor, ActorRef, ActorLogging, Props, AllForOneStrategy}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
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
  def props(processorActorRef: ActorRef, facadeResponseProcessorActorRef: ActorRef, servicesResponseProcessorActorRef: ActorRef, serviceId: String) = Props(new KafkaServicesTopicConsumerActor(processorActorRef, facadeResponseProcessorActorRef, servicesResponseProcessorActorRef, serviceId))

//  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
//    def genString(o: AnyRef): String = o.getClass.getName
//    override def getClazz(o: AnyRef): Class[_] = o.getClass
//  }
  sealed trait KafkaServicesTopicConsumerActorMessage
  case object StopMessage extends KafkaServicesTopicConsumerActorMessage
}



object KafkaServicesTopicConsumerActorJsonProtocol extends DefaultJsonProtocol {
  import TttsEngineMessages._
  implicit val strategyPayloadFormat = jsonFormat13(StrategyPayload)
  implicit val enginePayloadFormat = jsonFormat15(EnginePayload)
  implicit val responseStrategyServicesTopicMessageFormat = jsonFormat8(ResponseStrategyServicesTopicMessage)
  implicit val requestEngineServicesTopicMessageFormat = jsonFormat7(RequestEngineServicesTopicMessage)
  implicit val responseStrategyFacadeTopicMessageFormat = jsonFormat8(ResponseStrategyFacadeTopicMessage)
}

/**
 * This actor will register itself to consume messages from the Kafka server. 
 */
class KafkaServicesTopicConsumerActor(processorActorRef: ActorRef, facadeResponseProcessorActorRef: ActorRef, servicesResponseProcessorActorRef: ActorRef, serviceId: String) extends Actor with ActorLogging {
  
	import KafkaServicesTopicConsumerActor._
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
		val facadeResponseConsumer = new DefaultKafkaConsumer {
		    override def handleDelivery(message: TttsEngineMessage) = {
		        facadeResponseProcessorActorRef ! message
		    }
		}
		val servicesResponseConsumer = new DefaultKafkaConsumer {
		    override def handleDelivery(message: TttsEngineMessage) = {
		        servicesResponseProcessorActorRef ! message
		    }
		}
		register(consumer, facadeResponseConsumer, servicesResponseConsumer)
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
	
	
	private def register(consumer: DefaultKafkaConsumer, facadeResponseConsumer: DefaultKafkaConsumer, servicesResponseConsumer: DefaultKafkaConsumer): Unit = {

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
					    log.info("Services Consumer got {}", requestEngineServicesTopicMessage)
					    consumer.handleDelivery(requestEngineServicesTopicMessage)
				      }
				      case result if(msgStr.contains(ENGINE_STOP_REQUEST_MESSAGE_TYPE)) => {
				        // 1. Convert string JSON message to correspondent message type
				        val requestEngineServicesTopicMessage = msgJsonObj.convertTo[RequestEngineServicesTopicMessage]
				        // 2. Unregister client id from clients Map
					    clients -= requestEngineServicesTopicMessage.client
					    // 3. Log and handle delivery
					    log.info("Services Consumer got {}", requestEngineServicesTopicMessage)
					    consumer.handleDelivery(requestEngineServicesTopicMessage)
				      }
				      case result if(msgStr.contains(STRATEGY_RESPONSE_MESSAGE_TYPE)) => {
			        	val responseServicesMessage = msgJsonObj.convertTo[ResponseStrategyServicesTopicMessage]
		        	    // Only process STRATEGY_RSP messages intended to this Service instance
		        		if(responseServicesMessage.serviceId equals serviceId) {
				        	// If message is in registered clients map, it was initiated from services topic. Otherwise, request came from Facade Topic 
		        			log.info("Services Consumer got {}", responseServicesMessage)
				        	if(clients contains responseServicesMessage.client) {
				        	    // Here we need to sub serviceId that came from StrategyMS to serviceId of the Microservice that requested EngineMS at the beginning
				        	    val callerServiceId = clients(responseServicesMessage.client).asInstanceOf[RequestEngineServicesTopicMessage].serviceId
				        	    log.debug("!!!!!!!!!!!!!! reassigning STRATEGY serviceId {} to original serviceID {}", responseServicesMessage.serviceId, callerServiceId)
				        	    val reassignedResponseServiceMessage = ResponseStrategyServicesTopicMessage(responseServicesMessage.id, responseServicesMessage.msgType, responseServicesMessage.client, responseServicesMessage.payload, responseServicesMessage.timestamp, responseServicesMessage.sequenceNum, responseServicesMessage.signal, callerServiceId)
		        				servicesResponseConsumer.handleDelivery(reassignedResponseServiceMessage)
		        			} else {
		        				val responseServicesMessage = msgJsonObj.convertTo[ResponseStrategyFacadeTopicMessage]
		        				facadeResponseConsumer.handleDelivery(responseServicesMessage)
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
