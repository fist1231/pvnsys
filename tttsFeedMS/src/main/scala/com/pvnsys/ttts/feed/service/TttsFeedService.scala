package com.pvnsys.ttts.feed.service

import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy, PoisonPill}
import akka.actor.SupervisorStrategy.{Restart, Stop, Escalate}
import akka.util.Timeout
import akka.stream.actor.ActorProducer
import scala.concurrent.duration._
import com.pvnsys.ttts.feed.mq.KafkaFacadeTopicConsumerActor
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.feed.messages.TttsFeedMessages
import spray.json._
import org.reactivestreams.api.Producer
import com.pvnsys.ttts.feed.mq.{KafkaFacadeTopicConsumerActor, KafkaServicesTopicConsumerActor}
import com.pvnsys.ttts.feed.generator.FeedService
import akka.actor.ActorRef
import com.pvnsys.ttts.feed.generator.FeedGeneratorActor.StopFeedGeneratorMessage
import com.pvnsys.ttts.feed.mq.FeedActor
import com.pvnsys.ttts.feed.flows.{FacadeMessageFlow, ServicesMessageFlow}


//object FeedServiceJsonProtocol extends DefaultJsonProtocol {
//  implicit val facadeTopicMessageFormat = jsonFormat6(FacadeTopicMessage)
//  implicit val requestFeedFacadeTopicMessageFormat = jsonFormat6(RequestFeedFacadeTopicMessage)
//}

object TttsFeedService extends LazyLogging {
}

class TttsFeedService extends Actor with ActorLogging {
  
  import TttsFeedService._
  import TttsFeedMessages._
  
    override val supervisorStrategy = OneForOneStrategy(loggingEnabled = true) {
//	    case e: CustomException =>
//	      log.error("TttsFeedService Unexpected failure: {}", e.getMessage)
//	      Stop
	    case e: Exception =>
	      log.error("TttsFeedService Unexpected failure: {}", e.getMessage)
	      Restart
  	}
  
    
    private def startService() = {
      
		val feedFacadeActor = context.actorOf(Props(classOf[FeedActor]), "feedFacadeConsumer")
//		log.debug("+++++++ feedFacadeActor is: {}", feedFacadeActor)
//		val feedFacadeConsumer = ActorProducer(feedFacadeActor)
		
		// Start Kafka consumer actor for incoming messages from Facade Topic
		val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(feedFacadeActor), "kafkaFacadeConsumer")
//		log.debug("+++++++ KafkaFacadeTopicConsumerActor tttsFeedActorSystem is: {}", kafkaFacadeTopicConsumerActor)
		kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage
		
		
		val feedServicesActor = context.actorOf(Props(classOf[FeedActor]), "feedServicesConsumer")
//		log.debug("+++++++ feedServicesActor is: {}", feedServicesActor)
//		val feedServicesConsumer = ActorProducer(feedServicesActor)
		
		// Start Kafka consumer actor for incoming messages from Services Topic
		val kafkaServicesTopicConsumerActor = context.actorOf(KafkaServicesTopicConsumerActor.props(feedServicesActor), "kafkaServicesConsumer")
//		log.debug("+++++++ KafkaServicesTopicConsumerActor tttsFeedActorSystem is: {}", kafkaServicesTopicConsumerActor)
		kafkaServicesTopicConsumerActor ! StartListeningServicesTopicMessage
		  

		/*
		 * Start Facade topic message flow:
		 * 
		 * Kafka MQ ==> 
		 * ==> (FacadeTopicMessage from FacadeMS) ==> 
		 * ==> KafkeFacadeTopicConsumer via FeedActor ==> 
		 * ==> Processing Duct[RequestFeedFacadeTopicMessage, Producer]: 1.Logs nessage; 2.Logs client; 3.Converts message; 4.Creates Producer ==> 
		 * ==> Flow(Producer) ==> 
		 * ==> Publishing Duct: 1.Each message starts feed generator. 2.Each Feed generator's message published to Kafka.
		 * (Duct[RequestFeedFacadeTopicMessage, Unit]) ==> KafkaFacadeTopicProducer ==> 
		 * ==> (ResponseFeedFacadeTopicMessage from FeeddMS) ==> 
		 * ==> Kafka MQ
		 *   
		 */
		// 
		new FacadeMessageFlow(feedFacadeActor).startFlow

		// Start Services topic message flow
		new ServicesMessageFlow(feedServicesActor).startFlow

    }
  
	override def receive = {
		case StartFeedServiceMessage => startService()
		case StopFeedServiceMessage => {
			log.debug("TttsFeedService StopMessage")
		}
		case _ => log.error("TttsFeedService Received unknown message")
	}
  
}
