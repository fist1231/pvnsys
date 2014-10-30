package com.pvnsys.ttts.facade.feed

import com.pvnsys.ttts.facade.server.TttsFacadeMSServer
import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy}
import akka.actor.SupervisorStrategy.{Restart, Stop}
import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, Map}
import org.java_websocket.WebSocket
import com.pvnsys.ttts.facade.mq.KafkaProducerActor
import java.net.InetSocketAddress
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.FacadeIncomingMessage
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.FacadeClientFeedRequestMessage
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.FacadeOutgoingFeedRequestMessage
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.FacadeIncomingFeedResponseMessage
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.TttsFacadeMessage
import spray.json._


object FeedActor {
  sealed trait FeedActorMessage
  case object FeedActorStopMessage extends FeedActorMessage
  case class FeedActorUnregisterWebSocketMessage(webSocket : WebSocket) extends FeedActorMessage
}

object FeedActorJsonProtocol extends DefaultJsonProtocol {
  implicit val facadeOutgoingFeedRequestMessageFormat = jsonFormat2(FacadeOutgoingFeedRequestMessage)
  implicit val facadeClientFeedRequestMessageFormat = jsonFormat3(FacadeClientFeedRequestMessage)
  implicit val facadeIncomingFeedResponseMessageFormat = jsonFormat3(FacadeIncomingFeedResponseMessage)
}


class FeedActor extends Actor with ActorLogging {
  import FeedActor._
  import TttsFacadeMSServer._
  import FeedActorJsonProtocol._

//  override val supervisorStrategy = OneForOneStrategy(loggingEnabled = false) {
//    case e =>
//      log.error("Unexpected failure in FeedActor: {}", e.getMessage)
//      Restart
////      Stop
//  }
  
  
  val clients = mutable.ListBuffer[WebSocket]()
  val sockets = mutable.Map[String, WebSocket]()
  
  override def receive = {
    case Open(ws, hs) => {
      clients += ws
      sockets += (ws.getRemoteSocketAddress().toString() -> ws)
      log.debug("FeedActor Registered monitor for url {}; key {}", ws.getResourceDescriptor, ws.getRemoteSocketAddress())
//      kafkaConsumerActor ! KafkaConsumerMessage(xsock)
      
    }
    case FeedActorUnregisterWebSocketMessage(ws) => {
      if (null != ws) {
        log.debug("FeedActor Unregister monitor for url {}; key {}", ws.getResourceDescriptor, ws.getRemoteSocketAddress())
        clients -= ws
        sockets.foreach { case (key, value) => log.debug("zzz key: {} ==> value: {}", key, value) }
        if(null != ws.getRemoteSocketAddress()) {
        	sockets -= ws.getRemoteSocketAddress().toString()
        }
      }
    }
    case Close(ws, code, reason, ext) => {
//      log.error("3 FeedActor closed for this reason @@@@@@@@@@@@@@@@@", reason)
      self ! FeedActorUnregisterWebSocketMessage(ws)
    }
    case Error(ws, ex) => {
      ex.printStackTrace()
//      log.error("4 FeedActor error @@@@@@@@@@@@@@@@@", ex.getMessage())
      self ! FeedActorUnregisterWebSocketMessage(ws)
    }
    case msg: FacadeIncomingFeedResponseMessage => {
      
//      log.debug(s"TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT FeedActor received KafkaReceivedMessage: $msg")
        sockets.get(msg.client) match {
		  case Some(wsk) => {
		    if(null != wsk && wsk.isOpen()) {
		    	wsk.send(msg.toJson.compactPrint)
		    }
		  }
		  case None => //log.debug("@@@@@@@@@@@@@@@@@ no such key {}", key)
		}
    }
    case Message(ws, msg) => {
      
      val wsock = ws
      val webSocketId = wsock.getRemoteSocketAddress().toString()
      
      val msgStr = msg.parseJson.prettyPrint
      log.debug("~~~~ FeedActor url {} received msg '{}'", ws.getResourceDescriptor, msgStr)
      
      // Do parse messages to determine request type. Uses custom FeedActorJsonProtocol Format converter for spray json objects
//      val jsonMsg = msg.toJson
//      log.debug("~~~~ FeedActor JSON version of the message: {}", jsonMsg)
      
//      val facadeClientFeedRequestMessage = msg.toJson.convertTo[FacadeClientFeedRequestMessage]
      val facadeClientFeedRequestMessage = msg.parseJson.convertTo[FacadeClientFeedRequestMessage]
      log.debug("~~~~ FeedActor FacadeOutgoingFeedRequestMessage version of the message: {}", facadeClientFeedRequestMessage)
      
      
      val client = wsock.getRemoteSocketAddress().toString()
      val facadeOutgoingFeedRequestMessage = FacadeOutgoingFeedRequestMessage(facadeClientFeedRequestMessage.id, client)
      log.debug("~~~~ FeedActor FacadeOutgoingFeedRequestMessage version of the message: {}", facadeOutgoingFeedRequestMessage)

      wsock.send(s"Beginning ... - $client" )
	  sendMessages(webSocketId, facadeOutgoingFeedRequestMessage)
    }
      
  }
  
  def sendMessages(wid: String, msg: TttsFacadeMessage) = {
    
    // Creating Producer Actor to post outgoing message to facade topic MQ. Creating new Actor for every message
    val kafkaProducerActor = context.actorOf(KafkaProducerActor.props(new InetSocketAddress("127.0.0.1", 5672)))
    kafkaProducerActor ! msg
//    kafkaProducerActor ! FacadeOutgoingMessage(wid)
//	kafkaProducerActor ! FeedActorStopMessage

  }
  
}
