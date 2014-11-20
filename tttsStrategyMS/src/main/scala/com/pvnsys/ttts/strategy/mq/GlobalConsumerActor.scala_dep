package com.pvnsys.ttts.strategy.mq

import akka.actor.{ActorLogging, OneForOneStrategy, AllForOneStrategy, Props}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.TttsStrategyMessage
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.stream.actor.ActorProducer
import akka.stream.actor.ActorProducer._
import com.pvnsys.ttts.strategy.Configuration
import java.util.Properties
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import spray.json.DefaultJsonProtocol
import spray.json.pimpString
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.mutable
import scala.collection.mutable.Map
import akka.stream.actor.ActorConsumer
import akka.actor.ActorRef
import com.pvnsys.ttts.strategy.util.Utils


object GlobalConsumerActor {
  def props(publisher: ActorRef) = Props(new GlobalConsumerActor(publisher))

}


class GlobalConsumerActor(publisher: ActorRef) extends ActorConsumer with ActorLogging {
  import GlobalConsumerActor._
  import TttsStrategyMessages._
  import StrategyConsumerActorJsonProtocol._
  import ActorConsumer._

  
  private var inFlight = 0
  
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("GlobalConsumerActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
  
//  override protected def requestStrategy = new WatermarkRequestStrategy(1, 1) 
  
  override protected def requestStrategy = new MaxInFlightRequestStrategy(1) {
	  override def batchSize = 1
	  override def inFlightInternally = inFlight
  }
 
 
  
	override def receive = {

		case OnNext(mesg: TttsStrategyMessage) => {
		   publisher ! mesg
		   inFlight += 1
		   log.debug("$$$$$$$$$$ GlobalConsumerActor, inFlight = {}", inFlight)
		}	  

		case ProducerConfirmationMessage => {
		  inFlight -= 1	
		  log.debug("$$$$$$$$$$ GlobalConsumerActor, inFlight = {}", inFlight)
		}
		
		case _ => log.error("GlobalConsumerActor Received unknown message")
	}
  
}
