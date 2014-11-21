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


object ServicesStrategyRequestMessageFlow extends LazyLogging {
}


class ServicesStrategyRequestMessageFlow(source: Source[TttsStrategyMessage], sink: Sink[TttsStrategyMessage])(implicit factory: ActorSystem) extends StrategyServiceFlow with LazyLogging {
  
	import FacadeStrategyRequestMessageFlow._
	import TttsStrategyMessages._

    implicit val materializer = FlowMaterializer()
  	
	override def startFlow() = {
		source.
	    map { msg =>
	      val messageType = msg match {
	        case x: RequestStrategyServicesTopicMessage => x.msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.debug("*******>> Step 0: StrategyMS ServicesStrategyRequestMessageFlow Initialized. Received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      	  
	          logger.debug("*******>> Step 1: Strategy ServicesStrategyRequestMessageFlow processing RequestStrategyServicesTopicMessage {}", msg)
		      msg match {
		        case x: RequestStrategyServicesTopicMessage => // Nothing for Strategy messages 
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 2: Strategy ServicesStrategyRequestMessageFlow processing RequestStrategyServicesTopicMessage {}", msg)
		      msg match {
		        case x: RequestStrategyServicesTopicMessage => // Nothing for Strategy messages 
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 3: Strategy ServicesStrategyRequestMessageFlow processing RequestStrategyServicesTopicMessage {}", msg)
		      val outp = msg match {
		        case x: RequestStrategyServicesTopicMessage => msg
		        case _ => msg
		      }
	          outp
	    }.
		map { msg =>
			logger.debug("*******>> Step 4: Strategy ServicesStrategyRequestMessageFlow converting RequestStrategyServicesTopicMessage {}", msg)
			msg match {
		        case x: RequestStrategyServicesTopicMessage => {
		          x.asInstanceOf[RequestStrategyServicesTopicMessage].msgType match {
		            case STRATEGY_REQUEST_MESSAGE_TYPE => RequestStrategyServicesTopicMessage(msg.asInstanceOf[RequestStrategyServicesTopicMessage].id, msg.asInstanceOf[RequestStrategyServicesTopicMessage].msgType, msg.asInstanceOf[RequestStrategyServicesTopicMessage].client, msg.asInstanceOf[RequestStrategyServicesTopicMessage].payload, msg.asInstanceOf[RequestStrategyServicesTopicMessage].timestamp, msg.asInstanceOf[RequestStrategyServicesTopicMessage].sequenceNum, msg.asInstanceOf[RequestStrategyServicesTopicMessage].serviceId)
		            case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => RequestStrategyServicesTopicMessage(msg.asInstanceOf[RequestStrategyServicesTopicMessage].id, msg.asInstanceOf[RequestStrategyServicesTopicMessage].msgType, msg.asInstanceOf[RequestStrategyServicesTopicMessage].client, msg.asInstanceOf[RequestStrategyServicesTopicMessage].payload, msg.asInstanceOf[RequestStrategyServicesTopicMessage].timestamp, msg.asInstanceOf[RequestStrategyServicesTopicMessage].sequenceNum, msg.asInstanceOf[RequestStrategyServicesTopicMessage].serviceId)
		            case _ => msg
		          }
		        }
		        case _ => msg
			}
		}.
		runWith(sink) 
	}
  	

}