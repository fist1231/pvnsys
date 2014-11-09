package com.pvnsys.ttts.engine.generator

import com.pvnsys.ttts.engine.Configuration
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import com.typesafe.scalalogging.slf4j.LazyLogging


object EngineService {
  
  import TttsEngineMessages._

  def convertFacadeMessage(msg: RequestEngineFacadeTopicMessage): RequestEngineFacadeTopicMessage = {
     RequestEngineFacadeTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum)
  }

  def convertServicesMessage(msg: TttsEngineMessage): TttsEngineMessage = {
	    msg match {
	        case x: RequestEngineServicesTopicMessage => {
	          x.asInstanceOf[RequestEngineServicesTopicMessage].msgType match {
	            case ENGINE_REQUEST_MESSAGE_TYPE => RequestEngineServicesTopicMessage(msg.asInstanceOf[RequestEngineServicesTopicMessage].id, msg.asInstanceOf[RequestEngineServicesTopicMessage].msgType, msg.asInstanceOf[RequestEngineServicesTopicMessage].client, msg.asInstanceOf[RequestEngineServicesTopicMessage].payload, msg.asInstanceOf[RequestEngineServicesTopicMessage].timestamp, msg.asInstanceOf[RequestEngineServicesTopicMessage].sequenceNum, msg.asInstanceOf[RequestEngineServicesTopicMessage].serviceId)
	            case ENGINE_STOP_REQUEST_MESSAGE_TYPE => RequestEngineServicesTopicMessage(msg.asInstanceOf[RequestEngineServicesTopicMessage].id, msg.asInstanceOf[RequestEngineServicesTopicMessage].msgType, msg.asInstanceOf[RequestEngineServicesTopicMessage].client, msg.asInstanceOf[RequestEngineServicesTopicMessage].payload, msg.asInstanceOf[RequestEngineServicesTopicMessage].timestamp, msg.asInstanceOf[RequestEngineServicesTopicMessage].sequenceNum, msg.asInstanceOf[RequestEngineServicesTopicMessage].serviceId)
	            case _ => msg
	          }
	        }
//	        case x: ResponseFeedFacadeTopicMessage => {
//	          x.asInstanceOf[ResponseFeedFacadeTopicMessage].msgType match {
//	            case FEED_RESPONSE_MESSAGE_TYPE => ResponseFeedFacadeTopicMessage(msg.asInstanceOf[ResponseFeedFacadeTopicMessage].id, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].msgType, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].client, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].payload, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].timestamp, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].sequenceNum, msg.asInstanceOf[ResponseFeedFacadeTopicMessage].serviceId)
//	            case _ => msg
//	          }
//	        }
//	        case x: ResponseFeedServicesTopicMessage => {
//	          x.asInstanceOf[ResponseFeedServicesTopicMessage].msgType match {
//	            case FEED_RESPONSE_MESSAGE_TYPE => ResponseFeedServicesTopicMessage(msg.asInstanceOf[ResponseFeedServicesTopicMessage].id, msg.asInstanceOf[ResponseFeedServicesTopicMessage].msgType, msg.asInstanceOf[ResponseFeedServicesTopicMessage].client, msg.asInstanceOf[ResponseFeedServicesTopicMessage].payload, msg.asInstanceOf[ResponseFeedServicesTopicMessage].timestamp, msg.asInstanceOf[ResponseFeedServicesTopicMessage].sequenceNum, msg.asInstanceOf[ResponseFeedServicesTopicMessage].serviceId)
//	            case _ => msg
//	          }
//	        }
	        case _ => msg
	    }
     
  }

}
