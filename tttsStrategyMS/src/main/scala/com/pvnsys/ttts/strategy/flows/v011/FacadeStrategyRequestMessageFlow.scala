package com.pvnsys.ttts.strategy.flows.v011


import akka.stream.{FlowMaterializer, MaterializerSettings}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.{TttsStrategyMessage, FacadeTopicMessage, RequestStrategyFacadeTopicMessage}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.generator.StrategyService
import akka.actor.{ActorRef, ActorContext, Props}
import scala.collection.mutable
import scala.collection.mutable.Map
import com.pvnsys.ttts.strategy.generator.StrategyExecutorActor.StopStrategyExecutorMessage
import com.pvnsys.ttts.strategy.generator.StrategyExecutorActor
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.pvnsys.ttts.strategy.flows.StrategyServiceFlow
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import akka.actor.ActorSystem


object FacadeStrategyRequestMessageFlow extends LazyLogging {
}


class FacadeStrategyRequestMessageFlow(source: Source[TttsStrategyMessage], sink: Sink[TttsStrategyMessage])(implicit factory: ActorSystem) extends StrategyServiceFlow with LazyLogging {
  
	import FacadeStrategyRequestMessageFlow._
	import TttsStrategyMessages._

    implicit val materializer = FlowMaterializer()
  	
	override def startFlow() = {
		source.
	    map { msg =>
	      val messageType = msg match {
	        case x: RequestStrategyFacadeTopicMessage => x.msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.debug("*******>> Step 0: StrategyMS FacadeStrategyRequestMessageFlow Initialized. Received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      	  
	          logger.debug("*******>> Step 1: Strategy FacadeStrategyRequestMessageFlow processing message {}", msg)
		      msg match {
		        case x: RequestStrategyFacadeTopicMessage => // Nothing for Strategy messages 
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 2: Strategy FacadeStrategyRequestMessageFlow processing {}", msg)
		      msg match {
		        case x: RequestStrategyFacadeTopicMessage => // Nothing for Strategy messages 
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 3: Strategy FacadeStrategyRequestMessageFlow processing RequestStrategyFacadeTopicMessage {}", msg)
		      val outp = msg match {
		        case x: RequestStrategyFacadeTopicMessage => msg 
		        case _ => msg
		      }
	          outp
	    }.
		map { msg =>
	          val mess = RequestStrategyFacadeTopicMessage(msg.asInstanceOf[RequestStrategyFacadeTopicMessage].id, msg.asInstanceOf[RequestStrategyFacadeTopicMessage].msgType, msg.asInstanceOf[RequestStrategyFacadeTopicMessage].client, msg.asInstanceOf[RequestStrategyFacadeTopicMessage].payload, msg.asInstanceOf[RequestStrategyFacadeTopicMessage].timestamp, msg.asInstanceOf[RequestStrategyFacadeTopicMessage].sequenceNum)
	          logger.debug("*******>> Step 4: Strategy FacadeStrategyRequestMessageFlow converting the message {}", mess)
	          mess
		}.runWith(sink)	  
	}
  	

}