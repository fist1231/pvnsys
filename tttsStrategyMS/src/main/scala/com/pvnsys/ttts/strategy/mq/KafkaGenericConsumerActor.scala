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
//import akka.event.LogSource
//import akka.event.Logging


object KafkaGenericConsumerActor {
//  def props(address: InetSocketAddress, groupName: Option[String]) = Props(new KafkaConsumerActor(address, groupName))
//  def props(toWhom: ActorRef) = Props(new KafkaFacadeTopicConsumerActor(toWhom))

//  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
//    def genString(o: AnyRef): String = o.getClass.getName
//    override def getClazz(o: AnyRef): Class[_] = this.getClazz(o)
//  }
}


object KafkaGenericConsumerActorJsonProtocol extends DefaultJsonProtocol {
  
  import TttsStrategyMessages._
  implicit val requestStrategyFacadeTopicMessageFormat = jsonFormat6(RequestStrategyFacadeTopicMessage)

//  implicit val servicesTopicMessageFormat = jsonFormat7(ServicesTopicMessage)
  implicit val responseFeedServicesTopicMessageFormat = jsonFormat6(ResponseFeedServicesTopicMessage)
  implicit val requestStrategyServicesTopicMessageFormat = jsonFormat6(RequestStrategyServicesTopicMessage)

}


/**
 * This actor will register itself to consume messages from the Kafka server. 
 */
class KafkaGenericConsumerActor extends Actor with ActorLogging {
  
	import KafkaGenericConsumerActor._
	import StrategyActor._
	import context._
	import KafkaGenericConsumerActorJsonProtocol._
	import TttsStrategyMessages._

	val clients = mutable.Map[String, String]()
	
//    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
//    case e: Exception =>
//      log.error("KafkaGenericConsumerActor Unexpected failure: {}", e.getMessage)
//      Restart
//  	}
	
	
	private def startListening(toWhom: ActorRef, topic: String, flowType: Int) = {
		val consumer = new DefaultKafkaConsumer {
		    override def handleDelivery(message: TttsStrategyMessage) = {
		        toWhom ! message
		    }
		}
		flowType match {
		  case 1 => registerFacadeStrategy(topic, consumer)
		  case 2 => registerServicesStrategy(topic, consumer)
		  case 3 => registerServicesToFacadeFeed(topic, consumer)
		  case 4 => registerServicesToServicesFeed(topic, consumer)
		}
//		register(topic, consumer)
	}

	
	
	override def receive = {
		case StopMessage => {
			log.debug("KafkaGenericConsumerActor StopMessage")
			//self ! PoisonPill
		}
		case msg: StartListeningStrategyRequestFlowFacadeTopicMessage => {
			log.debug("KafkaGenericConsumerActor StartListeningFacadeTopicMessage")
			val topic = Configuration.facadeTopic 
			startListening(msg.actorRef, topic, 1)
		}

		case msg: StartListeningStrategyRequestFlowServicesTopicMessage => {
			log.debug("KafkaGenericConsumerActor StartListeningServicesTopicMessage")
			val topic = Configuration.servicesTopic 
			startListening(msg.actorRef, topic, 2)
		}

		case msg: StartListeningFeedResponseToFacadeFlowServicesTopicMessage => {
			log.debug("KafkaGenericConsumerActor StartListeningServicesTopicMessage")
			val topic = Configuration.servicesTopic 
			startListening(msg.actorRef, topic, 3)
		}

		case msg: StartListeningFeedResponseToServicesFlowServicesTopicMessage => {
			log.debug("KafkaGenericConsumerActor StartListeningServicesTopicMessage")
			val topic = Configuration.servicesTopic 
			startListening(msg.actorRef, topic, 4)
		}
		
		case _ => log.error("KafkaGenericConsumerActor received unknown message")
	}
	
	
//	private def register(topic: String, consumer: DefaultKafkaConsumer): Unit = {
//
//	    val groupId = Configuration.facadeGroupId
//		val prps = new Properties()
//		prps.put("group.id", groupId)
//		prps.put("socket.buffer.size", Configuration.socketBufferSizeConsumer)
//		prps.put("fetch.size", Configuration.fetchSizeConsumer)
//		prps.put("auto.commit", Configuration.autoCommitConsumer)
//		prps.put("autocommit.interval.ms", Configuration.autocommitIntervalConsumer)
//		prps.put("autooffset.reset", Configuration.autooffsetResetConsumer)
//		prps.put("zookeeper.connect", Configuration.zookeeperConnectionConsumer)
//	
//		val config = new ConsumerConfig(prps)
//	    val connector = Consumer.create(config)
////	    val topic = Configuration.facadeTopic 
//	    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
//	    val maxMessages = -1 //no limit 
//	 
//	    try {
//		      stream map {arr =>
//				    val mess = new String(arr.message, "UTF-8")
//				    val msgJsonObj = mess.parseJson
//			        val msgStr = msgJsonObj.compactPrint
//				    
//					/*
//					 * KafkaServicesTopicConsumerActor listens for only three message types: 
//					 * 1. FEED_RESPONSE_MESSAGE_TYPE of ResponseFeedServicesTopicMessage
//					 * 2. STRATEGY_REQUEST_MESSAGE_TYPE of RequestStrategyServicesTopicMessage
//					 * 3. STRATEGY_STOP_REQUEST_MESSAGE_TYPE of RequestStrategyServicesTopicMessage
//					 */ 
//			        
//			        topic match {
//					/*
//					 * KafkaGenericConsumerActor listens for two message types from Facade Topic: 
//					 * 1. STRATEGY_REQUEST_MESSAGE_TYPE of RequestStrategyFacadeTopicMessage of FacadeTopicMessage
//					 * 2. STRATEGY_STOP_REQUEST_MESSAGE_TYPE of RequestStrategyFacadeTopicMessage of FacadeTopicMessage
//					 */ 
//				      case Configuration.facadeTopic =>
//					        msgStr match {
//						      case result if(msgStr.contains(STRATEGY_REQUEST_MESSAGE_TYPE)) => {
//						        // 1. Convert string JSON message to correspondent message type
//						        val requestStrategyFacadeTopicMessage = msgJsonObj.convertTo[RequestStrategyFacadeTopicMessage]
//						        // 2. Register client id and topic to the map (WebSocket address -> topic)
//       						    clients += (requestStrategyFacadeTopicMessage.client -> topic)
//       						    // 3. Log and handle delivery
//       						    log.debug("KafkaGenericConsumerActor received STRATEGY_REQUEST_MESSAGE_TYPE from Kafka Facade Topic: {}", requestStrategyFacadeTopicMessage)
//       						    consumer.handleDelivery(requestStrategyFacadeTopicMessage)
//						      }
//						      case result if(msgStr.contains(STRATEGY_STOP_REQUEST_MESSAGE_TYPE)) => {
//						        // 1. Convert string JSON message to correspondent message type
//						        val requestStrategyFacadeTopicMessage = msgJsonObj.convertTo[RequestStrategyFacadeTopicMessage]
//						        // 2. Unregister client id from clients Map
//       						    clients -= requestStrategyFacadeTopicMessage.client
//       						    // 3. Log and handle delivery
//       						    log.debug("KafkaGenericConsumerActor received STRATEGY_STOP_REQUEST_MESSAGE_TYPE from Kafka Facade Topic: {}", requestStrategyFacadeTopicMessage)
//       						    consumer.handleDelivery(requestStrategyFacadeTopicMessage)
//						      }
//						      case _ =>
//						    }
//					/*
//					 * KafkaGenericConsumerActor listens for three message types from Services Topic: 
//					 * 1. STRATEGY_REQUEST_MESSAGE_TYPE of RequestStrategyServicesTopicMessage
//					 * 2. STRATEGY_STOP_REQUEST_MESSAGE_TYPE of RequestStrategyServicesTopicMessage
//					 * 3. FEED_RESPONSE_MESSAGE_TYPE of ResponseFeedServicesTopicMessage
//					 */ 
//				      case Configuration.servicesTopic =>
//					        msgStr match {
//						      case result if(msgStr.contains(STRATEGY_REQUEST_MESSAGE_TYPE)) => {
//						        // 1. Convert string JSON message to correspondent message type
//						        val requestStrategyServicesTopicMessage = msgJsonObj.convertTo[RequestStrategyServicesTopicMessage]
//						        // 2. Register client id and topic to the map (WebSocket address -> topic)
//       						    clients += (requestStrategyServicesTopicMessage.client -> topic)
//       						    // 3. Log and handle delivery
//       						    log.debug("KafkaGenericConsumerActor received STRATEGY_REQUEST_MESSAGE_TYPE from Kafka Services Topic: {}", requestStrategyServicesTopicMessage)
//       						    consumer.handleDelivery(requestStrategyServicesTopicMessage)
//						      }
//						      case result if(msgStr.contains(STRATEGY_STOP_REQUEST_MESSAGE_TYPE)) => {
//						        // 1. Convert string JSON message to correspondent message type
//						        val requestStrategyServicesTopicMessage = msgJsonObj.convertTo[RequestStrategyServicesTopicMessage]
//						        // 2. Unregister client id from clients Map
//       						    clients -= requestStrategyServicesTopicMessage.client
//       						    // 3. Log and handle delivery
//       						    log.debug("KafkaGenericConsumerActor received STRATEGY_STOP_REQUEST_MESSAGE_TYPE from Kafka Services Topic: {}", requestStrategyServicesTopicMessage)
//       						    consumer.handleDelivery(requestStrategyServicesTopicMessage)
//						      }
//						      case result if(msgStr.contains(FEED_RESPONSE_MESSAGE_TYPE)) => {
//					        	val responseServicesMessage = msgJsonObj.convertTo[ResponseFeedServicesTopicMessage]
//					        	log.debug("KafkaServicesTopicConsumerActor received FEED_RESPONSE_MESSAGE_TYPE from Kafka Services Topic: {}", responseServicesMessage)
//					        	// Only accept FEED_RSP for registered clients
//					        	if(clients contains responseServicesMessage.client) {
//					        		consumer.handleDelivery(responseServicesMessage)
//					        	}
//						      }
//						      case _ =>
//						    }
//				    }
//			        
//		      }
//		} catch {
//		  case e: Throwable => log.error("KafkaGenericConsumerActor Error processing message, stop consuming: " + e)
//		}
//	  
//	}

	private def registerFacadeStrategy(topic: String, consumer: DefaultKafkaConsumer): Unit = {

        log.debug("!!!!!!!! registerFacadeStrategy")
//	    val groupId = Configuration.facadeGroupId
	    val groupId = s"${Configuration.facadeGroupId}-registerFacadeStrategy"
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
//	    val topic = Configuration.facadeTopic 
	    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
	    val maxMessages = -1 //no limit 
	 
	    try {
		      stream map {arr =>
				    val mess = new String(arr.message, "UTF-8")
				    val msgJsonObj = mess.parseJson
			        val msgStr = msgJsonObj.compactPrint
				    log.debug("registerFacadeStrategy received JSON msg from Kafka Facade Topic: {}", msgStr)
				    
			        msgStr match {
				      case result if(msgStr.contains(STRATEGY_REQUEST_MESSAGE_TYPE)) => {
				        // 1. Convert string JSON message to correspondent message type
				        val requestStrategyFacadeTopicMessage = msgJsonObj.convertTo[RequestStrategyFacadeTopicMessage]
				        // 2. Register client id and topic to the map (WebSocket address -> topic)
					    clients += (requestStrategyFacadeTopicMessage.client -> topic)
					    // 3. Log and handle delivery
					    log.debug("KafkaGenericConsumerActor received STRATEGY_REQUEST_MESSAGE_TYPE from Kafka Facade Topic: {}", requestStrategyFacadeTopicMessage)
					    consumer.handleDelivery(requestStrategyFacadeTopicMessage)
				      }
				      case result if(msgStr.contains(STRATEGY_STOP_REQUEST_MESSAGE_TYPE)) => {
				        // 1. Convert string JSON message to correspondent message type
				        val requestStrategyFacadeTopicMessage = msgJsonObj.convertTo[RequestStrategyFacadeTopicMessage]
				        // 2. Unregister client id from clients Map
					    clients -= requestStrategyFacadeTopicMessage.client
					    // 3. Log and handle delivery
					    log.debug("KafkaGenericConsumerActor received STRATEGY_STOP_REQUEST_MESSAGE_TYPE from Kafka Facade Topic: {}", requestStrategyFacadeTopicMessage)
					    consumer.handleDelivery(requestStrategyFacadeTopicMessage)
				      }
				      case _ =>
				    }
			        
		      }
		} catch {
		  case e: Throwable => log.error("KafkaGenericConsumerActor registerFacadeStrategy Error processing message, stop consuming: " + e)
		}
	  
	}
	
	private def registerServicesStrategy(topic: String, consumer: DefaultKafkaConsumer): Unit = {

        log.debug("!!!!!!!! registerServicesStrategy")
//	    val groupId = Configuration.facadeGroupId
	    val groupId = s"${Configuration.facadeGroupId}-registerServicesStrategy"
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
//	    val topic = Configuration.facadeTopic 
	    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
	    val maxMessages = -1 //no limit 
	 
	    try {
		      stream map {arr =>
				    val mess = new String(arr.message, "UTF-8")
				    val msgJsonObj = mess.parseJson
			        val msgStr = msgJsonObj.compactPrint
				    log.debug("registerServicesStrategy received JSON msg from Kafka Services Topic: {}", msgStr)
				    
			        
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
					    clients += (requestStrategyServicesTopicMessage.client -> topic)
					    // 3. Log and handle delivery
					    log.debug("KafkaGenericConsumerActor received STRATEGY_REQUEST_MESSAGE_TYPE from Kafka Services Topic: {}", requestStrategyServicesTopicMessage)
					    consumer.handleDelivery(requestStrategyServicesTopicMessage)
				      }
				      case result if(msgStr.contains(STRATEGY_STOP_REQUEST_MESSAGE_TYPE)) => {
				        // 1. Convert string JSON message to correspondent message type
				        val requestStrategyServicesTopicMessage = msgJsonObj.convertTo[RequestStrategyServicesTopicMessage]
				        // 2. Unregister client id from clients Map
					    clients -= requestStrategyServicesTopicMessage.client
					    // 3. Log and handle delivery
					    log.debug("KafkaGenericConsumerActor received STRATEGY_STOP_REQUEST_MESSAGE_TYPE from Kafka Services Topic: {}", requestStrategyServicesTopicMessage)
					    consumer.handleDelivery(requestStrategyServicesTopicMessage)
				      }
				      case _ =>
				    }
			        
		      }
		} catch {
		  case e: Throwable => log.error("KafkaGenericConsumerActor registerServicesStrategy Error processing message, stop consuming: " + e)
		}
	  
	}

	private def registerServicesToFacadeFeed(topic: String, consumer: DefaultKafkaConsumer): Unit = {

        log.debug("!!!!!!!! registerServicesToFacadeFeed")
//	    val groupId = Configuration.facadeGroupId
	    val groupId = s"${Configuration.facadeGroupId}-registerServicesToFacadeFeed"
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
//	    val topic = Configuration.facadeTopic 
	    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
	    val maxMessages = -1 //no limit 
	 
	    try {
		      stream map {arr =>
				    val mess = new String(arr.message, "UTF-8")
				    val msgJsonObj = mess.parseJson
			        val msgStr = msgJsonObj.compactPrint
				    log.debug("registerServicesToFacadeFeed received JSON msg from Kafka Services Topic: {}", msgStr)
				    
			        msgStr match {
				      case result if(msgStr.contains(FEED_RESPONSE_MESSAGE_TYPE)) => {
			        	val responseServicesMessage = msgJsonObj.convertTo[ResponseFeedServicesTopicMessage]
			        	log.debug("KafkaServicesTopicConsumerActor received FEED_RESPONSE_MESSAGE_TYPE from Kafka Services Topic: {}", responseServicesMessage)
			        	// Only accept FEED_RSP for registered clients
			        	if(clients contains responseServicesMessage.client) {
			        	    // Only process FEED_RSP messages to Facade clients
			        		if(clients(responseServicesMessage.client) equals Configuration.facadeTopic) {
			        			consumer.handleDelivery(responseServicesMessage)
			        		}
			        	}
				      }
				      case _ =>
				    }
			        
		      }
		} catch {
		  case e: Throwable => log.error("KafkaGenericConsumerActor registerServicesToFacadeFeed Error processing message, stop consuming: " + e)
		}
	  
	}

	private def registerServicesToServicesFeed(topic: String, consumer: DefaultKafkaConsumer): Unit = {

        log.debug("!!!!!!!! registerServicesToServicesFeed")
	    val groupId = s"${Configuration.facadeGroupId}-registerServicesToServicesFeed"
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
//	    val topic = Configuration.facadeTopic 
	    val stream = connector.createMessageStreams(Map(topic -> 1)).get(topic).get.get(0)
	    val maxMessages = -1 //no limit 
	 
	    try {
		      stream map {arr =>
				    val mess = new String(arr.message, "UTF-8")
				    val msgJsonObj = mess.parseJson
			        val msgStr = msgJsonObj.compactPrint
				    log.debug("registerServicesToServicesFeed received JSON msg from Kafka Services Topic: {}", msgStr)
				    
			        msgStr match {
				      case result if(msgStr.contains(FEED_RESPONSE_MESSAGE_TYPE)) => {
			        	val responseServicesMessage = msgJsonObj.convertTo[ResponseFeedServicesTopicMessage]
			        	log.debug("KafkaServicesTopicConsumerActor received FEED_RESPONSE_MESSAGE_TYPE from Kafka Services Topic: {}", responseServicesMessage)
			        	// Only accept FEED_RSP for registered clients
			        	if(clients contains responseServicesMessage.client) {
			        	    // Only process FEED_RSP messages to Services clients
			        		if(clients(responseServicesMessage.client) equals Configuration.servicesTopic) {
			        			consumer.handleDelivery(responseServicesMessage)
			        		}
			        	}
				      }
				      case _ =>
				    }
			        
		      }
		} catch {
		  case e: Throwable => log.error("KafkaGenericConsumerActor registerServicesToServicesFeed Error processing message, stop consuming: " + e)
		}
	  
	}
	
	override def postStop() = {}
}


//class KafkaConsumerActor extends Actor with ActorLogging {
//	import KafkaGenericConsumerActor._
//    import TttsStrategyMessages._
//    
//	var counter = 0
//	
//	override def receive = {
//		case StartFakeFeedGeneratorMessage(msg) => 
//
//	        // Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
//	        val messageTraits = Utils.generateMessageTraits
//	        log.debug(s"FakeFeedActor, Gettin message: {}", msg)
//  		    counter += 1
//		    
//		    msg match {
//				case msg: RequestFeedFacadeTopicMessage => {
//			    	val fakeQuote = "%.2f".format(Random.nextDouble() + 22)
//				    val fakeMessage = ResponseFeedFacadeTopicMessage(messageTraits._1, FEED_RESPONSE_MESSAGE_TYPE, msg.client , s"$fakeQuote", messageTraits._2, s"$counter")
//				    val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
//				    kafkaFacadeTopicProducerActor ! fakeMessage
//				}
//				case msg: RequestFeedServicesTopicMessage => {
//			    	val fakeQuote = "%.2f".format(Random.nextDouble() + 55)
//				    val fakeMessage = ResponseFeedServicesTopicMessage(messageTraits._1, FEED_RESPONSE_MESSAGE_TYPE, msg.client , s"$fakeQuote", messageTraits._2, s"$counter")
//				    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
//				    kafkaServicesTopicProducerActor ! fakeMessage
//				}
//				case _ =>
//		    }
//		    
//		
//	    case StopFakeFeedGeneratorMessage =>
//		  log.debug("FakeFeedActor Cancel")
//	      context.stop(self)
//		case _ => log.error("FakeFeedActor Received unknown message")
//	}
//  
//}
