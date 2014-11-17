package com.pvnsys.ttts.engine.impl

import com.pvnsys.ttts.engine.messages.TttsEngineMessages

object Engine {
}

trait Engine {
  import Engine._
  import TttsEngineMessages._

  def applyEngine(serviceId: String, message: TttsEngineMessage): TttsEngineMessage
  
}

