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
import com.pvnsys.ttts.strategy.impl.AbxStrategyImpl


object FacadeStrategyResponseMessageFlow extends LazyLogging {
}


class FacadeStrategyResponseMessageFlow(source: Source[TttsStrategyMessage], sink: Sink[TttsStrategyMessage], serviceUniqueID: String)(implicit factory: ActorSystem) extends StrategyServiceFlow with LazyLogging {
  
	import FacadeStrategyResponseMessageFlow._
	import TttsStrategyMessages._

    implicit val materializer = FlowMaterializer()
  	
	override def startFlow() = {
		source.
	    map { msg =>
	      val messageType = msg match {
	        case x: ResponseFeedFacadeTopicMessage => x.msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.debug("*******>> Step 0: StrategyMS FacadeStrategyResponseMessageFlow Initialized. Received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      	  
	          logger.debug("*******>> Step 1: Strategy FacadeStrategyResponseMessageFlow Creating schema for first Feed Response message {}", msg)
		      msg match {
		        case x: ResponseFeedFacadeTopicMessage => {
		          x.sequenceNum match {
		            case "1" => new AbxStrategyImpl().createSchema(serviceUniqueID, msg)
		            case _ => // Do nothing
		          }
 
		        }
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 2: Strategy FacadeStrategyResponseMessageFlow Write quotes feed data to db {}", msg)
		      msg match {
		        case x: ResponseFeedFacadeTopicMessage => {
		            new AbxStrategyImpl().writeQuotesData(serviceUniqueID, msg)
		        }
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 3: Strategy FacadeStrategyResponseMessageFlow Apply strategy logic to the quotes feed {}", msg)
		      val outp = msg match {
		        case x: ResponseFeedFacadeTopicMessage => {
		            new AbxStrategyImpl().applyStrategy(serviceUniqueID, msg)
		        }
		        case _ => msg
		      }
	          outp
	    }.
		map { msg =>
            logger.debug("*******>> Step 4: Strategy FacadeStrategyResponseMessageFlow passing message through: {}", msg)
	        msg
		}.runWith(sink)
	}
  	

}