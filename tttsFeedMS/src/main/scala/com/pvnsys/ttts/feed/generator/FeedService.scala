package com.pvnsys.ttts.feed.generator

import com.pvnsys.ttts.feed.Configuration
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.{TttsFeedMessage, FacadeTopicMessage, RequestFeedFacadeTopicMessage}
import com.typesafe.scalalogging.slf4j.LazyLogging


object FeedService {
  
  def startFeed(msg: FacadeTopicMessage): RequestFeedFacadeTopicMessage = {
     RequestFeedFacadeTopicMessage(msg.id, msg.msgType, msg.client, msg.payload, msg.timestamp, msg.sequenceNum)
  }
  
}
