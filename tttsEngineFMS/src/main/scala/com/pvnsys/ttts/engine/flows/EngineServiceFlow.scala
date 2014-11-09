package com.pvnsys.ttts.engine.flows

import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Duct
import com.pvnsys.ttts.engine.messages.TttsEngineMessages.TttsEngineMessage

trait EngineServiceFlow {
  
  def startFlow()
  
}