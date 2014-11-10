package com.pvnsys.ttts.feed.impl

import com.pvnsys.ttts.feed.messages.TttsFeedMessages.TttsFeedMessage

trait Feed {
  
  def process(msg: TttsFeedMessage): TttsFeedMessage
  
}

