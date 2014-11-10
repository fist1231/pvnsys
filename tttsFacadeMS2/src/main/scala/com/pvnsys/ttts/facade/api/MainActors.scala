package com.pvnsys.ttts.facade.api

import akka.actor.Props
import com.pvnsys.ttts.facade.strategy.StrategyActor
import com.pvnsys.ttts.facade.feed.FeedActor
import com.pvnsys.ttts.facade.engine.EngineActor

trait MainActors {
  this: AbstractSystem =>

  lazy val feed = system.actorOf(Props[FeedActor], "feed")
  lazy val strategy = system.actorOf(Props[StrategyActor], "strategy")
  lazy val engine = system.actorOf(Props[EngineActor], "engine")
}
