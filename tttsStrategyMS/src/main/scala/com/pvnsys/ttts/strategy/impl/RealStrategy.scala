package com.pvnsys.ttts.strategy.impl

import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.typesafe.scalalogging.slf4j.LazyLogging

object RealStrategy {
}

/**
 * Strategy X
 * 
 */
class RealStrategy extends Strategy with LazyLogging {

  import TttsStrategyMessages._

  override def process(msg: TttsStrategyMessage) = {
    
	  /*
	   * Insert code here
	   */
    msg
  }

}
  
