package com.pvnsys.ttts.strategy.service

import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy, PoisonPill}
import akka.actor.SupervisorStrategy.{Restart, Stop, Escalate}
import akka.util.Timeout
import akka.stream.actor.ActorProducer
import scala.concurrent.duration._
import com.pvnsys.ttts.strategy.mq.KafkaFacadeTopicConsumerActor
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.{StartStrategyServiceMessage, StopStrategyServiceMessage, StartListeningFacadeTopicMessage, FacadeTopicMessage, RequestStrategyFacadeTopicMessage, TttsStrategyMessage}
import spray.json._
import org.reactivestreams.api.Producer
import com.pvnsys.ttts.strategy.mq.{KafkaFacadeTopicConsumerActor, KafkaServicesTopicConsumerActor}
import com.pvnsys.ttts.strategy.generator.StrategyService
import akka.actor.ActorRef
import com.pvnsys.ttts.strategy.mq.StrategyActor
import com.pvnsys.ttts.strategy.flows.{FacadeMessageFlow, ServicesMessageFlow}


object TttsStrategyService extends LazyLogging {
}

class TttsStrategyService extends Actor with ActorLogging {
  
  import TttsStrategyService._
  
    override val supervisorStrategy = OneForOneStrategy(loggingEnabled = true) {
	    case e: Exception =>
	      log.error("TttsStrategyService Unexpected failure: {}", e.getMessage)
	      Restart
  	}
  
    
    private def startService() = {
      
		val strategyFacadeActor = context.actorOf(Props(classOf[StrategyActor]), "strategyFacadeConsumer")
		
		// Start Kafka consumer actor for incoming messages from Facade Topic
		val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(strategyFacadeActor), "strategyKafkaFacadeConsumer")
		kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage
		
		val strategyServicesActor = context.actorOf(Props(classOf[StrategyActor]), "feedServicesConsumer")
		
		// Start Kafka consumer actor for incoming messages from Services Topic
		val kafkaServicesTopicConsumerActor = context.actorOf(KafkaServicesTopicConsumerActor.props(strategyServicesActor), "strategyKafkaServicesConsumer")
		kafkaServicesTopicConsumerActor ! StartListeningFacadeTopicMessage
		  

		// Start Facade topic message flow
		new FacadeMessageFlow(strategyFacadeActor).startFlow

		// Start Services topic message flow
		new ServicesMessageFlow(strategyServicesActor).startFlow

    }
  
	override def receive = {
		case StartStrategyServiceMessage => startService()
		case StopStrategyServiceMessage => {
			log.debug("TttsStrategyService StopMessage")
		}
		case _ => log.error("TttsStrategyService Received unknown message")
	}
  
}
