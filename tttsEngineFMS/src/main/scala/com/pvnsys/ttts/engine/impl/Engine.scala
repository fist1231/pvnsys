package com.pvnsys.ttts.engine.impl

import com.pvnsys.ttts.engine.messages.TttsEngineMessages.TttsEngineMessage
import akka.actor.ActorContext

object Engine {
}

trait Engine {
  import Engine._

  def process(msg: TttsEngineMessage, serviceId: String): TttsEngineMessage
  
}

