package com.pvnsys.ttts.engine.flows.v011


import akka.stream.{FlowMaterializer, MaterializerSettings}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages.TttsEngineMessage
import com.typesafe.scalalogging.slf4j.LazyLogging
//import com.pvnsys.ttts.engine.generator.EngineService
import akka.actor.{ActorRef, ActorContext, Props}
//import scala.collection.mutable
//import scala.collection.mutable.Map
//import com.pvnsys.ttts.engine.generator.EngineExecutorActor.StopEngineExecutorMessage
//import com.pvnsys.ttts.engine.generator.EngineExecutorActor
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import com.pvnsys.ttts.engine.flows.EngineServiceFlow
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import akka.actor.ActorSystem
import com.pvnsys.ttts.engine.messages.TttsEngineMessages


object FacadeEngineRequestMessageFlow extends LazyLogging {
}


class FacadeEngineRequestMessageFlow(source: Source[TttsEngineMessage], sink: Sink[TttsEngineMessage])(implicit factory: ActorSystem) extends EngineServiceFlow with LazyLogging {
  
	import FacadeEngineRequestMessageFlow._
	import TttsEngineMessages._

    implicit val materializer = FlowMaterializer()
  	
	override def startFlow() = {
		source.
	    map { msg =>
	      val messageType = msg match {
	        case x: RequestEngineFacadeTopicMessage => x.msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.debug("*******>> Step 0: EngineMS FacadeEngineRequestMessageFlow Initialized. Received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      	  
	          logger.debug("*******>> Step 1: Engine FacadeEngineRequestMessageFlow processing message {}", msg)
		      msg match {
		        case x: RequestEngineFacadeTopicMessage => // Nothing for Engine messages 
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 2: Engine FacadeEngineRequestMessageFlow processing {}", msg)
		      msg match {
		        case x: RequestEngineFacadeTopicMessage => // Nothing for Engine messages 
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 3: Engine FacadeEngineRequestMessageFlow processing RequestEngineFacadeTopicMessage {}", msg)
		      val outp = msg match {
		        case x: RequestEngineFacadeTopicMessage => msg 
		        case _ => msg
		      }
	          outp
	    }.
		map { msg =>
	          val mess = RequestEngineFacadeTopicMessage(msg.asInstanceOf[RequestEngineFacadeTopicMessage].id, msg.asInstanceOf[RequestEngineFacadeTopicMessage].msgType, msg.asInstanceOf[RequestEngineFacadeTopicMessage].client, msg.asInstanceOf[RequestEngineFacadeTopicMessage].payload, msg.asInstanceOf[RequestEngineFacadeTopicMessage].timestamp, msg.asInstanceOf[RequestEngineFacadeTopicMessage].sequenceNum)
	          logger.debug("*******>> Step 4: Engine FacadeEngineRequestMessageFlow converting the message {}", mess)
	          mess
		}.runWith(sink)	  
	}
  	

}