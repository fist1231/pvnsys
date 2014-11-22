package com.pvnsys.ttts.strategy.service

import akka.actor.{Actor, ActorLogging, Props, OneForOneStrategy, AllForOneStrategy, ActorRef, ActorSystem}
import akka.actor.SupervisorStrategy.{Restart, Stop, Escalate}
import com.pvnsys.ttts.strategy.mq.{KafkaFacadeTopicConsumerActor, KafkaServicesTopicConsumerActor, KafkaFacadeTopicProducerActor, KafkaServicesTopicProducerActor}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.pvnsys.ttts.strategy.util.Utils
import com.pvnsys.ttts.strategy.db.KdbActor
import akka.stream.FlowMaterializer
import com.pvnsys.ttts.strategy.flows.pub.{FacadePublisherActor, ServicesPublisherActor}
import com.pvnsys.ttts.strategy.flow.sub.{FacadeSubscriberActor, ServicesSubscriberActor}
import akka.stream.actor.{ActorSubscriber, ActorPublisher}
import org.reactivestreams.Publisher
import akka.stream.scaladsl.{Source, Sink}
import com.pvnsys.ttts.strategy.flows.v011.{FacadeStrategyRequestMessageFlow, ServicesStrategyRequestMessageFlow, ServicesStrategyResponseMessageFlow, FacadeStrategyResponseMessageFlow}
import com.pvnsys.ttts.strategy.flow.sub.ServicesFeedRequestSubscriberActor
import com.pvnsys.ttts.strategy.flow.sub.ServicesStrategyResponseSubscriberActor


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
		val facadeMessagesProducerActor = FacadePublisherActor(factory) // Create Publisher Actor that consumes messages received by KafkaFacadeTopicConsumerActor
		val facadeStrategyRequestMessagesProducer: Publisher[TttsStrategyMessage] = ActorPublisher[TttsStrategyMessage](facadeMessagesProducerActor) // Create ActprPublisher from Publisher actor
		val facadeStrategyRequestFlowSource = Source(facadeStrategyRequestMessagesProducer)
		
		val facadeStrategyRequestMessagesConsumerActor = ServicesSubscriberActor(factory, serviceUniqueID, kafkaServicesTopicProducerActor) // Create Subscriber Actor that sends messages to KafkaServicesTopicProducerActor
		val facadeStrategyRequestMessagesConsumer = ActorSubscriber[TttsStrategyMessage](facadeStrategyRequestMessagesConsumerActor) // Create ActorSubscriber from Subscriber actor
		val facadeStrategyRequestFlowSink = Sink(facadeStrategyRequestMessagesConsumer) // Create Flow Sink
		
		// Start listening to Kafka Facade Topic
		val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(facadeMessagesProducerActor, serviceUniqueID), "strategyKafkaFacadeConsumer")
		kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage
		
		// Prepare Source and Sink for Flow 2: Services STRATEGY_REQ/STRATEGY_STOP_REQ ~> Services FEED_REQ/FEED_STOP_REQ
		val servicesStrategyRequestMessagesProducerActor = ServicesPublisherActor(factory)
		val servicesStrategyRequestMessagesProducer: Publisher[TttsStrategyMessage] = ActorPublisher[TttsStrategyMessage](servicesStrategyRequestMessagesProducerActor)
		val servicesStrategyRequestFlowSource = Source(servicesStrategyRequestMessagesProducer)
		
		val servicesStrategyRequestMessagesConsumerActor = ServicesFeedRequestSubscriberActor(factory, serviceUniqueID, kafkaServicesTopicProducerActor)
		val servicesStrategyRequestMessagesConsumer = ActorSubscriber[TttsStrategyMessage](servicesStrategyRequestMessagesConsumerActor)
		val servicesStrategyRequestFlowSink = Sink(servicesStrategyRequestMessagesConsumer)

		// Prepare Source and Sink for Flow 3: Services FEED_RESPONSE intended for Facade Topic ~> Facade STRATEGY_RESPONSE
		val facadeStrategyResponseServicesMessagesProducerActor = ServicesPublisherActor(factory)
		val facadeStrategyResponseServicesMessagesProducer: Publisher[TttsStrategyMessage] = ActorPublisher[TttsStrategyMessage](facadeStrategyResponseServicesMessagesProducerActor)
		val facadeStrategyResponseFlowSource = Source(facadeStrategyResponseServicesMessagesProducer)
		
		val facadeStrategyResponseMessagesConsumerActor = FacadeSubscriberActor(factory, serviceUniqueID, kafkaFacadeTopicProducerActor)
		val facadeStrategyResponseMessagesConsumer = ActorSubscriber[TttsStrategyMessage](facadeStrategyResponseMessagesConsumerActor)
		val facadeStrategyResponseFlowFlowSink = Sink(facadeStrategyResponseMessagesConsumer)
		
		// Prepare Source and Sink for Flow 4: Services FEED_RESPONSE intended for Services Topic ~> Services STRATEGY_RESPONSE
		val servicesStrategyResponseServicesMessagesProducerActor = ServicesPublisherActor(factory)
		val servicesStrategyResponseServicesMessagesProducer: Publisher[TttsStrategyMessage] = ActorPublisher[TttsStrategyMessage](servicesStrategyResponseServicesMessagesProducerActor)
		val servicesStrategyResponseFlowSource = Source(servicesStrategyResponseServicesMessagesProducer)

		val servicesStrategyResponseMessagesConsumerActor = ServicesStrategyResponseSubscriberActor(factory, serviceUniqueID, kafkaServicesTopicProducerActor)
		val servicesStrategyResponseMessagesConsumer = ActorSubscriber[TttsStrategyMessage](servicesStrategyResponseMessagesConsumerActor)
		val servicesStrategyResponseFlowSink = Sink(servicesStrategyResponseMessagesConsumer)

		/*
		 * 5. Start listening to Kafka Services Topic
		 */
		val kafkaServicesTopicConsumerActor = context.actorOf(KafkaServicesTopicConsumerActor.props(servicesStrategyRequestMessagesProducerActor, facadeStrategyResponseServicesMessagesProducerActor, servicesStrategyResponseServicesMessagesProducerActor, serviceUniqueID), "strategyKafkaServicesConsumer")
		kafkaServicesTopicConsumerActor ! StartListeningServicesTopicMessage
		
		/*
		 * 6. Wire the Flows
		 */
		// Start Flow 1: Facade STRATEGY_REQ/STRATEGY_STOP_REQ ~> Services FEED_REQ/FEED_STOP_REQ
		new FacadeStrategyRequestMessageFlow(facadeStrategyRequestFlowSource, facadeStrategyRequestFlowSink).startFlow
		
		// Start Flow 2: Services STRATEGY_REQ/STRATEGY_STOP_REQ ~> Services FEED_REQ/FEED_STOP_REQ
		new ServicesStrategyRequestMessageFlow(servicesStrategyRequestFlowSource, servicesStrategyRequestFlowSink).startFlow	

		// Start Flow 3: Services FEED_RESPONSE intended for Facade Topic ~> Facade STRATEGY_RESPONSE
		new FacadeStrategyResponseMessageFlow(facadeStrategyResponseFlowSource, facadeStrategyResponseFlowFlowSink, serviceUniqueID).startFlow	

		// Start Flow 4: Services FEED_RESPONSE intended for Services Topic ~> Services STRATEGY_RESPONSE
		new ServicesStrategyResponseMessageFlow(servicesStrategyResponseFlowSource, servicesStrategyResponseFlowSink, serviceUniqueID).startFlow	

    }
  
	override def receive = {
		case StartStrategyServiceMessage => startService()
		case StopStrategyServiceMessage => {
			log.debug("TttsStrategyService StopMessage")
		}
		case _ => log.error("TttsStrategyService Received unknown message")
	}
	
}
