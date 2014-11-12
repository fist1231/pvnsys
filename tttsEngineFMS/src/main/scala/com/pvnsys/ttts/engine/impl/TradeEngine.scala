package com.pvnsys.ttts.engine.impl

import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import com.typesafe.scalalogging.slf4j.LazyLogging
import akka.actor.ActorContext

object TradeEngine {
}

/**
 * Engine X
 * 
 */
class TradeEngine extends Engine with LazyLogging {

  import TttsEngineMessages._
  import Engine._
  
  override def process(msg: TttsEngineMessage, serviceId: String): TttsEngineMessage = {
    
	  /*
	   * Insert code here
	   */
    msg
  }

}
  
