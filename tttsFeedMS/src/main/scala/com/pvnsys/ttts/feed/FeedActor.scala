package com.pvnsys.ttts.feed

import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy}
import akka.actor.SupervisorStrategy.{Restart, Stop}
import scala.collection._
import org.java_websocket.WebSocket
import scala.concurrent.duration._
import scala.concurrent.duration.TimeUnit
import scala.concurrent.ExecutionContext
import com.pvnsys.ttts.feed.mq.KafkaProducerActor
import com.pvnsys.ttts.feed.mq.KafkaConsumerActor
import java.net.InetSocketAddress
import akka.dispatch.Foreach

case object FeedPushMessage
case class TickQuote(wSock: WebSocket)
case class KafkaNewMessage(message: String)
case class KafkaProducerMessage(id: String)
case class KafkaConsumerMessage()
case class KafkaReceivedMessage(key: String, message: String)

object FeedActor {
  sealed trait FeedMessage
  case class Unregister(webSock : WebSocket) extends FeedMessage
  case object StopMessage extends FeedMessage

}

class FeedActor extends Actor with ActorLogging {
  import FeedActor._
  import TttsFeedMS._

//  override val supervisorStrategy = OneForOneStrategy(loggingEnabled = true) {
//    case e =>
//      log.error("0 @@@@@@@@@@@@@@@ And here we have an Unexpected failure: {}", e.getMessage)
//      //Restart
//      Stop
//  }
  
  
  val clients = mutable.ListBuffer[WebSocket]()
  val sockets = mutable.Map[String, WebSocket]()
  
  override def receive = {
    case Unregister(ws) => {
      if (null != ws) {
        log.debug("@@@@@@@@@@@@@@@@@ unregister monitor for url {}; key {}", ws.getResourceDescriptor, ws.getRemoteSocketAddress())
        clients -= ws
        sockets.foreach { case (key, value) => log.debug("zzz key: {} ==> value: {}", key, value) }
        if(null != ws.getRemoteSocketAddress()) {
        	sockets -= ws.getRemoteSocketAddress().toString()
        }
      }
    }
    case KafkaReceivedMessage(key, msg) => {
      
//      log.debug(s"TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT FeedActor received KafkaReceivedMessage: $msg")
        sockets.get(key) match {
		  case Some(wsk) => {
		    if(null != wsk && wsk.isOpen()) {
		    	wsk.send(msg)
		    }
		  }
		  case None => //log.debug("@@@@@@@@@@@@@@@@@ no such key {}", key)
		}
    }
      
  }
  
  def sendMessages(wid: String) = {
    val kafkaProducerActor = context.actorOf(KafkaProducerActor.props(new InetSocketAddress("127.0.0.1", 5672)))
    kafkaProducerActor ! KafkaProducerMessage(wid)
	kafkaProducerActor ! StopMessage

  }
  
}
