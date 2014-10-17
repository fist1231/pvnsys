package com.pvnsys.ttts.facade.api

import com.pvnsys.ttts.facade.feed.FeedService
import akka.actor.{ ActorSystem, Props }
import akka.event.Logging.InfoLevel
import spray.http.HttpRequest
import spray.http.StatusCodes.{ MovedPermanently, NotFound }
import spray.routing.{Directives, RouteConcatenation}
import spray.routing.directives.LogEntry

trait AbstractSystem {
  implicit def system: ActorSystem
}

trait TttsFacadeApi extends RouteConcatenation with StaticRoute with AbstractSystem {
  this: MainActors =>

  val rootService = system.actorOf(Props(classOf[RootService], routes))
//  val socketService = system.actorOf(Props[SocketService])

  lazy val routes = logRequest(showReq _) {
    new FeedService(feed).route ~
    staticRoute
  }
  private def showReq(req : HttpRequest) = LogEntry(req.uri, InfoLevel)
}

trait StaticRoute extends Directives {
  this: AbstractSystem =>

  lazy val staticRoute =
    path("favicon.ico") {
      getFromResource("favicon.ico")
    } ~
    pathPrefix("markers") {
      getFromResourceDirectory("markers/")
    } ~
    pathPrefix("css") {
      getFromResourceDirectory("css/")
    } ~
    pathEndOrSingleSlash {
      getFromResource("index.html")
    } ~ complete(NotFound)
}
