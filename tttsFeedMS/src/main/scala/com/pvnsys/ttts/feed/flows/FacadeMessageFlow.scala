package com.pvnsys.ttts.feed.flows

import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.{TttsFeedMessage, FacadeTopicMessage, RequestFeedFacadeTopicMessage}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.feed.generator.FeedService
import org.reactivestreams.api.Producer
import akka.actor.{ActorRef, ActorContext, Props}
import scala.collection.mutable
import scala.collection.mutable.Map
import com.pvnsys.ttts.feed.generator.FeedGeneratorActor.StopFeedGeneratorMessage
import com.pvnsys.ttts.feed.generator.FeedGeneratorActor
import com.pvnsys.ttts.feed.messages.TttsFeedMessages


object FacadeMessageFlow extends LazyLogging {

  type facadeMessageFlowOutDuctType = (String, Producer[RequestFeedFacadeTopicMessage])
  
  def apply(): Duct[FacadeTopicMessage, facadeMessageFlowOutDuctType] = Duct[FacadeTopicMessage].
	    // acknowledge and pass on
	    map { msg =>
	      val z = msg.msgType 
	      logger.debug("FacadeMessageFlow duct step 1; message Type is: {}", z)
	      msg
	    }.
	    
	    map { msg =>
	      val x = msg.client 
	      logger.debug("FacadeMessageFlow duct step 2; converting FacadeTopicMessage to RequestFeedFacadeTopicMessage for Client is: {}", x)
	      msg
	    }.
	
	    map { //msg =>
	        logger.debug("FacadeMessageFlow duct step 3; starting feed")
	        FeedService.convertFacadeMessage
//	        RequestFeedFacadeTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum)
	    }.
	    
	    groupBy {
	      case msg: TttsFeedMessage => "Outloop"
	    }
  
}


class FacadeMessageFlow(feedFacadeActor: ActorRef)(implicit context: ActorContext) extends FeedServiceFlow with LazyLogging {
  
	import FacadeMessageFlow._
	import TttsFeedMessages._

	implicit val executor = context.dispatcher
    val materializer = FlowMaterializer(MaterializerSettings())
	
	
    val feedFacadeConsumer = ActorProducer(feedFacadeActor)
  	val facadeMessageDuct = FacadeMessageFlow()
  	
	val feeds = mutable.Map[String, ActorRef]()
	val feedPublisherDuct: Duct[RequestFeedFacadeTopicMessage, Unit] = 
			Duct[RequestFeedFacadeTopicMessage] foreach {msg =>
				  /*
				   * For every new feed request add client -> feedActor to the Map
				   * For every feed termination request, find feedActor in the Map, stop it and remove entry from the Map 
				   */
				  msg.msgType match {
					    case FEED_REQUEST_MESSAGE_TYPE => {
					    	logger.debug("Got FEED_REQ. Key {}", msg.client)	          
						    val feedGeneratorActor = context.actorOf(Props(classOf[FeedGeneratorActor]))
						    logger.debug("Starting Feed Generator Actor. Key {}; ActorRef {}", msg.client, feedGeneratorActor)
						    feeds += (msg.client -> feedGeneratorActor)
						    feedGeneratorActor ! msg
					    }
					    case FEED_STOP_REQUEST_MESSAGE_TYPE => {
					    	logger.debug("Got FEED_STOP_REQ. Key {}", msg.client)	
//					    	feeds.foreach { case (key, value) => logger.debug(" key: {} ==> value: {}", key, value) }
					    	
					        feeds.get(msg.client) match {
							  case Some(feedGenActor) => {
							    logger.debug("Stopping Feed Generator Actor. Key {}; ActorRef {}", msg.client, feedGenActor)
							    feedGenActor ! StopFeedGeneratorMessage
							    feeds -= msg.client 
							  }
							  case None => logger.debug("No such TttsFeedService feed to stop. Key {}", msg.client)
							}
					    }
				  }
			}
  	
  	
  	
	override def startFlow() = Flow(feedFacadeConsumer) append facadeMessageDuct map {
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