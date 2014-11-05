package com.pvnsys.ttts.facade.api

import akka.actor.Props
import com.pvnsys.ttts.facade.strategy.StrategyActor
import com.pvnsys.ttts.facade.feed.FeedActor

trait MainActors {
  this: AbstractSystem =>

  lazy val feed = system.actorOf(Props[FeedActor], "feed")
  lazy val strategy = system.actorOf(Props[StrategyActor], "strategy")
}
