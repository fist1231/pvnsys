package com.pvnsys.ttts.strategy.generator

import com.pvnsys.ttts.strategy.Configuration
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.{TttsStrategyMessage, FacadeTopicMessage, RequestStrategyFacadeTopicMessage, ServicesTopicMessage, RequestStrategyServicesTopicMessage}
import com.typesafe.scalalogging.slf4j.LazyLogging


object StrategyService {
  
  def convertFacadeMessage(msg: FacadeTopicMessage): RequestStrategyFacadeTopicMessage = {
     RequestStrategyFacadeTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum)
  }

  def convertServicesMessage(msg: ServicesTopicMessage): RequestStrategyServicesTopicMessage = {
     RequestStrategyServicesTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum)
  }
  
}
