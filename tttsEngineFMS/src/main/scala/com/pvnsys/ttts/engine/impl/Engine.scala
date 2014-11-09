package com.pvnsys.ttts.engine.impl

import com.pvnsys.ttts.engine.messages.TttsEngineMessages.TttsEngineMessage

trait Engine {
  
  def process(msg: TttsEngineMessage): TttsEngineMessage
  
}

