package com.pvnsys.ttts.strategy.generator

import com.pvnsys.ttts.strategy.Configuration
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages


object StrategyService {
  
  import TttsStrategyMessages._

  def convertFacadeMessage(msg: RequestStrategyFacadeTopicMessage): RequestStrategyFacadeTopicMessage = {
     RequestStrategyFacadeTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum)
  }

  def convertServicesMessage(msg: TttsStrategyMessage): TttsStrategyMessage = {
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
     
  }

}
