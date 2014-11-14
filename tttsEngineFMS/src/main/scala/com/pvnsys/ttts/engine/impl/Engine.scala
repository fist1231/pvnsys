package com.pvnsys.ttts.engine.impl

import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import akka.actor.ActorContext
import scala.concurrent.Future

object Engine {
}

trait Engine {
  import Engine._
  import TttsEngineMessages._

  def process(tableId: String, payload: StrategyPayload, strategySignal: String): Future[Option[EnginePayload]]
  
}

