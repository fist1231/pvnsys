package com.pvnsys.ttts.engine.db

import java.util.Properties
import scala.collection.JavaConversions.seqAsJavaList
import com.pvnsys.ttts.engine.Configuration
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.AllForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy.Restart
import akka.actor.actorRef2Scala
import com.pvnsys.ttts.engine.impl.Engine
import kx.c
import kx.c._
import kx.c.Flip
import com.typesafe.scalalogging.slf4j.LazyLogging

object ReadKdbActor extends LazyLogging {
  
  import Engine._
  def props(tableId: String) = Props(new ReadKdbActor(tableId))
  sealed trait ReadKdbMessages
  case object ReadKdbMessage extends ReadKdbMessages
  case class ReadKdbResultMessage(result: EngineKdbType) extends ReadKdbMessages
  case object StopReadKdbActor extends ReadKdbMessages
  
	def getEngineData(tableId: String): EngineKdbType = {
	      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
		  logger.debug("Connected to KDB server. Retrieving data")
		  val res = conn.k(s"select from engine$tableId")
		  val tabres: Flip = res.asInstanceOf[Flip]
		  val colNames = tabres.x
		  val colData = tabres.y
		  
		  val funds: Double = (c.at(colData(0), 0)).asInstanceOf[Double]
		  val balance: Double = (c.at(colData(1), 0)).asInstanceOf[Double]
		  val transnum: Long = (c.at(colData(2), 0)).asInstanceOf[Long]
		  val intrade: Boolean = (c.at(colData(3), 0)).asInstanceOf[Boolean]
		  val possize: Long = (c.at(colData(4), 0)).asInstanceOf[Long]
		  val price: Double = (c.at(colData(5), 0)).asInstanceOf[Double]
		//      val kdb: KdbType = (c.at(colData(0), 0).asInstanceOf[Double], c.at(colData(1), 0).asInstanceOf[Double], c.at(colData(2), 0).asInstanceOf[Int], c.at(colData(3), 0).asInstanceOf[Boolean], c.at(colData(4), 0).asInstanceOf[Int])
		  val kdb: EngineKdbType = (funds, balance, transnum, intrade, possize, price)
		  logger.debug("^^^^^^^^^^^^ data = {}", kdb)
	      conn.close
	      kdb
	}
  
  
}

/**
 * This actor will do read-only operations on kdb database
 */
class ReadKdbActor(tableId: String) extends Actor with ActorLogging {
  
	import ReadKdbActor._
	import TttsEngineMessages._
	import Engine._
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("ReadKdbActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	override def receive = {
		case ReadKdbMessage => {
			log.debug("ReadKdbActor received ReadKdbMessage")
			val result = getEngineData()
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


	def getEngineData(): EngineKdbType = {
	      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
		  log.debug("Connected to KDB server. Retrieving data")
		  val res = conn.k(s"select from engine$tableId")
		  val tabres: Flip = res.asInstanceOf[Flip]
		  val colNames = tabres.x
		  val colData = tabres.y
		  
		  val funds: Double = (c.at(colData(0), 0)).asInstanceOf[Double]
		  val balance: Double = (c.at(colData(1), 0)).asInstanceOf[Double]
		  val transnum: Long = (c.at(colData(2), 0)).asInstanceOf[Long]
		  val intrade: Boolean = (c.at(colData(3), 0)).asInstanceOf[Boolean]
		  val possize: Long = (c.at(colData(4), 0)).asInstanceOf[Long]
		  val price: Double = (c.at(colData(5), 0)).asInstanceOf[Double]
		//      val kdb: KdbType = (c.at(colData(0), 0).asInstanceOf[Double], c.at(colData(1), 0).asInstanceOf[Double], c.at(colData(2), 0).asInstanceOf[Int], c.at(colData(3), 0).asInstanceOf[Boolean], c.at(colData(4), 0).asInstanceOf[Int])
		  val kdb: EngineKdbType = (funds, balance, transnum, intrade, possize, price)
		  log.debug("^^^^^^^^^^^^ data = {}", kdb)
	      conn.close
	      kdb
	}
	
	override def postStop() = {
	}
	
	
	
}
