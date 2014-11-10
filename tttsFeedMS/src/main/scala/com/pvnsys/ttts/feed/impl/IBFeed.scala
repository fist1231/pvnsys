package com.pvnsys.ttts.feed.impl

import com.pvnsys.ttts.feed.messages.TttsFeedMessages
import com.typesafe.scalalogging.slf4j.LazyLogging

object IBFeed {
}

/**
 * IB Feed
 * 
 */
class IBFeed extends Feed with LazyLogging {

  import TttsFeedMessages._

  override def process(msg: TttsFeedMessage) = {
    
	  /*
	   * Insert code here
	   */
    msg
  }

}
  
