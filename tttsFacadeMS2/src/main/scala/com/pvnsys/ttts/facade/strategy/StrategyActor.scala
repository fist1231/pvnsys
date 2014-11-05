package com.pvnsys.ttts.facade.strategy

import com.pvnsys.ttts.facade.server.TttsFacadeMSServer
import akka.actor.{Actor, ActorLogging, ActorContext, Props, OneForOneStrategy, AllForOneStrategy}
import akka.actor.SupervisorStrategy.{Restart, Stop}
import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, Map}
import org.java_websocket.WebSocket
import com.pvnsys.ttts.facade.mq.KafkaProducerActor
import java.net.InetSocketAddress
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.{FacadeClientMessage, RequestStrategyFacadeMessage, ResponseStrategyFacadeMessage}
import com.pvnsys.ttts.facade.messages.TttsFacadeMessages.TttsFacadeMessage
import spray.json._
import com.pvnsys.ttts.facade.util.Utils


object StrategyActor {
  sealed trait StrategyActorMessage
  case object StrategyActorStopMessage extends StrategyActorMessage
  case class StrategyActorUnregisterWebSocketMessage(webSocket : WebSocket) extends StrategyActorMessage
  
}

object StrategyActorJsonProtocol extends DefaultJsonProtocol {
  implicit val facadeClientMessageFormat = jsonFormat2(FacadeClientMessage)
  implicit val requestFacadeMessageFormat = jsonFormat6(RequestStrategyFacadeMessage)
  implicit val responseFacadeMessageFormat = jsonFormat7(ResponseStrategyFacadeMessage)
}


class StrategyActor extends Actor with ActorLogging {
  import StrategyActor._
  import TttsFacadeMSServer._
  import StrategyActorJsonProtocol._
  import Utils._
  import TttsFacadeMessages._

  override val supervisorStrategy = OneForOneStrategy(loggingEnabled = false) {
    case e =>
      log.error("Unexpected failure in FeedActor: {}", e.getMessage)
      Restart
//      Stop
  }
  
  
  val clients = mutable.ListBuffer[WebSocket]()
  val sockets = mutable.Map[String, WebSocket]()
  
  override def receive = {
    case Open(ws, hs) => {
      clients += ws
      sockets += (ws.getRemoteSocketAddress().toString() -> ws)
      log.debug("TttsFacadeMS Registered WebSocket for url {}; key {}", ws.getResourceDescriptor, ws.getRemoteSocketAddress())
//      kafkaConsumerActor ! KafkaConsumerMessage(xsock)
      
    }
    case StrategyActorUnregisterWebSocketMessage(ws) => {
      if (null != ws) {
        log.debug("TttsFacadeMS Unregister WebSocket for url {}; key {}", ws.getResourceDescriptor, ws.getRemoteSocketAddress())
//        sockets.foreach { case (key, value) => log.debug("wwwww key: {} ==> value: {}", key, value) }
        clients -= ws
        val keyToKill = sockets.find(_._2 == ws).get._1 
//        log.debug("xxxxx keyToKill: {}", keyToKill)
            
        if(null != keyToKill) {
//        	sockets -= ws.getRemoteSocketAddress().toString()
        	sockets -= keyToKill
        	val messageTraits = Utils.generateMessageTraits
	        val feedStopRequestMessage = RequestStrategyFacadeMessage(messageTraits._1, "STRATEGY_STOP_REQ", keyToKill, "", messageTraits._2, messageTraits._3)
		    sendMessages(feedStopRequestMessage)
        }
//        sockets.foreach { case (key, value) => log.debug("zzzzz key: {} ==> value: {}", key, value) }
      }
    }
    case Close(ws, code, reason, ext) => {
//      log.error("3 FeedActor closed for this reason @@@@@@@@@@@@@@@@@", reason)
      self ! StrategyActorUnregisterWebSocketMessage(ws)
    }
    case Error(ws, ex) => {
//      ex.printStackTrace()
//      log.error("4 FeedActor error @@@@@@@@@@@@@@@@@", ex.getMessage())
      self ! StrategyActorUnregisterWebSocketMessage(ws)
    }
    case msg: ResponseStrategyFacadeMessage => {
      
//      log.debug(s"TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT FeedActor received KafkaReceivedMessage: $msg")
        log.debug("TttsFacadeMS StrategyActor received Services Response message: {}", msg)
//        sockets.get(msg.client) match {
//        sockets.foreach { case (key, value) => log.debug("zzzzz key: {} ==> value: {}", key, value) }
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
      
      /*
       * This is a message received from UI clients.
       * Message is in JSON format: { msgType:STRATEGY_REQ , payload:MESSAGE }
       * 
       */ 
      
      // TODO: Put all this processing into the Flow
      
      val wsock = ws
      val webSocketId = wsock.getRemoteSocketAddress().toString()
      
      val msgStr = msg.parseJson.compactPrint
//      log.debug("FeedActor url {} received msg '{}'", ws.getResourceDescriptor, msgStr)
      
      // Convert incoming WebSocket message string to JSON object and to FacadeClientFeedRequestMessage
      val facadeClientMessage = msg.parseJson.convertTo[FacadeClientMessage]
//      log.debug("FeedActor FacadeOutgoingFeedRequestMessage version of the message: {}", facadeClientMessage)
      
      // Convert FacadeClientFeedRequestMessage into specific TttsFacadeMessage based on the Request Type field of JSON: msgType
//      val facadeOutMessage = matchRequest(facadeClientMessage, webSocketId)

        // Generate unique message ID, timestamp and sequence number to be assigned to every incoming message.
        val messageTraits = Utils.generateMessageTraits
      
		matchRequest(facadeClientMessage, webSocketId, messageTraits) match {
		  case Some(facadeClientMessage) => {
		      wsock.send(s"Beginning ... - $webSocketId" )
		      log.debug("TttsFacadeMS StrategyActor received client request message: {}", facadeClientMessage)
			  sendMessages(facadeClientMessage)
//		      consumer.handleDelivery(facadeTopicMessage)
		  }
		  case None => //"Do nothing"
		}
      
//      log.debug("~~~~ FeedActor FacadeOutgoingFeedRequestMessage version of the message: {}", facadeClientMessage)

    }
      
  }
  
  def matchRequest(clientReq: FacadeClientMessage, webSocketId: String, messageTraits: MessageTraits): Option[TttsFacadeMessage] = clientReq.msgType match {
  	  case STRATEGY_REQUEST_MESSAGE_TYPE => Some(RequestStrategyFacadeMessage(messageTraits._1 , clientReq.msgType, webSocketId, clientReq.payload, messageTraits._2, messageTraits._3))
  	  case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => Some(RequestStrategyFacadeMessage(messageTraits._1, clientReq.msgType, webSocketId, clientReq.payload, messageTraits._2, messageTraits._3))
  	  case _ => {
  	    log.debug("TttsFacadeMS StrategyActor received unknown message type from the client: [{}]", clientReq.msgType) 
  	    None
  	  }
  }
  
  def sendMessages(msg: TttsFacadeMessage) = {
    
    // Creating Producer Actor to post outgoing message to facade topic MQ. Creating new Actor for every message
    val kafkaProducerActor = context.actorOf(KafkaProducerActor.props(new InetSocketAddress("127.0.0.1", 5672)))
    kafkaProducerActor ! msg
//    kafkaProducerActor ! FacadeOutgoingMessage(wid)
//	kafkaProducerActor ! FeedActorStopMessage

  }
  
}
