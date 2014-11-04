package com.pvnsys.ttts.strategy.flows

import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Duct
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.TttsStrategyMessage

trait StrategyServiceFlow {
  
  def startFlow()
  
}