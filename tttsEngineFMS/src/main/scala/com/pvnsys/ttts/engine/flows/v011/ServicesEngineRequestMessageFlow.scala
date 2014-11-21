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


object ServicesEngineRequestMessageFlow extends LazyLogging {
}


class ServicesEngineRequestMessageFlow(source: Source[TttsEngineMessage], sink: Sink[TttsEngineMessage])(implicit factory: ActorSystem) extends EngineServiceFlow with LazyLogging {
  
	import FacadeEngineRequestMessageFlow._
	import TttsEngineMessages._

    implicit val materializer = FlowMaterializer()
  	
	override def startFlow() = {
		source.
	    map { msg =>
	      val messageType = msg match {
	        case x: RequestEngineServicesTopicMessage => x.msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.debug("*******>> Step 0: EngineMS ServicesEngineRequestMessageFlow Initialized. Received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      	  
	          logger.debug("*******>> Step 1: Engine ServicesEngineRequestMessageFlow processing RequestEngineServicesTopicMessage {}", msg)
		      msg match {
		        case x: RequestEngineServicesTopicMessage => // Nothing for Engine messages 
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 2: Engine ServicesEngineRequestMessageFlow processing RequestEngineServicesTopicMessage {}", msg)
		      msg match {
		        case x: RequestEngineServicesTopicMessage => // Nothing for Engine messages 
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 3: Engine ServicesEngineRequestMessageFlow processing RequestEngineServicesTopicMessage {}", msg)
		      val outp = msg match {
		        case x: RequestEngineServicesTopicMessage => msg
		        case _ => msg
		      }
	          outp
	    }.
		map { msg =>
			logger.debug("*******>> Step 4: Engine ServicesEngineRequestMessageFlow converting RequestEngineServicesTopicMessage {}", msg)
			msg match {
		        case x: RequestEngineServicesTopicMessage => {
		          x.msgType match {
		            case ENGINE_REQUEST_MESSAGE_TYPE => RequestEngineServicesTopicMessage(x.id, x.msgType, x.client, x.payload, x.timestamp,x.sequenceNum, x.serviceId)
		            case ENGINE_STOP_REQUEST_MESSAGE_TYPE => RequestEngineServicesTopicMessage(x.id, x.msgType ,x.client, x.payload, x.timestamp, x.sequenceNum, x.serviceId)
		            case _ => msg
		          }
		        }
		        case _ => msg
			}
		}.
		runWith(sink) 
	}
  	

}