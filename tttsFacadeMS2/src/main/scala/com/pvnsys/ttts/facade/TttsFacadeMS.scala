package com.pvnsys.ttts.facade

import com.pvnsys.ttts.facade.api.{MainActors, TttsFacadeApi}
import com.pvnsys.ttts.facade.server.TttsFacadeMSServer
import akka.actor.ActorSystem
import akka.io.{IO, Tcp}
import java.net.InetSocketAddress
import spray.can.Http

object TttsFacadeMS extends App with MainActors with TttsFacadeApi {
  implicit lazy val system = ActorSystem("ttts-facade-service")
  private val rs = new TttsFacadeMSServer(Configuration.portWs)
  rs.forResource("/feed/ws", Some(feed))
  rs.start
  sys.addShutdownHook({system.shutdown;rs.stop})
  IO(Http) ! Http.Bind(rootService, Configuration.host, port = Configuration.portHttp)
}

object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("ttts-facade-ms.host")
  val portHttp = config.getInt("ttts-facade-ms.ports.http")
  val portWs   = config.getInt("ttts-facade-ms.ports.ws")
}
