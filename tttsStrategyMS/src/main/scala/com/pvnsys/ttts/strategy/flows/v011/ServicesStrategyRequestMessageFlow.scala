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


class ServicesStrategyRequestMessageFlow(servicesStrategyRequestFlowSource: Source[TttsStrategyMessage], facadeStrategyRequestFlowSink: Sink[TttsStrategyMessage], servicesStrategyRequestFlowSink: Sink[TttsStrategyMessage], serviceUniqueID: String)(implicit factory: ActorSystem) extends StrategyServiceFlow with LazyLogging {
  
	import FacadeStrategyRequestMessageFlow._
	import TttsStrategyMessages._

//	implicit val executor = context.dispatcher
    implicit val materializer = FlowMaterializer()
	
	
  	
	override def startFlow() = {
		servicesStrategyRequestFlowSource.
	    map { msg =>
	      val messageType = msg match {
	        case x: RequestStrategyServicesTopicMessage => x.msgType 
	        case x: ResponseFeedFacadeTopicMessage => x.msgType 
	        case x: ResponseFeedServicesTopicMessage => x.msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.debug("*******>> Step 0: StrategyMS ServicesStrategyRequestMessageFlow Initialized. Received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      	  
	          logger.debug("*******>> Step 1: Strategy ServicesStrategyRequestMessageFlow Creating schema for first Feed Response message {}", msg)
		      msg match {
		        case x: RequestStrategyServicesTopicMessage => // Nothing for Strategy messages 
		        case x: ResponseFeedFacadeTopicMessage => {
		          x.sequenceNum match {
		            case "1" => new AbxStrategyImpl().createSchema(serviceUniqueID, msg)
		            case _ => // Do nothing
		          }
 
		        }
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
	      	  
	          logger.debug("*******>> Step 2: Strategy ServicesStrategyRequestMessageFlow Write quotes feed data to db {}", msg)
		      msg match {
		        case x: RequestStrategyServicesTopicMessage => // Nothing for Strategy messages 
		        case x: ResponseFeedFacadeTopicMessage => {
		            new AbxStrategyImpl().writeQuotesData(serviceUniqueID, msg)
		        }
		        case x: ResponseFeedServicesTopicMessage => {
		            new AbxStrategyImpl().writeQuotesData(serviceUniqueID, msg)
		        }
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 3: Strategy ServicesStrategyRequestMessageFlow Apply strategy logic to the quotes feed {}", msg)
		      val outp = msg match {
		        case x: RequestStrategyServicesTopicMessage => msg
		        case x: ResponseFeedFacadeTopicMessage => {
		            new AbxStrategyImpl().applyStrategy(serviceUniqueID, msg)
		        }
		        case x: ResponseFeedServicesTopicMessage => {
		            new AbxStrategyImpl().applyStrategy(serviceUniqueID, msg)
		        }
		        case _ => msg
		      }
	          outp
	    }.
		map { msg =>
			logger.debug("*******>> Step 4: Strategy ServicesStrategyRequestMessageFlow converting the message {}", msg)
			msg match {
		        case x: RequestStrategyServicesTopicMessage => {
		          x.asInstanceOf[RequestStrategyServicesTopicMessage].msgType match {
		            case STRATEGY_REQUEST_MESSAGE_TYPE => RequestStrategyServicesTopicMessage(msg.asInstanceOf[RequestStrategyServicesTopicMessage].id, msg.asInstanceOf[RequestStrategyServicesTopicMessage].msgType, msg.asInstanceOf[RequestStrategyServicesTopicMessage].client, msg.asInstanceOf[RequestStrategyServicesTopicMessage].payload, msg.asInstanceOf[RequestStrategyServicesTopicMessage].timestamp, msg.asInstanceOf[RequestStrategyServicesTopicMessage].sequenceNum, msg.asInstanceOf[RequestStrategyServicesTopicMessage].serviceId)
		            case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => RequestStrategyServicesTopicMessage(msg.asInstanceOf[RequestStrategyServicesTopicMessage].id, msg.asInstanceOf[RequestStrategyServicesTopicMessage].msgType, msg.asInstanceOf[RequestStrategyServicesTopicMessage].client, msg.asInstanceOf[RequestStrategyServicesTopicMessage].payload, msg.asInstanceOf[RequestStrategyServicesTopicMessage].timestamp, msg.asInstanceOf[RequestStrategyServicesTopicMessage].sequenceNum, msg.asInstanceOf[RequestStrategyServicesTopicMessage].serviceId)
		            case _ => msg
		          }
		        }
		        case x: ResponseFeedFacadeTopicMessage => {
		          x.asInstanceOf[ResponseFeedFacadeTopicMessage].msgType match {
		            case FEED_RESPONSE_MESSAGE_TYPE => ResponseFeedFacadeTopicMessage(msg.asInstanceOf[ResponseFeedFacadeTopicMessage].id, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].msgType, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].client, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].payload, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].timestamp, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].sequenceNum, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].serviceId)
		            case _ => msg
		          }
		        }
		        case x: ResponseFeedServicesTopicMessage => {
		          x.asInstanceOf[ResponseFeedServicesTopicMessage].msgType match {
		            case FEED_RESPONSE_MESSAGE_TYPE => ResponseFeedServicesTopicMessage(msg.asInstanceOf[ResponseFeedServicesTopicMessage].id, msg.asInstanceOf[ResponseFeedServicesTopicMessage].msgType, msg.asInstanceOf[ResponseFeedServicesTopicMessage].client, msg.asInstanceOf[ResponseFeedServicesTopicMessage].payload, msg.asInstanceOf[ResponseFeedServicesTopicMessage].timestamp, msg.asInstanceOf[ResponseFeedServicesTopicMessage].sequenceNum, msg.asInstanceOf[ResponseFeedServicesTopicMessage].serviceId)
		            case _ => msg
		          }
		        }
		        case x: ResponseStrategyFacadeTopicMessage => x
		        case x: ResponseStrategyServicesTopicMessage => x
		        case _ => msg
			}
//            mess
		}.
//		groupBy {
//		      case msg: RequestStrategyServicesTopicMessage => "ServicesStrategyRequestMessage"
//		      case msg: ResponseStrategyFacadeTopicMessage => {
//		        logger.debug("*******>> Step 5: Strategy ServicesMessageFlow Groupby operation on ResponseStrategyFacadeTopicMessage")
//		        "FacadeStrategyResponseMessage"
//		      }
//		      case msg: ResponseStrategyServicesTopicMessage => {
//		        logger.debug("*******>> Step 5: Strategy ServicesMessageFlow Groupby operation on ResponseStrategyServicesTopicMessage")
//		        "ServicesStrategyResponseMessage"
//		      }
//		      case _ => "Garbage"
//		}.
//		map {
//		  case (str, source) => 
//		    str match {
//		    	case "ServicesStrategyRequestMessage" => source.runWith(servicesStrategyRequestFlowSink)
//		        
//		    	case "FacadeStrategyResponseMessage" => source.runWith(facadeStrategyRequestFlowSink)
//		    	case "ServicesStrategyResponseMessage" => source.runWith(servicesStrategyRequestFlowSink)
//		  }
//		  
//		}
		runWith { 
		  facadeStrategyRequestFlowSink
		}
	}
  	

}