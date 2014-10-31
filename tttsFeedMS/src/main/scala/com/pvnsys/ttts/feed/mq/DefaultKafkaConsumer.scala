package com.pvnsys.ttts.feed.mq

import com.pvnsys.ttts.feed.messages.TttsFeedMessages.FacadeTopicMessage

class DefaultKafkaConsumer {
  def handleDelivery(message: FacadeTopicMessage) = {}
}