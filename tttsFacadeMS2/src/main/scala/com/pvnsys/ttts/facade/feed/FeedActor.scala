package com.pvnsys.ttts.facade.feed

import com.pvnsys.ttts.facade.server.TttsFacadeMSServer
import akka.actor.{Actor, ActorLogging, ActorContext, Props}
import scala.collection._
import org.java_websocket.WebSocket
import scala.concurrent.duration._
import scala.concurrent.duration.TimeUnit
import scala.concurrent.ExecutionContext

case object FeedMessage
case class TickQuote(wSock: WebSocket)


object FeedActor {
  sealed trait FindMessage
  case object Clear extends FindMessage
  case class Unregister(ws : WebSocket) extends FindMessage
  case class Marker(id : String, idx : String) extends FindMessage
  case class Clear(marker : Marker) extends FindMessage
  case class Move(marker : Marker, longitude : String, latitude : String) extends FindMessage
  case class Infa(infa : String) extends FindMessage
}
class FeedActor extends Actor with ActorLogging {
  import FeedActor._
  import TttsFacadeMSServer._

  val clients = mutable.ListBuffer[WebSocket]()
  val markers = mutable.Map[Marker,Option[Move]]()
  override def receive = {
    case Open(ws, hs) => {
      clients += ws

  val greetPrinter = context.system.actorOf(Props[GreetPrinter])
  // after zero seconds, send a Greet message every second to the greeter with a sender of the greetPrinter
//        var i = 0
  context.system.scheduler.schedule(0.seconds, 1.second, greetPrinter, TickQuote(ws))(context.system.dispatcher, self)
      
//      context.system.scheduler.schedule(() => println("Do something"), 0L, 5L, MINUTES)
//      context.system.scheduler.schedule(0 seconds, 1 seconds){ws.send(message(s"Vot eto da tak da: $i"))}
//        while(true) {
//          i = i + 1
//          if((i%11)==0) {
//        	  ws.send(message(s"Vot eto da tak da: $i"))
//          }
//        }
//      ws.send(message(marker._2.get))
//      for (marker <- markers if None != marker._2) {
//        ws.send(message(marker._2.get))
//      }
      log.debug("registered monitor for url {}", ws.getResourceDescriptor)
    }
    case Close(ws, code, reason, ext) => self ! Unregister(ws)
    case Error(ws, ex) => self ! Unregister(ws)
    case Message(ws, msg) =>
      log.debug("url {} received msg '{}'", ws.getResourceDescriptor, msg)
    case Clear => {
      for (marker <- markers if None != marker._2) {
        val msg = message(marker._1)
        for (client <- clients) {
          client.send(msg)
        }
      }
      markers.clear
    }
    case Unregister(ws) => {
      if (null != ws) {
        log.debug("unregister monitor")
        clients -= ws
      }
    }
    case Clear(marker) => {
      log.debug("clear marker {} '{}'", marker.idx, marker.id)
      val msg = message(marker)
      markers -= marker
      for (client <- clients) {
        client.send(msg)
      }
      log.debug("sent to {} clients to clear marker '{}'", clients.size, msg)
    }
    case marker @ Marker(id, idx) => {
      markers += ((marker, None))
      log.debug("create new marker {} '{}'", idx, id)
    }
    case move @ Move(marker, lng, lat) => {
      markers += ((marker, Some(move)))
      val msg = message(move)
      for (client <- clients) {
        client.send(msg)
      }
      log.debug("sent to {} clients the new move '{}'", clients.size, msg)
    }
  }
  private def message(move :Move) = s"""{"move":{"id":"${move.marker.id}","idx":"${move.marker.idx}","longitude":${move.longitude},"latitude":${move.latitude}}}"""
  private def message(marker :Marker) = s"""{"clear":{"id":"${marker.id}","idx":"${marker.idx}"}}"""
  private def message(inf :String) = s"""{"infa":{"Zakolupali vurdalaki ":"${inf}"}}"""
}

class GreetPrinter extends Actor {
  def receive = {
    case TickQuote(ws) => {
      val rand = Seq.fill(6)(scala.util.Random.nextInt(100))
      ws.send(s"Vot eto da tak da: $rand")
    }
  }
}
