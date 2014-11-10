package com.pvnsys.ttts.facade.engine

import com.pvnsys.ttts.facade.Configuration
import akka.actor.{ ActorRef, ActorSystem }
import spray.http.StatusCodes
import spray.routing.Directives

class EngineService(find : ActorRef)(implicit system : ActorSystem) extends Directives {
  lazy val route =
    pathPrefix("engine") {
      val dir = "engine/"
      pathEndOrSingleSlash {
        getFromResource(dir + "index.html")
      } ~
      path("ws") {
        requestUri { uri =>
          val wsUri = uri.withPort(Configuration.portWs)
          redirect(wsUri, StatusCodes.PermanentRedirect)
        }
      } ~
      getFromResourceDirectory(dir)
    }
}
