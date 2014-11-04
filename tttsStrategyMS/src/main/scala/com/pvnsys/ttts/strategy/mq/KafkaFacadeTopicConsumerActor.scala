package com.pvnsys.ttts.strategy.mq

import akka.actor.{Actor, ActorRef, ActorLogging, Props, AllForOneStrategy}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.{StartListeningFacadeTopicMessage, FacadeTopicMessage, RequestStrategyFacadeTopicMessage, TttsStrategyMessage}
import kafka.consumer.ConsumerConfig
import java.util.Properties
import kafka.consumer.Consumer
import scala.collection.JavaConversions._
import com.pvnsys.ttts.strategy.Configuration
import akka.actor.SupervisorStrategy.{Restart, Stop}
import spray.json._


object KafkaFacadeTopicConsumerActorJsonProtocol extends DefaultJsonProtocol {
  implicit val facadeTopicMessageFormat = jsonFormat7(FacadeTopicMessage)
}

object KafkaFacadeTopicConsumerActor {
//  def props(address: InetSocketAddress, groupName: Option[String]) = Props(new KafkaConsumerActor(address, groupName))
  def props(toWhom: ActorRef) = Props(new KafkaFacadeTopicConsumerActor(toWhom))
}

/**
 * This actor will register itself to consume messages from the Kafka server. 
 */
class KafkaFacadeTopicConsumerActor(toWhom: ActorRef) extends Actor with ActorLogging {
  
	import KafkaFacadeTopicConsumerActor._
	import StrategyActor._
	import context._
	import KafkaFacadeTopicConsumerActorJsonProtocol._
	import TttsStrategyMessages._
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("KafkaFacadeTopicConsumerActor Unexpected failure: {}", e.getMessage)
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
		case STRATEGY_REQUEST_MESSAGE_TYPE => Some(message)
		case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => Some(message)
		case _ => {
			log.debug("KafkaFacadeTopicConsumerActorJsonProtocol - not Strategy Service request, skipping Kafka message") 
			None
		}
	}
	
   
	override def postStop() = {}
}
