package com.pvnsys.ttts.strategy.service

import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy, PoisonPill}
import akka.actor.SupervisorStrategy.{Restart, Stop, Escalate}
import akka.util.Timeout
import akka.stream.actor.ActorProducer
import scala.concurrent.duration._
import com.pvnsys.ttts.strategy.mq.KafkaFacadeTopicConsumerActor
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import spray.json._
import org.reactivestreams.api.Producer
import com.pvnsys.ttts.strategy.mq.{KafkaGenericConsumerActor, KafkaFacadeTopicConsumerActor, KafkaServicesTopicConsumerActor}
import com.pvnsys.ttts.strategy.generator.StrategyService
import akka.actor.ActorRef
import com.pvnsys.ttts.strategy.mq.StrategyActor
import com.pvnsys.ttts.strategy.flows.{FacadeMessageFlow, ServicesMessageFlow}


object TttsStrategyService extends LazyLogging {
}

class TttsStrategyService extends Actor with ActorLogging {
  
  import TttsStrategyService._
  import TttsStrategyMessages._
  
  val commonVar: String = "sasfgs"
  
//    override val supervisorStrategy = OneForOneStrategy(loggingEnabled = true) {
//	    case e: Exception =>
//	      log.error("TttsStrategyService Unexpected failure: {}", e.getMessage)
//	      Restart
//  	}
  
    
    private def startService() = {
      
//		val strategyFacadeActor = context.actorOf(Props(classOf[StrategyActor]), "strategyFacadeConsumer")
//		
//		// Start Kafka consumer actor for incoming messages from Facade Topic
//		val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(strategyFacadeActor), "strategyKafkaFacadeConsumer")
//		kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage
//		
//		val strategyServicesActor = context.actorOf(Props(classOf[StrategyActor]), "strategyServicesConsumer")
//		
//		// Start Kafka consumer actor for incoming messages from Services Topic
//		val kafkaServicesTopicConsumerActor = context.actorOf(KafkaServicesTopicConsumerActor.props(strategyServicesActor), "strategyKafkaServicesConsumer")
//		kafkaServicesTopicConsumerActor ! StartListeningServicesTopicMessage

		val facadeStrategyRequestFlowStrategyActor = context.actorOf(Props(classOf[StrategyActor]), "facadeStrategyRequestFlowStrategyActor")
		val servicesStrategyRequestFlowStrategyActor = context.actorOf(Props(classOf[StrategyActor]), "servicesStrategyRequestFlowStrategyActor")
		val servicesFeedResponseToFacadeFlowStrategyActor = context.actorOf(Props(classOf[StrategyActor]), "servicesFeedResponseToFacadeFlowStrategyActor")
		val servicesFeedResponseToServicesFlowStrategyActor = context.actorOf(Props(classOf[StrategyActor]), "servicesFeedResponseToServicesFlowStrategyActor")
		// Start Kafka consumer actor for incoming messages from both Facade and Services Topics
		val kafkaGenericConsumerActor = context.actorOf(Props(classOf[KafkaGenericConsumerActor]), "strategyKafkaConsumer")
		log.debug("TttsStrategyService sending StartListeningStrategyRequestFlowFacadeTopicMessage to kafkaGenericConsumerActor")
		kafkaGenericConsumerActor ! StartListeningStrategyRequestFlowFacadeTopicMessage(facadeStrategyRequestFlowStrategyActor)
		log.debug("TttsStrategyService sending StartListeningStrategyRequestFlowFacadeTopicMessage to kafkaGenericConsumerActor")
		kafkaGenericConsumerActor ! StartListeningStrategyRequestFlowServicesTopicMessage(servicesStrategyRequestFlowStrategyActor)
		log.debug("TttsStrategyService sending StartListeningStrategyRequestFlowServicesTopicMessage to kafkaGenericConsumerActor")
		kafkaGenericConsumerActor ! StartListeningFeedResponseToFacadeFlowServicesTopicMessage(servicesFeedResponseToFacadeFlowStrategyActor)
		log.debug("TttsStrategyService sending StartListeningFeedResponseToFacadeFlowServicesTopicMessage to kafkaGenericConsumerActor")
		kafkaGenericConsumerActor ! StartListeningFeedResponseToServicesFlowServicesTopicMessage(servicesFeedResponseToServicesFlowStrategyActor)
		
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
//		new FacadeMessageFlow(strategyFacadeActor).startFlow
//
//		// Start Services topic message flow
//		new ServicesMessageFlow(strategyServicesActor).startFlow

		new FacadeMessageFlow(facadeStrategyRequestFlowStrategyActor).startFlow

		// Start Services topic message flow
		new ServicesMessageFlow(servicesStrategyRequestFlowStrategyActor).startFlow

		// Start Services topic message flow
		new ServicesMessageFlow(servicesFeedResponseToFacadeFlowStrategyActor).startFlow

		// Start Services topic message flow
		new ServicesMessageFlow(servicesFeedResponseToServicesFlowStrategyActor).startFlow
		
		
    }
  
	override def receive = {
		case StartStrategyServiceMessage => startService()
		case StopStrategyServiceMessage => {
			log.debug("TttsStrategyService StopMessage")
		}
		case _ => log.error("TttsStrategyService Received unknown message")
	}
  
}
