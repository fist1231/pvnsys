package com.pvnsys.ttts.feed

import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy}
import akka.actor.SupervisorStrategy.{Restart, Stop, Escalate}
import akka.util.Timeout
import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import scala.concurrent.duration._
import com.pvnsys.ttts.feed.mq.KafkaConsumerActor

case object FeedServiceMessage
case class StartFeedServiceMessage()
case class StopFeedServiceMessage()

object TttsFeedService {
}

class TttsFeedService extends Actor with ActorLogging {

  
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
	
	  val kafkaConsumerActor = context.actorOf(KafkaConsumerActor.props(feedActor), "kafkaConsumer")
	  log.debug("+++++++ KafkaConsumerActor tttsFeedActorSystem is: {}", kafkaConsumerActor)
	  kafkaConsumerActor ! KafkaStartListeningMessage
	  
	  
	  val domainProcessingDuct = MyDomainProcessing()
	
	      Flow(feedConsumer) append domainProcessingDuct map { msg =>
	
		  	log.debug("~~~~~~~ And inside the main Flow is: {}", msg)
	    
	//        // start a new flow for each message type
	//        Flow(producer)
	//        
	//          // extract the message
	//          .map(_.message) 
	//          
	//          // add the outbound publishing duct
	//          .append(publisherDuct(exchange))
	//          
	//          // and start the flow
	//          .consume(materializer)
	        
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
