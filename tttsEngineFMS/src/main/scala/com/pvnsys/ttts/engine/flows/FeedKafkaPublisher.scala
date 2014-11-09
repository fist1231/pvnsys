package com.pvnsys.ttts.engine.flows

import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Duct
import com.pvnsys.ttts.engine.messages.TttsEngineMessages.RequestEngineFacadeTopicMessage

/**
 * Wraps the action of publishing to a Kafka facade topic MQ and exposes it as a Flow processing.
 * 
 */
class EngineKafkaPublisher {

  val flow: Duct[RequestEngineFacadeTopicMessage, Unit] = 
    Duct[RequestEngineFacadeTopicMessage] foreach { 
      msg => startEngineStream(msg) 
    }
  
  def startEngineStream(msg: RequestEngineFacadeTopicMessage) = {
  }
  
}