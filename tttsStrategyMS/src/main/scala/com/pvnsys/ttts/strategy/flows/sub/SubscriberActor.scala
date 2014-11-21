package com.pvnsys.ttts.strategy.flow.sub

import akka.actor.{ActorLogging, OneForOneStrategy, AllForOneStrategy, Props}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.TttsStrategyMessage
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import com.pvnsys.ttts.strategy.Configuration
import java.util.Properties
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import spray.json.DefaultJsonProtocol
import spray.json.pimpString
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.mutable
import scala.collection.mutable.Map
import akka.actor.ActorRef
import com.pvnsys.ttts.strategy.util.Utils
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy}
import akka.stream.actor.ActorSubscriberMessage.OnNext


object SubscriberActor {
  sealed trait StrategyMessage
  case object StopMessage extends StrategyMessage
}

abstract class SubscriberActor extends ActorSubscriber with ActorLogging {
  import SubscriberActor._
  import TttsStrategyMessages._
  
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("SubscriberActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
}
