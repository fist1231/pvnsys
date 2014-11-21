package com.pvnsys.ttts.engine.flows.pub

import akka.actor.{ActorLogging, OneForOneStrategy, AllForOneStrategy}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages.TttsEngineMessage
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import com.pvnsys.ttts.engine.Configuration
import java.util.Properties
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import spray.json.DefaultJsonProtocol
import spray.json.pimpString
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.mutable
import scala.collection.mutable.Map
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}

object PublisherActor {
  sealed trait EngineMessage
  case object StopMessage extends EngineMessage

}

abstract class PublisherActor extends ActorPublisher[TttsEngineMessage] with ActorLogging {
  import PublisherActor._
  import TttsEngineMessages._

    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("PublisherActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
}
