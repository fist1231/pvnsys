package com.pvnsys.ttts.facade.feed

import com.pvnsys.ttts.facade.server.TttsFacadeMSServer
import com.pvnsys.ttts.facade.TttsFacadeMS
import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy}
import akka.actor.SupervisorStrategy.{Restart, Stop}
import scala.collection._
import org.java_websocket.WebSocket
import scala.concurrent.duration._
import scala.concurrent.duration.TimeUnit
import scala.concurrent.ExecutionContext
import com.pvnsys.ttts.facade.mq.KafkaProducerActor
import com.pvnsys.ttts.facade.mq.KafkaConsumerActor
import java.net.InetSocketAddress

case object FeedPushMessage
case class TickQuote(wSock: WebSocket)
case class KafkaNewMessage(message: String)
case class KafkaProducerMessage(id: String)
case class KafkaConsumerMessage(ws: WebSocket)
case class KafkaReceivedMessage(ws: WebSocket, message: String)

object FeedActor {
  sealed trait FeedMessage
  case class Unregister(webSock : WebSocket) extends FeedMessage
  case object StopMessage extends FeedMessage

}

class FeedActor extends Actor with ActorLogging {
  import FeedActor._
  import TttsFacadeMSServer._
  import TttsFacadeMS._

//  override val supervisorStrategy = OneForOneStrategy(loggingEnabled = true) {
//    case e =>
//      log.error("0 @@@@@@@@@@@@@@@ And here we have an Unexpected failure: {}", e.getMessage)
//      //Restart
//      Stop
//  }
  
  
  val clients = mutable.ListBuffer[WebSocket]()
  override def receive = {
    case Open(ws, hs) => {
      val xsock = ws
      clients += xsock
      log.debug("@@@@@@@@@@@@@@@@@ registered monitor for url {}", ws.getResourceDescriptor)
//      kafkaConsumerActor ! KafkaConsumerMessage(xsock)
      
    }
    case Unregister(ws) => {
      if (null != ws) {
        log.debug("@@@@@@@@@@@@@@@@@ unregister monitor for url {}", ws.getResourceDescriptor)
        clients -= ws
      }
    }
    case Close(ws, code, reason, ext) => {
//      log.error("3 FeedActor closed for this reason @@@@@@@@@@@@@@@@@", reason)
      self ! Unregister(ws)
    }
    case Error(ws, ex) => {
      ex.printStackTrace()
//      log.error("4 FeedActor error @@@@@@@@@@@@@@@@@", ex.getMessage())
      self ! Unregister(ws)
    }
    case Message(ws, msg) => {
      
      val wsock = ws
      val webSocketId = wsock.getRemoteSocketAddress().toString()
      
      log.debug("@@@@@@@@@@@@@@@@@ url {} received msg '{}'", ws.getResourceDescriptor, msg.toString())
      val str = wsock.getRemoteSocketAddress().toString()
      wsock.send(s"Beginning ... - $str" )
//      val feedPushActor = context.actorOf(Props[FeedPushActor])
	  // after zero seconds, send a Greet message every second to the greeter with a sender of the greetPrinter
//	  context.system.scheduler.schedule(0.seconds, 1.second, feedPushActor, TickQuote(ws))(context.dispatcher, self)
//      log.debug("6 @@@@@@@@@@@@@@@@@ u")
	  sendMessages(webSocketId)
//      log.debug("7 @@@@@@@@@@@@@@@@@ u")
	  receiveMessages(wsock)
//        	val kafkaConsumerActor = context.actorOf(KafkaConsumerActor.props(new InetSocketAddress("127.0.0.1", 5672)))
//        	kafkaConsumerActor ! KafkaConsumerMessage(ws)
	  
//      log.debug("8 @@@@@@@@@@@@@@@@@ u")
	  
//	  val kafkaConsumerActor = context.actorOf(KafkaConsumerActor.props(new InetSocketAddress("127.0.0.1", 5672)))
	  //kafkaConsumerActor.tell(KafkaConsumerMessage(), self)

      
//      if(ws.isOpen()) {
//        ws.send(msg)
//      } else {
//        log.debug(s"**** FeedActor is Unregistering self")
//        context.stop(self)
//      }
	  
//	  self ! Unregister(ws)
    }
      
//    case KafkaConsumerMessage(str) => {
//      if(ws.isOpen()) {
//        ws.send(str)
//      } else {
//        log.debug(s"**** Unregistering self")
//        context.stop(self)
//      }
//    }
      
  }
  
  def sendMessages(wid: String) = {
    val kafkaProducerActor = context.actorOf(KafkaProducerActor.props(new InetSocketAddress("127.0.0.1", 5672)))
    kafkaProducerActor ! KafkaProducerMessage(wid)
	kafkaProducerActor ! StopMessage

  }

  
  def receiveMessages(ws: WebSocket) = {
//        for (client <- clients) {
	val kafkaConsumerActor = context.actorOf(KafkaConsumerActor.props(new InetSocketAddress("127.0.0.1", 5672)))
	kafkaConsumerActor ! KafkaConsumerMessage(ws)
	kafkaConsumerActor ! StopMessage
        	
//	kafkaConsumerActor ! KafkaNewMessage("%%%%%%%%%%% Lets roll %%%%%%%%%")

//          client.send(msg)
//        }

  }

  
}


//class FeedPushActor extends Actor with ActorLogging {
//  def receive = {
//    case TickQuote(ws) => {
//      val rand = Seq.fill(5)(scala.util.Random.nextInt(100))
//      val msg = s"Dummy quote: $rand"
//      //log.debug(s"##### Sending message: $msg")
//      if(ws.isOpen()) {
//        ws.send(msg)
//      } else {
//        log.debug(s"**** Unregistering self")
//        context.stop(self)
//      }
//    }
//  }
//}
