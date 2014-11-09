package com.pvnsys.ttts.engine.service

import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy, PoisonPill}
import akka.actor.SupervisorStrategy.{Restart, Stop, Escalate}
import akka.util.Timeout
import akka.stream.actor.ActorProducer
import scala.concurrent.duration._
import com.pvnsys.ttts.engine.mq.KafkaFacadeTopicConsumerActor
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import spray.json._
import org.reactivestreams.api.Producer
import com.pvnsys.ttts.engine.mq.{KafkaFacadeTopicConsumerActor, KafkaServicesTopicConsumerActor}
import com.pvnsys.ttts.engine.generator.EngineService
import akka.actor.ActorRef
import com.pvnsys.ttts.engine.mq.EngineActor
import com.pvnsys.ttts.engine.flows.{FacadeMessageFlow, ServicesMessageFlow}
import com.pvnsys.ttts.engine.util.Utils


object TttsEngineFService extends LazyLogging {
}

class TttsEngineFService extends Actor with ActorLogging {
  
  import TttsEngineFService._
  import TttsEngineMessages._
  
  val serviceUniqueID = Utils.generateUuid
  
    override val supervisorStrategy = OneForOneStrategy(loggingEnabled = true) {
	    case e: Exception =>
	      log.error("TttsEngineFService Unexpected failure: {}", e.getMessage)
	      Restart
  	}
  
    
    private def startService() = {
      
		val engineFacadeActor = context.actorOf(Props(classOf[EngineActor]), "engineFacadeConsumer")
		
		// Start Kafka consumer actor for incoming messages from Facade Topic
		val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(engineFacadeActor, serviceUniqueID), "engineKafkaFacadeConsumer")
		kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage
		
		val engineServicesActor = context.actorOf(Props(classOf[EngineActor]), "engineServicesConsumer")
		
		// Start Kafka consumer actor for incoming messages from Services Topic
		val kafkaServicesTopicConsumerActor = context.actorOf(KafkaServicesTopicConsumerActor.props(engineServicesActor, serviceUniqueID), "engineKafkaServicesConsumer")
		kafkaServicesTopicConsumerActor ! StartListeningServicesTopicMessage

		/*
		 * Start Facade topic message flow:
		 * 
		 * Kafka MQ ==> 
		 * ==> (FacadeTopicMessage from FacadeMS) ==> 
		 * ==> KafkeFacadeTopicConsumer ==> 
		 * ==> Processing Duct[RequestEngineFacadeTopicMessage, Producer]: 1.Logs message; 2.Logs client; 3.Converts message; 4.Creates Producer ==> 
		 * ==> Flow(Producer) ==> 
		 * ==> Publishing Duct: 1.Each message starts feed generator. 2.Each Feed generator's message published to Kafka.
		 * (Duct[RequestFeedFacadeTopicMessage, Unit]) ==> KafkaFacadeTopicProducer ==> 
		 * ==> (ResponseFeedFacadeTopicMessage from FeeddMS) ==> 
		 * ==> Kafka MQ
		 *   
		 */
		new FacadeMessageFlow(engineFacadeActor, serviceUniqueID).startFlow

		// Start Services topic message flow
		new ServicesMessageFlow(engineServicesActor, serviceUniqueID).startFlow

    }
  
	override def receive = {
		case StartEngineServiceMessage => startService()
		case StopEngineServiceMessage => {
			log.debug("TttsEngineService StopMessage")
		}
		case _ => log.error("TttsEngineService Received unknown message")
	}
  
}
