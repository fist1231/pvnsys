package com.pvnsys.ttts.engine.flows.v011


import akka.stream.{FlowMaterializer, MaterializerSettings}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages.TttsEngineMessage
import com.typesafe.scalalogging.slf4j.LazyLogging
import akka.actor.{ActorRef, ActorContext, Props}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import com.pvnsys.ttts.engine.flows.EngineServiceFlow
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import akka.actor.ActorSystem
import com.pvnsys.ttts.engine.impl.SimulatorEngineImpl


object FacadeEngineResponseMessageFlow extends LazyLogging {
}


class FacadeEngineResponseMessageFlow(source: Source[TttsEngineMessage], sink: Sink[TttsEngineMessage], serviceUniqueID: String)(implicit factory: ActorSystem) extends EngineServiceFlow with LazyLogging {
  
	import FacadeEngineResponseMessageFlow._
	import TttsEngineMessages._

    implicit val materializer = FlowMaterializer()
  	
	override def startFlow() = {
		source.
	    map { msg =>
	      val messageType = msg match {
	        case x: ResponseStrategyFacadeTopicMessage => x.msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.debug("*******>> Step 0: EngineMS FacadeEngineResponseMessageFlow Initialized. Received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      	  
	          logger.debug("*******>> Step 1: Engine FacadeEngineResponseMessageFlow Creating schema for first Feed Response message {}", msg)
		      msg match {
		        case x: ResponseStrategyFacadeTopicMessage => {
		          x.sequenceNum match {
		            case "1" => new SimulatorEngineImpl().createSchema(serviceUniqueID, msg)
		            case _ => // Do nothing
		          }
 		        }
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 2: Engine FacadeEngineResponseMessageFlow Apply engine logic to the quotes feed {}", msg)
		      val outp = msg match {
		        case x: ResponseStrategyFacadeTopicMessage => {
		            new SimulatorEngineImpl().applyEngine(serviceUniqueID, msg)
		        }
		        case _ => msg
		      }
	          outp
	    }.
		map { msg =>
            logger.debug("*******>> Step 3: Engine FacadeEngineResponseMessageFlow passing message through: {}", msg)
	        msg
		}.runWith(sink)
	}
  	

}