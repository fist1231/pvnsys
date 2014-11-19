package com.pvnsys.ttts.strategy.service

import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy, PoisonPill}
import akka.actor.SupervisorStrategy.{Restart, Stop, Escalate}
import akka.util.Timeout
import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import scala.concurrent.duration._
import com.pvnsys.ttts.strategy.mq.KafkaFacadeTopicConsumerActor
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.TttsStrategyMessage
import spray.json._
import org.reactivestreams.api.Producer
import com.pvnsys.ttts.strategy.mq.{KafkaFacadeTopicConsumerActor, KafkaServicesTopicConsumerActor}
import com.pvnsys.ttts.strategy.generator.StrategyService
import akka.actor.ActorRef
import com.pvnsys.ttts.strategy.mq.StrategyActor
import com.pvnsys.ttts.strategy.flows.{FacadeMessageFlow, ServicesMessageFlow}
import com.pvnsys.ttts.strategy.util.Utils
import com.pvnsys.ttts.strategy.db.KdbActor
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.actor.ActorConsumer


object TttsStrategyService extends LazyLogging {
}

class TttsStrategyService extends Actor with ActorLogging {
  
  import TttsStrategyService._
  import TttsStrategyMessages._
  import KdbActor._
  
  val serviceUniqueID = Utils.generateUuid
  
    override val supervisorStrategy = OneForOneStrategy(loggingEnabled = true) {
	    case e: Exception =>
	      log.error("TttsStrategyService Unexpected failure: {}", e.getMessage)
	      Restart
  	}
  
    
    private def startService() = {

		// Start kdb database server
		val kdbActor = context.actorOf(KdbActor.props(serviceUniqueID), "kdbActor")
		kdbActor ! StartKdbMessage
      
		val strategyFacadeActor = context.actorOf(Props(classOf[StrategyActor]), "strategyFacadeConsumer")
		
		// Start Kafka consumer actor for incoming messages from Facade Topic
		val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(strategyFacadeActor, serviceUniqueID), "strategyKafkaFacadeConsumer")
		kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage
		
		val strategyServicesActor = context.actorOf(Props(classOf[StrategyActor]), "strategyServicesConsumer")
		
		// Start Kafka consumer actor for incoming messages from Services Topic
		val kafkaServicesTopicConsumerActor = context.actorOf(KafkaServicesTopicConsumerActor.props(strategyServicesActor, serviceUniqueID), "strategyKafkaServicesConsumer")
		kafkaServicesTopicConsumerActor ! StartListeningServicesTopicMessage

		
		/*
		 * Start Facade topic message flow:
		 * 
		 * Kafka MQ ==> 
		 * ==> (FacadeTopicMessage from FacadeMS) ==> 
		 * ==> KafkeFacadeTopicConsumer ==> 
		 * ==> Processing Duct[RequestStrategyFacadeTopicMessage, Producer]: 1.Logs message; 2.Logs client; 3.Converts message; 4.Creates Producer ==> 
		 * ==> Flow(Producer) ==> 
		 * ==> Publishing Duct: 1.Each message starts feed generator. 2.Each Feed generator's message published to Kafka.
		 * (Duct[RequestFeedFacadeTopicMessage, Unit]) ==> KafkaFacadeTopicProducer ==> 
		 * ==> (ResponseFeedFacadeTopicMessage from FeeddMS) ==> 
		 * ==> Kafka MQ
		 *   
		 */
		new FacadeMessageFlow(strategyFacadeActor, serviceUniqueID).startFlow

		// Start Services topic message flow
		new ServicesMessageFlow(strategyServicesActor, serviceUniqueID).startFlow

    }
  
	override def receive = {
		case StartStrategyServiceMessage => startService()
		case StopStrategyServiceMessage => {
			log.debug("TttsStrategyService StopMessage")
		}
		case msg: StartBPStrategyServiceMessage => {
		  val client = sender
		  startNewFlow(client, msg.actor)
		}
		case _ => log.error("TttsStrategyService Received unknown message")
	}
	
	
    private def startNewFlow(client: ActorRef, consumerActor: ActorRef) = {

		// Start kdb database server
//		val kdbActor = context.actorOf(KdbActor.props(serviceUniqueID), "kdbActor")
//		kdbActor ! StartKdbMessage
      
//		val strategyFacadeActor = context.actorOf(Props(classOf[StrategyActor]), "strategyFacadeConsumer")
//		
//		// Start Kafka consumer actor for incoming messages from Facade Topic
//		val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(strategyFacadeActor, serviceUniqueID), "strategyKafkaFacadeConsumer")
//		kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage
		
//		val strategyServicesActor = context.actorOf(Props(classOf[StrategyActor]), "strategyServicesConsumer")
		
		// Start Kafka consumer actor for incoming messages from Services Topic
//		val kafkaServicesTopicConsumerActor = context.actorOf(KafkaServicesTopicConsumerActor.props(consumerActor, serviceUniqueID), "strategyKafkaServicesConsumer")
//		kafkaServicesTopicConsumerActor ! StartListeningServicesTopicMessage

		
		/*
		 * Start Facade topic message flow:
		 * 
		 * Kafka MQ ==> 
		 * ==> (FacadeTopicMessage from FacadeMS) ==> 
		 * ==> KafkeFacadeTopicConsumer ==> 
		 * ==> Processing Duct[RequestStrategyFacadeTopicMessage, Producer]: 1.Logs message; 2.Logs client; 3.Converts message; 4.Creates Producer ==> 
		 * ==> Flow(Producer) ==> 
		 * ==> Publishing Duct: 1.Each message starts feed generator. 2.Each Feed generator's message published to Kafka.
		 * (Duct[RequestFeedFacadeTopicMessage, Unit]) ==> KafkaFacadeTopicProducer ==> 
		 * ==> (ResponseFeedFacadeTopicMessage from FeeddMS) ==> 
		 * ==> Kafka MQ
		 *   
		 */
//		new FacadeMessageFlow(strategyFacadeActor, serviceUniqueID).startFlow

		// Start Services topic message flow
		new ServicesMessageFlow(consumerActor, serviceUniqueID).startFlow
		client ! ProducerConfirmationMessage

    }
	
  
}
