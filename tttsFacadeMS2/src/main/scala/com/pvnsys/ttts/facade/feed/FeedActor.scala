package com.pvnsys.ttts.facade.feed

import com.pvnsys.ttts.facade.server.TttsFacadeMSServer
import akka.actor.{Actor, ActorLogging, ActorContext, Props}
import scala.collection._
import org.java_websocket.WebSocket
import scala.concurrent.duration._
import scala.concurrent.duration.TimeUnit
import scala.concurrent.ExecutionContext

case object FeedPushMessage
case class TickQuote(wSock: WebSocket)

object FeedActor {
  sealed trait FeedMessage
  case class Unregister(ws : WebSocket) extends FeedMessage
}

class FeedActor extends Actor with ActorLogging {
  import FeedActor._
  import TttsFacadeMSServer._

  val clients = mutable.ListBuffer[WebSocket]()
  override def receive = {
    case Open(ws, hs) => {
      clients += ws
      ws.send("Beginning ...")
      val feedPushActor = context.system.actorOf(Props[FeedPushActor])
	  // after zero seconds, send a Greet message every second to the greeter with a sender of the greetPrinter
	  context.system.scheduler.schedule(0.seconds, 1.second, feedPushActor, TickQuote(ws))(context.system.dispatcher, self)
      log.debug("registered monitor for url {}", ws.getResourceDescriptor)
    }
    case Unregister(ws) => {
      if (null != ws) {
        log.debug("unregister monitor")
        clients -= ws
      }
    }
    case Close(ws, code, reason, ext) => self ! Unregister(ws)
    case Error(ws, ex) => self ! Unregister(ws)
    case Message(ws, msg) =>
      log.debug("url {} received msg '{}'", ws.getResourceDescriptor, msg)
  }
}


class FeedPushActor extends Actor with ActorLogging {
  def receive = {
    case TickQuote(ws) => {
      val rand = Seq.fill(5)(scala.util.Random.nextInt(100))
      val msg = s"Dummy quote: $rand"
      log.debug(s"##### Sending message: $msg")
      ws.send(msg)
    }
  }
}
