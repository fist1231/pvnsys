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
import com.pvnsys.ttts.feed.mq.KafkaFacadeTopicConsumerActor
import com.pvnsys.ttts.feed.generator.FeedGeneratorActor
import com.pvnsys.ttts.feed.generator.FeedService

object FeedServiceJsonProtocol extends DefaultJsonProtocol {
  implicit val facadeTopicMessageFormat = jsonFormat4(FacadeTopicMessage)
  implicit val requestFeedFacadeTopicMessageFormat = jsonFormat4(RequestFeedFacadeTopicMessage)
}

object TttsStrategyService extends LazyLogging {

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

class TttsStrategyService extends Actor with ActorLogging {
  
  import TttsStrategyService._
  
    override val supervisorStrategy = OneForOneStrategy(loggingEnabled = true) {
	    case e: Exception =>
	      log.error("@@@@@@@@@@@@@@@ TttsFeedService Unexpected failure: {}", e.getMessage)
	      Restart
	    case e: CustomException =>
	      log.error("@@@@@@@@@@@@@@@ TttsFeedService Unexpected failure: {}", e.getMessage)
	      Stop
  	}
  
    
    private def startService() = {
	  implicit val timeout = Timeout(2 seconds)
	  implicit val executor = context.dispatcher
	  
	  val materializer = FlowMaterializer(MaterializerSettings())
	  
	  val feedActor = context.actorOf(Props(classOf[FeedActor]), "feedConsumer")
	  log.debug("+++++++ feedActor is: {}", feedActor)
	  val feedConsumer = ActorProducer(feedActor)
	
	  val kafkaFacadeTopicConsumerActor = context.actorOf(KafkaFacadeTopicConsumerActor.props(feedActor), "kafkaConsumer")
	  log.debug("+++++++ KafkaFacadeTopicConsumerActor tttsFeedActorSystem is: {}", kafkaFacadeTopicConsumerActor)
	  kafkaFacadeTopicConsumerActor ! StartListeningFacadeTopicMessage
	  
	  
//	  val feedPublisherDuct = new FeedKafkaPublisher.flow
	  
      val feedPublisherDuct: Duct[RequestFeedFacadeTopicMessage, Unit] = 
	    Duct[RequestFeedFacadeTopicMessage] foreach {msg => 
		    val feedGeneratorActor = context.actorOf(Props(classOf[FeedGeneratorActor]))
		    feedGeneratorActor ! msg
	    }

	  
	  val feedServiceDuct = TttsFeedService()
	
	      Flow(feedConsumer) append feedServiceDuct map {
	
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
