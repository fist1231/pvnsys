package com.pvnsys.ttts.strategy.generator

import com.pvnsys.ttts.strategy.Configuration
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.{TttsStrategyMessage, FacadeTopicMessage, RequestStrategyFacadeTopicMessage, ServicesTopicMessage, RequestStrategyServicesTopicMessage, ResponseFeedServicesTopicMessage}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages


object StrategyService {
  
  import TttsStrategyMessages._

  def convertFacadeMessage(msg: FacadeTopicMessage): RequestStrategyFacadeTopicMessage = {
     RequestStrategyFacadeTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum)
  }

  def convertServicesMessage(msg: ServicesTopicMessage): ServicesTopicMessage = {
//     RequestStrategyServicesTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum)
     
	        msg.msgType match {
	          case FEED_RESPONSE_MESSAGE_TYPE => {
	            ResponseFeedServicesTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum).asInstanceOf[ServicesTopicMessage]
	          }
	          case STRATEGY_REQUEST_MESSAGE_TYPE => {
	            RequestStrategyServicesTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum).asInstanceOf[ServicesTopicMessage]
	          }
	        }
     
  }

}
