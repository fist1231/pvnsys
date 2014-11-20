package com.pvnsys.ttts.strategy.service

import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy, PoisonPill}
import akka.actor.SupervisorStrategy.{Restart, Stop, Escalate}
import akka.util.Timeout
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._
import com.pvnsys.ttts.strategy.mq.KafkaFacadeTopicConsumerActor
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import spray.json._
import com.pvnsys.ttts.strategy.mq.{KafkaFacadeTopicConsumerActor, KafkaServicesTopicConsumerActor}
import com.pvnsys.ttts.strategy.generator.StrategyService
import akka.actor.ActorRef
import com.pvnsys.ttts.strategy.util.Utils
import com.pvnsys.ttts.strategy.db.KdbActor
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.actor.ActorSystem
import com.pvnsys.ttts.strategy.flows.pub.FacadePublisherActor
import com.pvnsys.ttts.strategy.flows.pub.ServicesPublisherActor
import com.pvnsys.ttts.strategy.mq.KafkaFacadeTopicProducerActor
import com.pvnsys.ttts.strategy.flow.sub.FacadeSubscriberActor
import com.pvnsys.ttts.strategy.mq.KafkaServicesTopicProducerActor
import com.pvnsys.ttts.strategy.flow.sub.ServicesSubscriberActor
import akka.stream.actor.ActorPublisher
import org.reactivestreams.Publisher
import akka.stream.scaladsl.Source
import akka.stream.javadsl.FlowGraph
import akka.stream.scaladsl.FlowGraphImplicits
import akka.stream.scaladsl.PartialFlowGraph
import akka.stream.scaladsl.FlowGraphBuilder
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.javadsl.Broadcast
import akka.stream.scaladsl.Sink
import akka.stream.actor.ActorSubscriber
import com.pvnsys.ttts.strategy.impl.AbxStrategyImpl


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
      
		// Start kdb database server
		val kdbActor = context.actorOf(KdbActor.props(serviceUniqueID), "kdbActor")
		kdbActor ! StartKdbMessage
      
//		val strategyFacadeActor = context.actorOf(Props(classOf[StrategyActor]), "strategyFacadeConsumer")
//		
//		
//		
//		// Start Kafka consumer actor for incoming messages from Facade Topic
//		val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(strategyFacadeActor, serviceUniqueID), "strategyKafkaFacadeConsumer")
//		kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage
//		
//		val strategyServicesActor = context.actorOf(Props(classOf[StrategyActor]), "strategyServicesConsumer")
//		
//		// Start Kafka consumer actor for incoming messages from Services Topic
//		val kafkaServicesTopicConsumerActor = context.actorOf(KafkaServicesTopicConsumerActor.props(strategyServicesActor, serviceUniqueID), "strategyKafkaServicesConsumer")
//		kafkaServicesTopicConsumerActor ! StartListeningServicesTopicMessage

		
		
		// Create Producer Actor that consumes messages received by KafkaFacadeTopicConsumerActor
		val facadeMessagesProducerActor = FacadePublisherActor(factory)
		// Create ActprPublisher from producer actor
		val facadeMessagesProducer: Publisher[TttsStrategyMessage] = ActorPublisher[TttsStrategyMessage](facadeMessagesProducerActor)
		// Link producer actor to kafka facade consumer actor
		val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(facadeMessagesProducerActor, serviceUniqueID), "strategyKafkaFacadeConsumer")
		kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage
		
		
		val servicesMessagesProducerActor = ServicesPublisherActor(factory)
		val servicesMessagesProducer: Publisher[TttsStrategyMessage] = ActorPublisher[TttsStrategyMessage](servicesMessagesProducerActor)
		val kafkaServicesTopicConsumerActor = context.actorOf(KafkaServicesTopicConsumerActor.props(servicesMessagesProducerActor, serviceUniqueID), "strategyKafkaServicesConsumer")
		kafkaServicesTopicConsumerActor ! StartListeningServicesTopicMessage

		
		// Create Subscriber Actor that sends messages to KafkaFacadeTopicProducerActor 
		val kafkaFacadeTopicProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
		val facadeMessagesConsumer = FacadeSubscriberActor(factory, serviceUniqueID, kafkaFacadeTopicProducerActor)
		
		// Create Subscriber Actor that sends messages to KafkaServicesTopicProducerActor 
		val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
		val servicesMessagesConsumer = ServicesSubscriberActor(factory, serviceUniqueID, kafkaServicesTopicProducerActor)
		
		// Wire the Flows
		
		val facadeStrategyRequestFlowSource = Source(facadeMessagesProducer)
		val servicesStrategyRequestFlowSource = Source(servicesMessagesProducer)
		val facadeStrategyRequestFlowSink = Sink(facadeMessagesConsumer)
		val servicesStrategyRequestFlowSink = Sink(servicesMessagesConsumer)
		
		
		
//		val facadeStrategyRequestFlow = Flow[TttsStrategyMessage].
//		map {
//			StrategyService.convertServicesMessage
//		}.runWith(facadeStrategyRequestFlowSource, facadeStrategyRequestFlowSink)
		
		val facadeStrategyRequestFlow = Flow[TttsStrategyMessage].
	    map { msg =>
	      val messageType = msg match {
	        case x: RequestStrategyFacadeTopicMessage => x.msgType 
	        case x: RequestStrategyServicesTopicMessage => x.msgType 
	        case x: ResponseFeedFacadeTopicMessage => x.msgType 
	        case x: ResponseFeedServicesTopicMessage => x.msgType 
	        case _ => "UNKNOWN"
	      }
	      log.debug("*******>> Step 0: StrategyMS ServicesMessageFlow Initialized. Received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      	  
	          log.debug("*******>> Step 1: Strategy ServicesMessageFlow Creating schema for first Feed Response message {}", msg)
		      msg match {
		        case x: RequestStrategyFacadeTopicMessage => // Nothing for Strategy messages 
		        case x: RequestStrategyServicesTopicMessage => // Nothing for Strategy messages 
		        case x: ResponseFeedFacadeTopicMessage => {
		          x.sequenceNum match {
		            case "1" => new AbxStrategyImpl(context).createSchema(serviceUniqueID, msg)
		            case _ => // Do nothing
		          }
 
		        }
		        case x: ResponseFeedServicesTopicMessage => {
		          x.sequenceNum match {
		            case "1" => new AbxStrategyImpl(context).createSchema(serviceUniqueID, msg)
		            case _ => // Do nothing
		          }
 
		        }
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          log.debug("*******>> Step 2: Strategy ServicesMessageFlow Write quotes feed data to db {}", msg)
		      msg match {
		        case x: RequestStrategyFacadeTopicMessage => // Nothing for Strategy messages 
		        case x: RequestStrategyServicesTopicMessage => // Nothing for Strategy messages 
		        case x: ResponseFeedFacadeTopicMessage => {
		            new AbxStrategyImpl(context).writeQuotesData(serviceUniqueID, msg)
		        }
		        case x: ResponseFeedServicesTopicMessage => {
		            new AbxStrategyImpl(context).writeQuotesData(serviceUniqueID, msg)
		        }
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          log.debug("*******>> Step 3: Strategy ServicesMessageFlow Apply strategy logic to the quotes feed {}", msg)
		      val outp = msg match {
		        case x: RequestStrategyFacadeTopicMessage => msg 
		        case x: RequestStrategyServicesTopicMessage => msg
		        case x: ResponseFeedFacadeTopicMessage => {
		            new AbxStrategyImpl(context).applyStrategy(serviceUniqueID, msg)
		        }
		        case x: ResponseFeedServicesTopicMessage => {
		            new AbxStrategyImpl(context).applyStrategy(serviceUniqueID, msg)
		        }
		        case _ => msg
		      }
	          outp
	    }.
		map {
			StrategyService.convertServicesMessage
		}.groupBy {
	      // Splits stream of Messages by message type and returns map(String -> org.reactivestreams.api.Producer[TttsStrategyMessage]) 
//	      case msg: ResponseFeedFacadeTopicMessage => "FeedFacade"
//	      case msg: ResponseFeedServicesTopicMessage => "FeedServices"
	      case msg: RequestStrategyFacadeTopicMessage => "StrategyFacade"
	      case msg: RequestStrategyServicesTopicMessage => "StrategyServices"
	      case msg: ResponseStrategyFacadeTopicMessage => {
	        log.debug("*******>> Step 5: Strategy ServicesMessageFlow Groupby operation on ResponseStrategyFacadeTopicMessage")
	        "StrategyFacadeResponse"
	      }
	      case msg: ResponseStrategyServicesTopicMessage => {
	        log.debug("*******>> Step 5: Strategy ServicesMessageFlow Groupby operation on ResponseStrategyServicesTopicMessage")
	        "StrategyServicesResponse"
	      }
	      case _ => "Garbage"
		}.
		map {
		  case (str, producer) => 
		    str match {
//		    	case "FeedFacade" => Flow[TttsStrategyMessage].runWith(facadeStrategyRequestFlowSource, facadeStrategyRequestFlowSink)
//		    	case "FeedServices" => Flow[TttsStrategyMessage].runWith(facadeStrategyRequestFlowSource, facadeStrategyRequestFlowSink)
		    	case "StrategyFacade" => Flow[TttsStrategyMessage].runWith(facadeStrategyRequestFlowSource, servicesStrategyRequestFlowSink)
		    	case "StrategyServices" => Flow[TttsStrategyMessage].runWith(servicesStrategyRequestFlowSource, servicesStrategyRequestFlowSink)
		        
		    	case "StrategyFacadeResponse" => Flow[TttsStrategyMessage].runWith(servicesStrategyRequestFlowSource, facadeStrategyRequestFlowSink)
		    	case "StrategyServicesResponse" => Flow[TttsStrategyMessage].runWith(servicesStrategyRequestFlowSource, servicesStrategyRequestFlowSink)
		      
		    }
		}
		
		

//		Flow.with
		
//		facadeStrategyRequestFlow.
//		map{msg=>
//		  msg
//    	}.
//    	groupBy {
//    	  case msg: TttsStrategyMessage => "Outloop"
//    	}.
//    	produceTo(servicesMessagesConsumer)

    // send primes to both slow file sink and console sink using graph API
		
//		Flow {
//    	  facadeMessagesProducer ~> servicesMessagesConsumer
//    	}
		
		
//		val f1 = FlowFrom[TttsStrategyMessage].map(_.)
//		
//    val materialized = FlowGraph { implicit builder =>
//      import FlowGraphImplicits._
//      val broadcast = Broadcast[Int] // the splitter - like a Unix tee
//      facadeMessagesProducer ~> servicesMessagesConsumer // connect primes to splitter, and one side to file
////      broadcast ~> consoleSink // connect other side of splitter to console
//    }.run()
		
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
//		new ServicesMessageFlow(strategyServicesActor, serviceUniqueID).startFlow

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
//		new ServicesMessageFlow(consumerActor, serviceUniqueID).startFlow
		client ! ProducerConfirmationMessage

    }
	
  
}
