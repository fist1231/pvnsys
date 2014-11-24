package com.pvnsys.ttts.strategy.mq

import java.util.Properties

import scala.collection.JavaConversions.seqAsJavaList

import com.pvnsys.ttts.strategy.Configuration
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.AllForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy.Restart
import akka.actor.actorRef2Scala
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import spray.json.DefaultJsonProtocol
import spray.json.pimpString


object KafkaFacadeTopicConsumerActor {
//  def props(address: InetSocketAddress, groupName: Option[String]) = Props(new KafkaConsumerActor(address, groupName))
  def props(processorActorRef: ActorRef, serviceId: String) = Props(new KafkaFacadeTopicConsumerActor(processorActorRef, serviceId))

//  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
//    def genString(o: AnyRef): String = o.getClass.getName
//    override def getClazz(o: AnyRef): Class[_] = this.getClazz(o)
//  }
  
  sealed trait KafkaFacadeTopicConsumerActorMessage
  case object StopMessage extends KafkaFacadeTopicConsumerActorMessage
}


object KafkaFacadeTopicConsumerActorJsonProtocol extends DefaultJsonProtocol {
  import TttsStrategyMessages._
  implicit val strategyPayloadFormat = jsonFormat13(StrategyPayload)
  implicit val requestStrategyFacadeTopicMessageFormat = jsonFormat6(RequestStrategyFacadeTopicMessage)
}


/**
 * This actor will register itself to consume messages from the Kafka server. 
 */
class KafkaFacadeTopicConsumerActor(processorActorRef: ActorRef, serviceId: String) extends Actor with ActorLogging {
  
	import KafkaFacadeTopicConsumerActor._
	import context._
	import KafkaFacadeTopicConsumerActorJsonProtocol._
	import TttsStrategyMessages._

//	override val log = Logging(context.system, this)
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("KafkaFacadeTopicConsumerActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	
	private def startListening() = {
		
		val consumer = new DefaultKafkaConsumer {
		    override def handleDelivery(message: TttsStrategyMessage) = {
		        processorActorRef ! message
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

		case z => log.error("KafkaFacadeTopicConsumerActor Received unknown message: {}", z)
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
				    
				    val requestStrategyFacadeTopicMessage = msgJsonObj.convertTo[RequestStrategyFacadeTopicMessage]
				    matchRequest(requestStrategyFacadeTopicMessage) match {
				      case Some(facadeTopicMessage) => {
				    	log.info("Facade Consumer got {}", requestStrategyFacadeTopicMessage)
				        consumer.handleDelivery(requestStrategyFacadeTopicMessage)
				      }
				      case None => "Do nothing"
				    }
		      }
		} catch {
		  case e: Throwable => log.error("KafkaFacadeTopicConsumerActor Error processing message, stop consuming: " + e)
		}
	  
	}
	
	private def matchRequest(message: RequestStrategyFacadeTopicMessage): Option[RequestStrategyFacadeTopicMessage] = message.msgType match {
		/*
		 * KafkaFacadeTopicConsumerActor listens for only two message types: 
		 * 1. STRATEGY_REQUEST_MESSAGE_TYPE of RequestStrategyFacadeTopicMessage of FacadeTopicMessage
		 * 2. STRATEGY_STOP_REQUEST_MESSAGE_TYPE of RequestStrategyFacadeTopicMessage of FacadeTopicMessage
		 */ 
		case STRATEGY_REQUEST_MESSAGE_TYPE => Some(message)
		case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => Some(message)
		case m => {
			log.debug("KafkaFacadeTopicConsumerActor - not RequestStrategyFacadeTopicMessage from Facade Topic, skipping Kafka message: {}", m) 
			None
		}
	}
	
   
	override def postStop() = {}
}
