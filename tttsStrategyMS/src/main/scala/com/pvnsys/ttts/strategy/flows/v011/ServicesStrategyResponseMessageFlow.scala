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


object ServicesStrategyResponseMessageFlow extends LazyLogging {
}


class ServicesStrategyResponseMessageFlow(servicesStrategyRequestFlowSource: Source[TttsStrategyMessage], servicesStrategyRequestFlowSink: Sink[TttsStrategyMessage], serviceUniqueID: String)(implicit factory: ActorSystem) extends StrategyServiceFlow with LazyLogging {
  
	import ServicesStrategyResponseMessageFlow._
	import TttsStrategyMessages._

//	implicit val executor = context.dispatcher
    implicit val materializer = FlowMaterializer()
	
	
  	
	override def startFlow() = {
		servicesStrategyRequestFlowSource.
	    map { msg =>
	      val messageType = msg match {
	        case x: ResponseFeedServicesTopicMessage => x.msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.debug("*******>> Step 0: StrategyMS ServicesStrategyResponseMessageFlow Initialized. Received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      	  
	          logger.debug("*******>> Step 1: Strategy ServicesStrategyResponseMessageFlow Creating schema for first Feed Response message {}", msg)
		      msg match {
		        case x: ResponseFeedServicesTopicMessage => {
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
	      	  
	          logger.debug("*******>> Step 2: Strategy ServicesStrategyResponseMessageFlow Write quotes feed data to db {}", msg)
		      msg match {
		        case x: ResponseFeedServicesTopicMessage => {
		            new AbxStrategyImpl().writeQuotesData(serviceUniqueID, msg)
		        }
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 3: Strategy ServicesStrategyResponseMessageFlow Apply strategy logic to the quotes feed {}", msg)
		      val outp = msg match {
		        case x: ResponseFeedServicesTopicMessage => {
		            new AbxStrategyImpl().applyStrategy(serviceUniqueID, msg)
		        }
		        case _ => msg
		      }
	          outp
	    }.
		map { msg =>
            logger.debug("*******>> Step 4: Strategy ServicesStrategyResponseMessageFlow passing message through: {}", msg)
	        msg
		}.runWith(servicesStrategyRequestFlowSink)
	}
  	

}