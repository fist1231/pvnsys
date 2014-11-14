package com.pvnsys.ttts.engine.impl

import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import com.typesafe.scalalogging.slf4j.LazyLogging
import akka.actor.ActorContext
import scala.concurrent._
import ExecutionContext.Implicits.global

object TradeEngine {
}

/**
 * Engine X
 * 
 */
class TradeEngine extends Engine with LazyLogging {

  import TttsEngineMessages._
  import Engine._
  
  override def process(tableId: String, payload: StrategyPayload, strategySignal: String): Future[Option[EnginePayload]] = {
    
	  /*
	   * Insert code here
	   */
    Future { None }
  }

}
  
