package com.pvnsys.ttts.engine.flows

import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages.{TttsEngineMessage, FacadeTopicMessage, RequestEngineFacadeTopicMessage}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.engine.generator.EngineService
import org.reactivestreams.api.Producer
import akka.actor.{ActorRef, ActorContext, Props}
import scala.collection.mutable
import scala.collection.mutable.Map
import com.pvnsys.ttts.engine.generator.EngineExecutorActor.StopEngineExecutorMessage
import com.pvnsys.ttts.engine.generator.EngineExecutorActor
import com.pvnsys.ttts.engine.messages.TttsEngineMessages


object FacadeMessageFlow extends LazyLogging {

  type facadeMessageFlowOutDuctType = (String, Producer[RequestEngineFacadeTopicMessage])
  
  def apply(): Duct[RequestEngineFacadeTopicMessage, facadeMessageFlowOutDuctType] = Duct[RequestEngineFacadeTopicMessage].
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
	        EngineService.convertFacadeMessage
//	        RequestFeedFacadeTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum)
	    }.
	    
	    groupBy {
	      case msg: TttsEngineMessage => "Outloop"
	    }
  
}


class FacadeMessageFlow(engineFacadeActor: ActorRef, serviceUniqueID: String)(implicit context: ActorContext) extends EngineServiceFlow with LazyLogging {
  
	import FacadeMessageFlow._
	import TttsEngineMessages._

	implicit val executor = context.dispatcher
    val materializer = FlowMaterializer(MaterializerSettings())
	
	
    val engineFacadeConsumer = ActorProducer(engineFacadeActor)
  	val facadeMessageDuct = FacadeMessageFlow()
  	
	val strategies = mutable.Map[String, ActorRef]()
	val enginePublisherDuct: Duct[RequestEngineFacadeTopicMessage, Unit] = 
			Duct[RequestEngineFacadeTopicMessage] foreach {msg =>
				  /*
				   * For every new feed request add client -> feedActor to the Map
				   * For every feed termination request, find feedActor in the Map, stop it and remove entry from the Map 
				   */
				  msg.msgType match {
					    case ENGINE_REQUEST_MESSAGE_TYPE => {
					    	logger.debug("Got ENGINE_REQ. Key {}", msg.client)	          
						    val engineExecutorActor = context.actorOf(EngineExecutorActor.props(serviceUniqueID))
						    logger.debug("Starting EngineExecutorActor. Key {}; ActorRef {}", msg.client, engineExecutorActor)
						    strategies += (msg.client -> engineExecutorActor)
						    engineExecutorActor ! msg
					    }
					    case ENGINE_STOP_REQUEST_MESSAGE_TYPE => {
					    	logger.debug("Got ENGINE_STOP_REQ. Key {}", msg.client)	
//					    	feeds.foreach { case (key, value) => logger.debug(" key: {} ==> value: {}", key, value) }
					    	
					        strategies.get(msg.client) match {
							  case Some(engineExecutorActor) => {
							    logger.debug("Stopping EngineExecutorActor. Key {}; ActorRef {}", msg.client, engineExecutorActor)
							    engineExecutorActor ! msg
//							    engineExecutorActor ! StopEngineExecutorMessage
							    strategies -= msg.client 
							  }
							  case None => logger.debug("No such EngineExecutorActor engine to stop. Key {}", msg.client)
							}
					    }
				  }
			}
  	
  	
  	
	override def startFlow() = Flow(engineFacadeConsumer) append facadeMessageDuct map {
		case (str, producer) => 
		// start a new flow for each message type
		Flow(producer)
			// extract the client
			//	          .map(_.client) 
			// add the outbound publishing duct
			.append(enginePublisherDuct)
			// and start the flow
			.consume(materializer)
	    
	} consume(materializer)
  	

}