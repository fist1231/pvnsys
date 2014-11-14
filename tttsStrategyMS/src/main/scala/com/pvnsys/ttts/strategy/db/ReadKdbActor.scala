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
import java.lang.reflect.Array

object ReadKdbActor {
  
  import AbxStrategyActor._
  def props(serviceId: String) = Props(new ReadKdbActor(serviceId))
  sealed trait ReadKdbMessages
  case object ReadKdbMessage extends ReadKdbMessages
  case class ReadKdbResultMessage(result: List[Option[Double]]) extends ReadKdbMessages
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
			val result = getQuotesData()
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

	def getQuotesData(): List[Option[Double]] = {
	      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
		  log.debug("Connected to KDB server. Retrieving data")
		  val res = conn.k("reverse select [-10] high, low, close from quotes")
		  val tabres: Flip = res.asInstanceOf[Flip]
		  val colNames = tabres.x
		  val colData = tabres.y
		  
//		  Array.getLength(colData(0))
		  
//		  log.debug("^^^^^^^^^^^^ colData.size = {}", colData.size)
//		  log.debug("^^^^^^^^^^^^ colData.length = {}", colData.length)
//		  log.debug("^^^^^^^^^^^^ Array.getLength(colData(0)) = {}", Array.getLength(colData(0)))
//		  log.debug("^^^^^^^^^^^^ Array.getLength(colData(1)) = {}", Array.getLength(colData(1)))
//		  log.debug("^^^^^^^^^^^^ Array.getLength(colData(2)) = {}", Array.getLength(colData(2)))
		  
		  // close over max last 10 highs - buy. close under last 1 low - sell
		  val result= if(Array.getLength(colData(0)) > 9) {
			  val l2h: Option[Double] = Some((c.at(colData(0), 1)).asInstanceOf[Double])
			  val l2l: Option[Double] = Some((c.at(colData(1), 1)).asInstanceOf[Double])
			  val l2c: Option[Double] = Some((c.at(colData(2), 1)).asInstanceOf[Double])
			  val l1h: Option[Double] = Some((c.at(colData(0), 0)).asInstanceOf[Double])
			  val l1l: Option[Double] = Some((c.at(colData(1), 0)).asInstanceOf[Double])
			  val l1c: Option[Double] = Some((c.at(colData(2), 0)).asInstanceOf[Double])
			  
			  
			  val resMax = conn.k("select [-9] max high from reverse select [-10] high from quotes")
			  val tabresMax: Flip = resMax.asInstanceOf[Flip]
			  val colNamesMax = tabresMax.x
			  val colDataMax = tabresMax.y
			  val maxHigh: Option[Double] = Some((c.at(colDataMax(0), 0)).asInstanceOf[Double])
			  
//			  log.info("^^^^^^^^^^^^ List(l2h, l2l, l2c, l1h, l1l, l1c, maxHigh) = {}", List(l2h, l2l, l2c, l1h, l1l, l1c, maxHigh))
			  
//			  val kdb: KdbType = (c.at(colData(0), 0).asInstanceOf[Double], c.at(colData(1), 0).asInstanceOf[Double], c.at(colData(2), 0).asInstanceOf[Int], c.at(colData(3), 0).asInstanceOf[Boolean], c.at(colData(4), 0).asInstanceOf[Int])
		      List(l2h, l2l, l2c, l1h, l1l, l1c, maxHigh)
//			  log.debug("^^^^^^^^^^^^ data = {}", result)
//		      conn.close
//		      result
		  } else {
			  List(None, None, None, None, None, None, None)
//			  log.debug("^^^^^^^^^^^^ data = {}", result)
//		      conn.close
//		      result
		  }
		  log.debug("^^^^^^^^^^^^ data = {}", result)
	      conn.close
	      result
		  
	}
	
	override def postStop() = {
	}
	
	
	
}
