package com.pvnsys.ttts.facade.api

import com.pvnsys.ttts.facade.feed.FeedActor

import akka.actor.Props

trait MainActors {
  this: AbstractSystem =>

  lazy val feed = system.actorOf(Props[FeedActor], "feed")
}
