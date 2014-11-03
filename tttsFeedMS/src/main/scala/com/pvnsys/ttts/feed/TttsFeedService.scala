package com.pvnsys.ttts.feed

import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy, PoisonPill}
import akka.actor.SupervisorStrategy.{Restart, Stop, Escalate}
import akka.util.Timeout
import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import scala.concurrent.duration._
import com.pvnsys.ttts.feed.mq.KafkaFacadeTopicConsumerActor
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.{StartFeedServiceMessage, StopFeedServiceMessage, StartListeningFacadeTopicMessage, FacadeTopicMessage, RequestFeedFacadeTopicMessage, TttsFeedMessage}
import spray.json._
import org.reactivestreams.api.Producer
import com.pvnsys.ttts.feed.mq.{KafkaFacadeTopicConsumerActor, KafkaServicesTopicConsumerActor}
import com.pvnsys.ttts.feed.generator.FeedGeneratorActor
import com.pvnsys.ttts.feed.generator.FeedService
import akka.actor.ActorRef
import scala.collection.mutable
import scala.collection.mutable.Map
import com.pvnsys.ttts.feed.generator.FeedGeneratorActor.StopFeedGeneratorMessage

object FeedServiceJsonProtocol extends DefaultJsonProtocol {
  implicit val facadeTopicMessageFormat = jsonFormat4(FacadeTopicMessage)
  implicit val requestFeedFacadeTopicMessageFormat = jsonFormat4(RequestFeedFacadeTopicMessage)
}

object TttsFeedService extends LazyLogging {

  case class CustomException(smth:String) extends Exception

  import FeedServiceJsonProtocol._
  
  type TsaM = (String, Producer[RequestFeedFacadeTopicMessage])
  
  def apply(): Duct[FacadeTopicMessage, TsaM] = Duct[FacadeTopicMessage].
    // acknowledge and pass on
    map { msg =>
      val z = msg.msgType 
      logger.debug("------- duck 1; message Type is: {}", z)
      msg
    }.
    
    map { msg =>
      val x = msg.client 
      logger.debug("------- duck 2; converting FacadeTopicMessage to RequestFeedFacadeTopicMessage for Client is: {}", x)
      msg
    }.

    map { //msg =>
        logger.debug("------- duck 3; starting feed")
        FeedService.startFeed
//      RequestFeedFacadeTopicMessage(msg.id, msg.msgType, msg.client, msg.payload)
    }.
    
    groupBy {
      case msg: TttsFeedMessage => "Outloop"
    }
    
}

class TttsFeedService extends Actor with ActorLogging {
  
  import TttsFeedService._
  
    override val supervisorStrategy = OneForOneStrategy(loggingEnabled = true) {
	    case e: CustomException =>
	      log.error("@@@@@@@@@@@@@@@ TttsFeedService Unexpected failure: {}", e.getMessage)
	      Stop
	    case e: Exception =>
	      log.error("@@@@@@@@@@@@@@@ TttsFeedService Unexpected failure: {}", e.getMessage)
	      Restart
  	}
  
    
    private def startService() = {
	  implicit val timeout = Timeout(2 seconds)
	  implicit val executor = context.dispatcher
	  
	  val materializer = FlowMaterializer(MaterializerSettings())
	  
	  val feedFacadeActor = context.actorOf(Props(classOf[FeedActor]), "feedFacadeConsumer")
	  log.debug("+++++++ feedFacadeActor is: {}", feedFacadeActor)
	  val feedFacadeConsumer = ActorProducer(feedFacadeActor)
	
	  // Start Kafka consumer actor for incoming messages from Facade Topic
	  val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(feedFacadeActor), "kafkaFacadeConsumer")
	  log.debug("+++++++ KafkaFacadeTopicConsumerActor tttsFeedActorSystem is: {}", kafkaFacadeTopicConsumerActor)
	  kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage


	  val feedServicesActor = context.actorOf(Props(classOf[FeedActor]), "feedServicesConsumer")
	  log.debug("+++++++ feedServicesActor is: {}", feedServicesActor)
	  val feedServicesConsumer = ActorProducer(feedServicesActor)
	
	  // Start Kafka consumer actor for incoming messages from Services Topic
	  val kafkaServicesTopicConsumerActor = context.actorOf(KafkaServicesTopicConsumerActor.props(feedServicesActor), "kafkaServicesConsumer")
	  log.debug("+++++++ KafkaServicesTopicConsumerActor tttsFeedActorSystem is: {}", kafkaServicesTopicConsumerActor)
	  kafkaServicesTopicConsumerActor ! StartListeningFacadeTopicMessage
	  
	  
	  
//	  val feedPublisherDuct = new FeedKafkaPublisher.flow
	  
	  val feeds = mutable.Map[String, ActorRef]()
      val feedPublisherDuct: Duct[RequestFeedFacadeTopicMessage, Unit] = 
	    Duct[RequestFeedFacadeTopicMessage] foreach {msg =>

	      /*
	       * For every new feed request add client -> feedActor to the Map
	       * For every feed termination request, find feedActor in the Map, stop it and remove entry from the Map 
	       */
	      msg.msgType match {
	        case "FEED_REQ" => {
	        	log.debug("kkkkk got FEED_REQ. Key {}", msg.client)	          
			    val feedGeneratorActor = context.actorOf(Props(classOf[FeedGeneratorActor]))
			    log.debug("kkkkk Starting Feed Generator Actor. Key {}; ActorRef {}", msg.client, feedGeneratorActor)
			    feeds += (msg.client -> feedGeneratorActor)
			    feedGeneratorActor ! msg
	        }
	        case "FEED_STOP_REQ" => {
	        	log.debug("kkkkk got FEED_STOP_REQ. Key {}", msg.client)	
	        	feeds.foreach { case (key, value) => log.debug("zzzzz key: {} ==> value: {}", key, value) }
	        	
		        feeds.get(msg.client) match {
				  case Some(feedGenActor) => {
				    log.debug("kkkkk Stopping Feed Generator Actor. Key {}; ActorRef {}", msg.client, feedGenActor)
				    feedGenActor ! StopFeedGeneratorMessage
				    feeds -= msg.client 
				  }
				  case None => log.debug("No such TttsFeedService feed to stop. Key {}", msg.client)
				}
	          
	        }
	      }
	      
	    }

	  
	  val feedServiceDuct = TttsFeedService()
	
	      Flow(feedFacadeConsumer) append feedServiceDuct map {
	
	        case (str, producer) => 
//		  	log.debug("~~~~~~~ And inside the main Flow is: {}", producer)
		  	
		  	// For every message start new Flow that produces feed
		  	
		  	
//		  	val kafkaProducerActor = context.actorOf(KafkaProducerActor.props(new InetSocketAddress("127.0.0.1", 5672)))
//		  	kafkaProducerActor ! msg

	    
	        // start a new flow for each message type
	        Flow(producer)
	        
	          // extract the client
//	          .map(_.client) 
	        
	          // add the outbound publishing duct
	          .append(feedPublisherDuct)
	          
	          // and start the flow
	          .consume(materializer)
	        
	    } consume(materializer)
      
    }
  
	override def receive = {
		case StartFeedServiceMessage => startService()
		case StopFeedServiceMessage => {
			log.debug("%%%%% TttsFeedService StopMessage")
		}
		case _ => log.error("%%%%% TttsFeedService Received unknown message")
	}
  
}
