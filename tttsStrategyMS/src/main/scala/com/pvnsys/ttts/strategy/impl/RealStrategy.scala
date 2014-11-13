package com.pvnsys.ttts.strategy.impl

import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.typesafe.scalalogging.slf4j.LazyLogging
import scala.concurrent._
import ExecutionContext.Implicits.global

object RealStrategy {
}

/**
 * Strategy X
 * 
 */
class RealStrategy extends Strategy with LazyLogging {

  import TttsStrategyMessages._

  override def process(serviceId: String, payload: FeedPayload): Future[String] = {
    
	  /*
	   * Insert code here
	   */
    Future { "_RESULT_" }
  }

}
  
