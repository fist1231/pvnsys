package com.pvnsys.ttts.engine.impl

import com.pvnsys.ttts.engine.messages.TttsEngineMessages.TttsEngineMessage

object Engine {
	type EngineType = (TttsEngineMessage, Boolean)
}

trait Engine {
  import Engine._

  def process(msg: TttsEngineMessage, status: Boolean): EngineType
  
}

