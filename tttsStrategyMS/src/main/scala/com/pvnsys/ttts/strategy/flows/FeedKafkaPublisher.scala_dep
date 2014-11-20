package com.pvnsys.ttts.strategy.flows

import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Duct
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.RequestStrategyFacadeTopicMessage

/**
 * Wraps the action of publishing to a Kafka facade topic MQ and exposes it as a Flow processing.
 * 
 */
class StrategyKafkaPublisher {

  val flow: Duct[RequestStrategyFacadeTopicMessage, Unit] = 
    Duct[RequestStrategyFacadeTopicMessage] foreach { 
      msg => startStrategyStream(msg) 
    }
  
  def startStrategyStream(msg: RequestStrategyFacadeTopicMessage) = {
  }
  
}