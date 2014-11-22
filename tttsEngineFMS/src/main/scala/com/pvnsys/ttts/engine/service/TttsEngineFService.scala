package com.pvnsys.ttts.engine.service

import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy, ActorSystem}
import akka.actor.SupervisorStrategy.{Restart, Stop, Escalate}
import akka.util.Timeout
import scala.concurrent.duration._
import com.pvnsys.ttts.engine.mq.KafkaFacadeTopicConsumerActor
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import spray.json._
import com.pvnsys.ttts.engine.mq.{KafkaFacadeTopicConsumerActor, KafkaServicesTopicConsumerActor, KafkaFacadeTopicProducerActor, KafkaServicesTopicProducerActor}
import akka.actor.ActorRef
import com.pvnsys.ttts.engine.util.Utils
import com.pvnsys.ttts.engine.db.KdbActor
import akka.stream.FlowMaterializer
import com.pvnsys.ttts.engine.flows.pub.{FacadePublisherActor, ServicesPublisherActor}
import com.pvnsys.ttts.engine.flow.sub.{FacadeSubscriberActor, ServicesSubscriberActor}
import akka.stream.actor.{ActorSubscriber, ActorPublisher}
import org.reactivestreams.Publisher
import akka.stream.scaladsl.{Source, Sink}
import com.pvnsys.ttts.engine.flows.v011.{FacadeEngineRequestMessageFlow, ServicesEngineRequestMessageFlow, ServicesEngineResponseMessageFlow, FacadeEngineResponseMessageFlow}
import com.pvnsys.ttts.engine.flow.sub.ServicesEngineRequestSubscriberActor
import com.pvnsys.ttts.engine.flow.sub.ServicesEngineResponseSubscriberActor



object TttsEngineFService extends LazyLogging {
}

class TttsEngineFService extends Actor with ActorLogging {
  
  import TttsEngineFService._
  import TttsEngineMessages._
  import KdbActor._
  
  val serviceUniqueID = Utils.generateUuid
  
    override val supervisorStrategy = OneForOneStrategy(loggingEnabled = true) {
	    case e: Exception =>
	      log.error("TttsEngineFService Unexpected failure: {}", e.getMessage)
	      Restart
  	}
  
    
    private def startService() = {

		implicit val executor = context.dispatcher
	    implicit val materializer = FlowMaterializer()
    	implicit val factory = ActorSystem()
      
		/*
		 * 1. Start kdb database server
		 */
		val kdbActor = context.actorOf(KdbActor.props(serviceUniqueID), "kdbActor")
		kdbActor ! StartKdbMessage
 		
		/*
		 * 2. Start Kafka Facade Topic Producer
		 */
		val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
		
		/*
		 * 3. Start Kafka Services Topic Producer
		 */
		val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))

		/*
		 * 4. Prepare Sources and Sinks for the Flows
		 */
		// Prepare Source and Sink for Flow 1: Facade STRATEGY_REQ/STRATEGY_STOP_REQ ~> Services FEED_REQ/FEED_STOP_REQ
		val facadeEngineRequestMessagesProducerActor = FacadePublisherActor(factory) // Create Publisher Actor that consumes messages received by KafkaFacadeTopicConsumerActor
		val facadeEngineRequestMessagesProducer: Publisher[TttsEngineMessage] = ActorPublisher[TttsEngineMessage](facadeEngineRequestMessagesProducerActor) // Create ActprPublisher from Publisher actor
		val facadeEngineRequestFlowSource = Source(facadeEngineRequestMessagesProducer)
		
		val facadeEngineRequestMessagesConsumerActor = ServicesSubscriberActor(factory, serviceUniqueID, kafkaServicesTopicProducerActor) // Create Subscriber Actor that sends messages to KafkaServicesTopicProducerActor
		val facadeEngineRequestMessagesConsumer = ActorSubscriber[TttsEngineMessage](facadeEngineRequestMessagesConsumerActor) // Create ActorSubscriber from Subscriber actor
		val facadeEngineRequestFlowSink = Sink(facadeEngineRequestMessagesConsumer) // Create Flow Sink
		
		// Start listening to Kafka Facade Topic
		val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(facadeEngineRequestMessagesProducerActor, serviceUniqueID), "strategyKafkaFacadeConsumer")
		kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage
		
		// Prepare Source and Sink for Flow 2: Services STRATEGY_REQ/STRATEGY_STOP_REQ ~> Services FEED_REQ/FEED_STOP_REQ
		val servicesEngineRequestMessagesProducerActor = ServicesPublisherActor(factory)
		val servicesEngineRequestMessagesProducer: Publisher[TttsEngineMessage] = ActorPublisher[TttsEngineMessage](servicesEngineRequestMessagesProducerActor)
		val servicesEngineRequestFlowSource = Source(servicesEngineRequestMessagesProducer)
		
		val servicesEngineRequestMessagesConsumerActor = ServicesEngineRequestSubscriberActor(factory, serviceUniqueID, kafkaServicesTopicProducerActor)
		val servicesEngineRequestMessagesConsumer = ActorSubscriber[TttsEngineMessage](servicesEngineRequestMessagesConsumerActor)
		val servicesEngineRequestFlowSink = Sink(servicesEngineRequestMessagesConsumer)

		// Prepare Source and Sink for Flow 3: Services FEED_RESPONSE intended for Facade Topic ~> Facade STRATEGY_RESPONSE
		val facadeEngineResponseServicesMessagesProducerActor = ServicesPublisherActor(factory)
		val facadeEngineResponseServicesMessagesProducer: Publisher[TttsEngineMessage] = ActorPublisher[TttsEngineMessage](facadeEngineResponseServicesMessagesProducerActor)
		val facadeEngineResponseFlowSource = Source(facadeEngineResponseServicesMessagesProducer)
		
		val facadeEngineResponseMessagesConsumerActor = FacadeSubscriberActor(factory, serviceUniqueID, kafkaFacadeTopicProducerActor)
		val facadeEngineResponseMessagesConsumer = ActorSubscriber[TttsEngineMessage](facadeEngineResponseMessagesConsumerActor)
		val facadeEngineResponseFlowFlowSink = Sink(facadeEngineResponseMessagesConsumer)
		
		// Prepare Source and Sink for Flow 4: Services FEED_RESPONSE intended for Services Topic ~> Services STRATEGY_RESPONSE
		val servicesEngineResponseServicesMessagesProducerActor = ServicesPublisherActor(factory)
		val servicesEngineResponseServicesMessagesProducer: Publisher[TttsEngineMessage] = ActorPublisher[TttsEngineMessage](servicesEngineResponseServicesMessagesProducerActor)
		val servicesEngineResponseFlowSource = Source(servicesEngineResponseServicesMessagesProducer)

		val servicesEngineResponseMessagesConsumerActor = ServicesEngineResponseSubscriberActor(factory, serviceUniqueID, kafkaServicesTopicProducerActor)
		val servicesEngineResponseMessagesConsumer = ActorSubscriber[TttsEngineMessage](servicesEngineResponseMessagesConsumerActor)
		val servicesEngineResponseFlowSink = Sink(servicesEngineResponseMessagesConsumer)

		/*
		 * 5. Start listening to Kafka Services Topic
		 */
		val kafkaServicesTopicConsumerActor = context.actorOf(KafkaServicesTopicConsumerActor.props(servicesEngineRequestMessagesProducerActor, facadeEngineResponseServicesMessagesProducerActor, servicesEngineResponseServicesMessagesProducerActor, serviceUniqueID), "strategyKafkaServicesConsumer")
		kafkaServicesTopicConsumerActor ! StartListeningServicesTopicMessage
		
		/*
		 * 6. Wire the Flows
		 */
		// Start Flow 1: Facade STRATEGY_REQ/STRATEGY_STOP_REQ ~> Services FEED_REQ/FEED_STOP_REQ
		new FacadeEngineRequestMessageFlow(facadeEngineRequestFlowSource, facadeEngineRequestFlowSink).startFlow
		
		// Start Flow 2: Services STRATEGY_REQ/STRATEGY_STOP_REQ ~> Services FEED_REQ/FEED_STOP_REQ
		new ServicesEngineRequestMessageFlow(servicesEngineRequestFlowSource, servicesEngineRequestFlowSink).startFlow	

		// Start Flow 3: Services FEED_RESPONSE intended for Facade Topic ~> Facade STRATEGY_RESPONSE
		new FacadeEngineResponseMessageFlow(facadeEngineResponseFlowSource, facadeEngineResponseFlowFlowSink, serviceUniqueID).startFlow	

		// Start Flow 4: Services FEED_RESPONSE intended for Services Topic ~> Services STRATEGY_RESPONSE
		new ServicesEngineResponseMessageFlow(servicesEngineResponseFlowSource, servicesEngineResponseFlowSink, serviceUniqueID).startFlow	
      
      
/*      
		// Start kdb database server
		val kdbActor = context.actorOf(KdbActor.props(serviceUniqueID), "kdbActor")
		kdbActor ! StartKdbMessage
      
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
*/
    }
  
	override def receive = {
		case StartEngineServiceMessage => startService()
		case StopEngineServiceMessage => {
			log.debug("TttsEngineService StopMessage")
		}
		case _ => log.error("TttsEngineService Received unknown message")
	}
  
}
