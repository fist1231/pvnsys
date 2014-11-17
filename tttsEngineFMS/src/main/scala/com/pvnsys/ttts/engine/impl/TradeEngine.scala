package com.pvnsys.ttts.engine.impl

import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import com.typesafe.scalalogging.slf4j.LazyLogging

object TradeEngine {
}

/**
 * Engine X
 * 
 */
class TradeEngine extends Engine with LazyLogging {

  import TttsEngineMessages._
  import Engine._
  
  override def applyEngine(serviceId: String, message: TttsEngineMessage): TttsEngineMessage = {
    
	  /*
	   * Insert code here
	   */
    message
  }

}
  
