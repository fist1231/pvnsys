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

object ReadKdbActor {
  
  import AbxStrategyActor._
  def props(serviceId: String) = Props(new ReadKdbActor(serviceId))
  sealed trait ReadKdbMessages
  case object ReadKdbMessage extends ReadKdbMessages
  case class ReadKdbResultMessage(result: StrategyKdbType) extends ReadKdbMessages
  case object StopReadKdbActor extends ReadKdbMessages
}

/**
 * This actor will do read-only operations on kdb database
 */
class ReadKdbActor(serviceId: String) extends Actor with ActorLogging {
  
	import ReadKdbActor._
	import TttsStrategyMessages._
	import AbxStrategyActor._
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("ReadKdbActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	override def receive = {
		case ReadKdbMessage => {
			log.debug("ReadKdbActor received ReadKdbMessage")
			val result = getStrategyData()
			val client = sender()
			log.debug("Sending result back: {}", result)
		    client ! ReadKdbResultMessage(result)
		    self ! StopReadKdbActor
		}
		case StopReadKdbActor => {
			log.debug("ReadKdbActor received StopMessage")
			//self ! PoisonPill
			context stop self
		}
		case _ => log.error("ReadKdbActor Received unknown message")
	}


	def getStrategyData(): StrategyKdbType = {
	      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
		  log.debug("Connected to KDB server. Retrieving data")
		  val res = conn.k("select from strategy")
		  val tabres: Flip = res.asInstanceOf[Flip]
		  val colNames = tabres.x
		  val colData = tabres.y
		  
		  val funds: Double = (c.at(colData(0), 0)).asInstanceOf[Double]
		  val balance: Double = (c.at(colData(1), 0)).asInstanceOf[Double]
		  val transnum: Long = (c.at(colData(2), 0)).asInstanceOf[Long]
		  val intrade: Boolean = (c.at(colData(3), 0)).asInstanceOf[Boolean]
		  val possize: Long = (c.at(colData(4), 0)).asInstanceOf[Long]
		//      val kdb: KdbType = (c.at(colData(0), 0).asInstanceOf[Double], c.at(colData(1), 0).asInstanceOf[Double], c.at(colData(2), 0).asInstanceOf[Int], c.at(colData(3), 0).asInstanceOf[Boolean], c.at(colData(4), 0).asInstanceOf[Int])
		  val kdb: StrategyKdbType = (funds, balance, transnum, intrade, possize)
		  log.debug("^^^^^^^^^^^^ data = {}", kdb)
	      conn.close
	      kdb
	}
	
	override def postStop() = {
	}
	
	
	
}
