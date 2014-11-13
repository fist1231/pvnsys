package com.pvnsys.ttts.feed.flows

import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Duct
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.TttsFeedMessage

trait FeedServiceFlow {
  def startFlow()
}