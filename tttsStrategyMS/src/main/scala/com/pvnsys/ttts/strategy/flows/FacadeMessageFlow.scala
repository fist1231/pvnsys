package com.pvnsys.ttts.strategy.flows

import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.{TttsStrategyMessage, FacadeTopicMessage, RequestStrategyFacadeTopicMessage}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.generator.StrategyService
import org.reactivestreams.api.Producer
import akka.actor.{ActorRef, ActorContext, Props}
import scala.collection.mutable
import scala.collection.mutable.Map
import com.pvnsys.ttts.strategy.generator.StrategyExecutorActor.StopStrategyExecutorMessage
import com.pvnsys.ttts.strategy.generator.StrategyExecutorActor
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages


object FacadeMessageFlow extends LazyLogging {

  type facadeMessageFlowOutDuctType = (String, Producer[RequestStrategyFacadeTopicMessage])
  
  def apply(): Duct[RequestStrategyFacadeTopicMessage, facadeMessageFlowOutDuctType] = Duct[RequestStrategyFacadeTopicMessage].
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
	        StrategyService.convertFacadeMessage
//	        RequestFeedFacadeTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum)
	    }.
	    
	    groupBy {
	      case msg: TttsStrategyMessage => "Outloop"
	    }
  
}


class FacadeMessageFlow(strategyFacadeActor: ActorRef, serviceUniqueID: String)(implicit context: ActorContext) extends StrategyServiceFlow with LazyLogging {
  
	import FacadeMessageFlow._
	import TttsStrategyMessages._

	implicit val executor = context.dispatcher
    val materializer = FlowMaterializer(MaterializerSettings())
	
	
    val strategyFacadeConsumer = ActorProducer(strategyFacadeActor)
  	val facadeMessageDuct = FacadeMessageFlow()
  	
	val strategies = mutable.Map[String, ActorRef]()
	val strategyPublisherDuct: Duct[RequestStrategyFacadeTopicMessage, Unit] = 
			Duct[RequestStrategyFacadeTopicMessage] foreach {msg =>
				  /*
				   * For every new feed request add client -> feedActor to the Map
				   * For every feed termination request, find feedActor in the Map, stop it and remove entry from the Map 
				   */
				  msg.msgType match {
					    case STRATEGY_REQUEST_MESSAGE_TYPE => {
					    	logger.debug("Got STRATEGY_REQ. Key {}", msg.client)	          
						    val strategyExecutorActor = context.actorOf(StrategyExecutorActor.props(serviceUniqueID))
						    logger.debug("Starting StrategyExecutorActor. Key {}; ActorRef {}", msg.client, strategyExecutorActor)
						    strategies += (msg.client -> strategyExecutorActor)
						    strategyExecutorActor ! msg
					    }
					    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => {
					    	logger.debug("Got STRATEGY_STOP_REQ. Key {}", msg.client)	
//					    	feeds.foreach { case (key, value) => logger.debug(" key: {} ==> value: {}", key, value) }
					    	
					        strategies.get(msg.client) match {
							  case Some(strategyExecutorActor) => {
							    logger.debug("Stopping StrategyExecutorActor. Key {}; ActorRef {}", msg.client, strategyExecutorActor)
							    strategyExecutorActor ! msg
							    strategyExecutorActor ! StopStrategyExecutorMessage
							    strategies -= msg.client 
							  }
							  case None => logger.debug("No such StrategyExecutorActor strategy to stop. Key {}", msg.client)
							}
					    }
				  }
			}
  	
  	
  	
	override def startFlow() = Flow(strategyFacadeConsumer) append facadeMessageDuct map {
		case (str, producer) => 
		// start a new flow for each message type
		Flow(producer)
			// extract the client
			//	          .map(_.client) 
			// add the outbound publishing duct
			.append(strategyPublisherDuct)
			// and start the flow
			.consume(materializer)
	    
	} consume(materializer)
  	

}