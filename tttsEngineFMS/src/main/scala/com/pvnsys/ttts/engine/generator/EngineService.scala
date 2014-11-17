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
	        case x: ResponseStrategyFacadeTopicMessage => {
	          x.asInstanceOf[ResponseStrategyFacadeTopicMessage].msgType match {
	            case STRATEGY_RESPONSE_MESSAGE_TYPE => ResponseStrategyFacadeTopicMessage(msg.asInstanceOf[ResponseStrategyFacadeTopicMessage].id, msg.asInstanceOf[ResponseStrategyFacadeTopicMessage].msgType, msg.asInstanceOf[ResponseStrategyFacadeTopicMessage].client, msg.asInstanceOf[ResponseStrategyFacadeTopicMessage].payload, msg.asInstanceOf[ResponseStrategyFacadeTopicMessage].timestamp, msg.asInstanceOf[ResponseStrategyFacadeTopicMessage].sequenceNum, msg.asInstanceOf[ResponseStrategyFacadeTopicMessage].signal, msg.asInstanceOf[ResponseStrategyFacadeTopicMessage].serviceId)
	            case _ => msg
	          }
	        }
	        case x: ResponseStrategyServicesTopicMessage => {
	          x.asInstanceOf[ResponseStrategyServicesTopicMessage].msgType match {
	            case STRATEGY_RESPONSE_MESSAGE_TYPE => ResponseStrategyServicesTopicMessage(msg.asInstanceOf[ResponseStrategyServicesTopicMessage].id, msg.asInstanceOf[ResponseStrategyServicesTopicMessage].msgType, msg.asInstanceOf[ResponseStrategyServicesTopicMessage].client, msg.asInstanceOf[ResponseStrategyServicesTopicMessage].payload, msg.asInstanceOf[ResponseStrategyServicesTopicMessage].timestamp, msg.asInstanceOf[ResponseStrategyServicesTopicMessage].sequenceNum, msg.asInstanceOf[ResponseStrategyServicesTopicMessage].signal, msg.asInstanceOf[ResponseStrategyServicesTopicMessage].serviceId)
	            case _ => msg
	          }
	        }
	        case x: ResponseEngineFacadeTopicMessage => x
	        case x: ResponseEngineServicesTopicMessage => x
	        case _ => msg
	    }
     
  }

}
