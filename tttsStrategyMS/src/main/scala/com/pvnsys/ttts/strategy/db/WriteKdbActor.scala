package com.pvnsys.ttts.strategy.db

import java.util.Properties

import scala.collection.JavaConversions.seqAsJavaList
import com.pvnsys.ttts.strategy.Configuration
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.AllForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy.Restart
import akka.actor.actorRef2Scala
import com.pvnsys.ttts.strategy.impl.AbxStrategyActor
import kx.c
import kx.c._
import kx.c.Flip

object WriteKdbActor {
  
  import AbxStrategyActor._
  def props(serviceId: String) = Props(new WriteKdbActor(serviceId))
  sealed trait WriteKdbMessages
  case class WriteStrategyKdbMessage(data: StrategyKdbType) extends WriteKdbMessages
  case class WriteTransactionKdbMessage(data: TransactionKdbType) extends WriteKdbMessages
  case object StopWriteKdbActor extends WriteKdbMessages
}

/**
 * This actor will do create/insert/update/delete operations on kdb database
 */
class WriteKdbActor(serviceId: String) extends Actor with ActorLogging {
  
	import WriteKdbActor._
	import TttsStrategyMessages._
    import AbxStrategyActor._
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("WriteKdbActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	override def receive = {
		case msg: WriteStrategyKdbMessage => {
			log.debug("WriteKdbActor received WriteStrategyKdbMessage")
			setStrategyData(msg.data)
		}
		case msg: WriteTransactionKdbMessage => {
			log.debug("WriteKdbActor received WriteTransactionKdbMessage")
			setTransactionData(msg.data)
		}
		case StopWriteKdbActor => {
			log.debug("WriteKdbActor received StopMessage")
			//self ! PoisonPill
			context stop self
		}
		case _ => log.error("WriteKdbActor Received unknown message")
	}

  def setStrategyData(data: StrategyKdbType) = {
      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
//      val res = conn.k(s"update strategy set funds=${data._1}, balance=${data._2}, transnum=${data._3}, intrade=${data._4}, possize=${data._5}")
      
      var intradeStr = "0b"
      if(data._4) {
        intradeStr = "1b"
      }
      
      val updateStr = s"strategy:update funds:${data._1},balance:${data._2},transnum:${data._3},intrade:${intradeStr},possize:${data._5} from strategy" 
      conn.k(updateStr)
 	  log.debug("WriteKdbActor updated strategy data with: {}", updateStr)

      conn close
      
  }

  
  /*
   * 
   * (String, String, Double, Double, Double, Double, Long, Double, Long)
   * 
   * select sum size by sym from quotes
   * 
   */
  
  def setTransactionData(data: TransactionKdbType) = {
      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
// 	  quotes:([]datetime:`timestamp$();sym:`symbol$();open:`float$();high:`float$();low:`float$();close:`float$();volume:`long$();wap:`float$();size:`long$()) 
      val updateStr = s"`quotes insert(`${data._1};`${data._2};${data._3};${data._4};${data._5};${data._6};${data._7};${data._8};${data._9})" 
 	  log.debug("WriteKdbActor updating TRADE table data with: {}", updateStr)
      conn.k(updateStr)
      conn close
  }
  
	override def postStop() = {
	   
	}
}
