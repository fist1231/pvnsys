package com.pvnsys.ttts.feed.mq

import com.pvnsys.ttts.feed.messages.TttsFeedMessages.TttsFeedMessage

class DefaultKafkaConsumer {
  def handleDelivery(message: TttsFeedMessage) = {}
}