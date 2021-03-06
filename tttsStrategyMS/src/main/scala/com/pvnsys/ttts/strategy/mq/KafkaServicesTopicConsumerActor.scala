package com.pvnsys.ttts.strategy.mq

import akka.actor.{Actor, ActorRef, ActorLogging, Props, AllForOneStrategy}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import kafka.consumer.ConsumerConfig
import java.util.Properties
import kafka.consumer.Consumer
import scala.collection.JavaConversions._
import com.pvnsys.ttts.strategy.Configuration
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
  import TttsStrategyMessages._
  implicit val feedPayloadFormat = jsonFormat10(FeedPayload)
  implicit val strategyPayloadFormat = jsonFormat13(StrategyPayload)
  implicit val responseFeedServicesTopicMessageFormat = jsonFormat7(ResponseFeedServicesTopicMessage)
  implicit val requestStrategyServicesTopicMessageFormat = jsonFormat7(RequestStrategyServicesTopicMessage)
  implicit val responseFeedFacadeTopicMessageFormat = jsonFormat7(ResponseFeedFacadeTopicMessage)
}

/**
 * This actor will register itself to consume messages from the AkkaMQ server. 
 */
class KafkaServicesTopicConsumerActor(processorActorRef: ActorRef, facadeResponseProcessorActorRef: ActorRef, servicesResponseProcessorActorRef: ActorRef, serviceId: String) extends Actor with ActorLogging {
  
	import KafkaServicesTopicConsumerActor._
	import KafkaServicesTopicConsumerActorJsonProtocol._
	import TttsStrategyMessages._
	
//	override val log = Logging(context.system, this)	
    
	override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("KafkaServicesTopicConsumerActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	private def startListening() = {
		
		val consumer = new DefaultKafkaConsumer {
		    override def handleDelivery(message: TttsStrategyMessage) = {
		        processorActorRef ! message
		    }
		}
		val facadeResponseConsumer = new DefaultKafkaConsumer {
		    override def handleDelivery(message: TttsStrategyMessage) = {
		        facadeResponseProcessorActorRef ! message
		    }
		}
		val servicesResponseConsumer = new DefaultKafkaConsumer {
		    override def handleDelivery(message: TttsStrategyMessage) = {
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

	    val clients = mutable.Map[String, TttsStrategyMessage]()
	    
	    try {
		      stream map {arr =>
				    val mess = new String(arr.message, "UTF-8")
				    val msgJsonObj = mess.parseJson
			        val msgStr = msgJsonObj.compactPrint


				    log.info("KafkaServicesTopicConsumerActor register received JSON msg from Kafka Services Topic: {}", msgStr)
				    
			        
					/*
					 * KafkaGenericConsumerActor listens for three message types from Services Topic: 
					 * 1. STRATEGY_REQUEST_MESSAGE_TYPE of RequestStrategyServicesTopicMessage
					 * 2. STRATEGY_STOP_REQUEST_MESSAGE_TYPE of RequestStrategyServicesTopicMessage
					 * 3. FEED_RESPONSE_MESSAGE_TYPE of ResponseFeedServicesTopicMessage
					 */ 
			        msgStr match {
				      case result if(msgStr.contains(STRATEGY_REQUEST_MESSAGE_TYPE)) => {
				        // 1. Convert string JSON message to correspondent message type
				        val requestStrategyServicesTopicMessage = msgJsonObj.convertTo[RequestStrategyServicesTopicMessage]
				        // 2. Register client id and topic to the map (WebSocket address -> topic)
					    clients += (requestStrategyServicesTopicMessage.client -> requestStrategyServicesTopicMessage)
					    // 3. Log and handle delivery
					    log.info("Services Consumer got {}", requestStrategyServicesTopicMessage)
					    consumer.handleDelivery(requestStrategyServicesTopicMessage)
				      }
				      case result if(msgStr.contains(STRATEGY_STOP_REQUEST_MESSAGE_TYPE)) => {
				        // 1. Convert string JSON message to correspondent message type
				        val requestStrategyServicesTopicMessage = msgJsonObj.convertTo[RequestStrategyServicesTopicMessage]
				        // 2. Unregister client id from clients Map
					    clients -= requestStrategyServicesTopicMessage.client
					    // 3. Log and handle delivery
					    log.info("Services Consumer got {}", requestStrategyServicesTopicMessage)
					    consumer.handleDelivery(requestStrategyServicesTopicMessage)
				      }
				      case result if(msgStr.contains(FEED_RESPONSE_MESSAGE_TYPE)) => {
			        	val responseServicesMessage = msgJsonObj.convertTo[ResponseFeedServicesTopicMessage]
		        	    // Only process FEED_RSP messages intended to this Service instance
		        		if(responseServicesMessage.serviceId equals serviceId) {
				        	// If message is in registered clients map, it was initiated from services topic. Otherwise, request came from Facade Topic 
		        			log.info("Services Consumer got {}", responseServicesMessage)
				        	if(clients contains responseServicesMessage.client) {
				        	    // Here we need to sub serviceId that came from FeedMS to serviceId of the Microservice that requested StrategyMS at the beginning
				        	    val callerServiceId = clients(responseServicesMessage.client).asInstanceOf[RequestStrategyServicesTopicMessage].serviceId
				        	    log.debug("!!!!!!!!!!!!!! reassigning FEED serviceId {} to original serviceID {}", responseServicesMessage.serviceId, callerServiceId)
				        	    val reassignedResponseServiceMessage = ResponseFeedServicesTopicMessage(responseServicesMessage.id, responseServicesMessage.msgType, responseServicesMessage.client, responseServicesMessage.payload, responseServicesMessage.timestamp, responseServicesMessage.sequenceNum, callerServiceId)
		        				servicesResponseConsumer.handleDelivery(reassignedResponseServiceMessage)
		        			} else {
		        				val responseServicesMessage = msgJsonObj.convertTo[ResponseFeedFacadeTopicMessage]
		        				facadeResponseConsumer.handleDelivery(responseServicesMessage)
		        			}
			        	}
				      }
				      case _ => log.debug("KafkaServicesTopicConsumerActor skipping UNKNOWN message type from Kafka Services Topic: {}", msgStr)
				    }
			        
			        
//				    val servicesTopicMessage = msgJsonObj.convertTo[ServicesTopicMessage]
//				    log.debug("KafkaServicesTopicConsumerActor received message from Kafka Services Topic: {}", servicesTopicMessage)
//				    matchRequest(servicesTopicMessage) match {
//				      case Some(servicesTopicMessage) => consumer.handleDelivery(servicesTopicMessage)
//				      case None => "Lets do nothing"
//				    }
		      }
		} catch {
		  case e: Throwable => log.error("KafkaServicesTopicConsumerActor Error processing message, stop consuming: " + e)
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
