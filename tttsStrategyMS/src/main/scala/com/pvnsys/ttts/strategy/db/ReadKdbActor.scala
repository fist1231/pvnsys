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
import com.pvnsys.ttts.strategy.impl.Strategy
import kx.c
import kx.c._
import kx.c.Flip
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.tictactec.ta.lib.MInteger
import com.tictactec.ta.lib.Core
import com.tictactec.ta.lib.MAType

object ReadKdbActor extends LazyLogging {
  
  import Strategy._
  def props(tableId: String) = Props(new ReadKdbActor(tableId))
  sealed trait ReadKdbMessages
  case object ReadKdbMessage extends ReadKdbMessages
  case class ReadKdbResultMessage(result: List[Option[Double]]) extends ReadKdbMessages
  case object StopReadKdbActor extends ReadKdbMessages
  
  
	def getQuotesData(tableId: String): List[Option[Double]] = {
	  
		  val numberOfTicks = 2
		  val minLowTicks = 2
		  val maxHighTicks = 2
	  
	      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
		  logger.debug("Connected to KDB server. Retrieving data")
		  
		  // Select last $numberOfTicks ticks' high low and close prices
		  val res = conn.k(s"reverse select [-$numberOfTicks] high, low, close from quotes$tableId")
		  val tabres: Flip = res.asInstanceOf[Flip]
		  val colNames = tabres.x
		  val colData = tabres.y
		  
//		  Array.getLength(colData(0))
		  
//		  log.debug("^^^^^^^^^^^^ colData.size = {}", colData.size)
//		  log.debug("^^^^^^^^^^^^ colData.length = {}", colData.length)
//		  log.debug("^^^^^^^^^^^^ Array.getLength(colData(0)) = {}", Array.getLength(colData(0)))
//		  log.debug("^^^^^^^^^^^^ Array.getLength(colData(1)) = {}", Array.getLength(colData(1)))
//		  log.debug("^^^^^^^^^^^^ Array.getLength(colData(2)) = {}", Array.getLength(colData(2)))
		  
		  /*
		   * == Buy signal:
		   * If last tick close above max of last $numberOfTicks high
		   * Select max of last $numberOfTicks highs minus the very last one.
		   * Because the last close is never above the last high :)
		   * 
		   * == Sell signal:
		   * If last tick close is below previous tick low
		   */ 
		  val result= if(java.lang.reflect.Array.getLength(colData(0)) > (numberOfTicks - 1)) {
			  val l2h: Option[Double] = Some((c.at(colData(0), 1)).asInstanceOf[Double])
			  val l2l: Option[Double] = Some((c.at(colData(1), 1)).asInstanceOf[Double])
			  val l2c: Option[Double] = Some((c.at(colData(2), 1)).asInstanceOf[Double])
			  val l1h: Option[Double] = Some((c.at(colData(0), 0)).asInstanceOf[Double])
			  val l1l: Option[Double] = Some((c.at(colData(1), 0)).asInstanceOf[Double])
			  val l1c: Option[Double] = Some((c.at(colData(2), 0)).asInstanceOf[Double])
			  
			  // Select max high of last $numberOfTicks
			  val resMax = conn.k(s"select [-${maxHighTicks-1}] max high from reverse select [-$maxHighTicks] high from quotes$tableId")
			  val tabresMax: Flip = resMax.asInstanceOf[Flip]
			  val colNamesMax = tabresMax.x
			  val colDataMax = tabresMax.y
			  val maxHigh: Option[Double] = Some((c.at(colDataMax(0), 0)).asInstanceOf[Double])

			  val resMin = conn.k(s"select [-${minLowTicks-1}] min low from reverse select [-$minLowTicks] low from quotes$tableId")
			  val tabresMin: Flip = resMax.asInstanceOf[Flip]
			  val colNamesMin = tabresMin.x
			  val colDataMin = tabresMin.y
			  val minLow: Option[Double] = Some((c.at(colDataMin(0), 0)).asInstanceOf[Double])
			  
//			  log.info("^^^^^^^^^^^^ List(l2h, l2l, l2c, l1h, l1l, l1c, maxHigh) = {}", List(l2h, l2l, l2c, l1h, l1l, l1c, maxHigh))
			  
//			  val kdb: KdbType = (c.at(colData(0), 0).asInstanceOf[Double], c.at(colData(1), 0).asInstanceOf[Double], c.at(colData(2), 0).asInstanceOf[Int], c.at(colData(3), 0).asInstanceOf[Boolean], c.at(colData(4), 0).asInstanceOf[Int])
		      List(l2h, l2l, l2c, l1h, l1l, l1c, minLow, maxHigh)
//			  log.debug("^^^^^^^^^^^^ data = {}", result)
//		      conn.close
//		      result
		  } else {
			  List(None, None, None, None, None, None, None, None)
//			  log.debug("^^^^^^^^^^^^ data = {}", result)
//		      conn.close
//		      result
		  }
		  logger.debug("^^^^^^^^^^^^ data = {}", result)
	      conn.close
	      result
		  
	}
  
  
	def getAbxQuotesWithBBData(tableId: String): List[Option[Double]] = {
	  
		  val numberOfTicks = 22
		  val numberOfCloseTicksForBB = 20
		  val numberOfBBTicks = 20
		  val minLowTicks = 40
		  val maxHighTicks = 20

	      val begin = new MInteger();
	      val length = new MInteger();
	
	      val upperBB = new Array[Double](numberOfBBTicks)
	      val middBB = new Array[Double](numberOfBBTicks)
	      val lowerBB = new Array[Double](numberOfBBTicks)

	      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
		  logger.debug("Connected to KDB server. Retrieving data")
		  
		  // Select last $numberOfTicks ticks' high low and close prices
		  val res = conn.k(s"reverse select [-$numberOfTicks] high, low, close from quotes$tableId")
		  val tabres: Flip = res.asInstanceOf[Flip]
		  val colNames = tabres.x
		  val colData = tabres.y
		  
		  
		  /*
		   * == Buy signal:
		   * If last tick close below BB Low - buy
		   * Select max of last $numberOfTicks highs minus the very last one.
		   * Because the last close is never above the last high :)
		   * 
		   * == Sell signal:
		   * If last tick close is above BB Middle
		   */ 
		  val result= if(java.lang.reflect.Array.getLength(colData(0)) > numberOfTicks-1) {
			  val thish: Option[Double] = Some((c.at(colData(0), 0)).asInstanceOf[Double])
			  val thisl: Option[Double] = Some((c.at(colData(1), 0)).asInstanceOf[Double])
			  val thisc: Option[Double] = Some((c.at(colData(2), 0)).asInstanceOf[Double])
			  val l1h: Option[Double] = Some((c.at(colData(0), 1)).asInstanceOf[Double])
			  val l1l: Option[Double] = Some((c.at(colData(1), 1)).asInstanceOf[Double])
			  val l1c: Option[Double] = Some((c.at(colData(2), 1)).asInstanceOf[Double])
			  val l2h: Option[Double] = Some((c.at(colData(0), 2)).asInstanceOf[Double])
			  val l2l: Option[Double] = Some((c.at(colData(1), 2)).asInstanceOf[Double])
			  val l2c: Option[Double] = Some((c.at(colData(2), 2)).asInstanceOf[Double])
			  
			  // Select max high of last $numberOfTicks
			  val resMax = conn.k(s"select [-${maxHighTicks-1}] max high from reverse select [-$maxHighTicks] high from quotes$tableId")
			  val tabresMax: Flip = resMax.asInstanceOf[Flip]
			  val colNamesMax = tabresMax.x
			  val colDataMax = tabresMax.y
			  val maxHigh: Option[Double] = Some((c.at(colDataMax(0), 0)).asInstanceOf[Double])

			  val resMin = conn.k(s"select [-${minLowTicks-1}] min low from reverse select [-$minLowTicks] low from quotes$tableId")
			  val tabresMin: Flip = resMin.asInstanceOf[Flip]
			  val colNamesMin = tabresMin.x
			  val colDataMin = tabresMin.y
			  val minLow: Option[Double] = Some((c.at(colDataMin(0), 0)).asInstanceOf[Double])
			  
//			  log.info("^^^^^^^^^^^^ List(l2h, l2l, l2c, l1h, l1l, l1c, maxHigh) = {}", List(l2h, l2l, l2c, l1h, l1l, l1c, maxHigh))
			  
			  // Select last $numberOfTicks ticks' high low and close prices
			  val resBB = conn.k(s"select [-$numberOfTicks] high, low, close from quotes$tableId")
			  val tabresBB: Flip = resBB.asInstanceOf[Flip]
			  val colNamesBB = tabresBB.x
			  val colDataBB = tabresBB.y
			  
			  
			  var i = 0;
			  val cp = for(i <- 0 to (numberOfTicks-1)) yield ((c.at(colDataBB(2), i)).asInstanceOf[Double])
			  val closePrice = cp.toArray
			  
			  val core = new Core
			  // Take last 2 BB values, thus (numberOfBBTicks-1)
	          val retCode = core.bbands(0, closePrice.length - 1, closePrice, numberOfBBTicks, 2.0, 2.0, MAType.Ema, begin, length, upperBB, middBB, lowerBB);
			  
//			  closePrice.foreach(println)
//			  println("=================")
//			  lowerBB.foreach(println)
//			  println("=================")
//			  upperBB.foreach(println)
			  

			  val l2LowerBBVal = Some(lowerBB(0))
			  val l2MiddBBVal = Some(middBB(0))
			  val l2UpperBBVal = Some(upperBB(0))

			  val l1LowerBBVal = Some(lowerBB(1))
			  val l1MiddBBVal = Some(middBB(1))
			  val l1UpperBBVal = Some(upperBB(1))
			  
			  val thisLowerBBVal = Some(lowerBB(2))
			  val thisMiddBBVal = Some(middBB(2))
			  val thisUpperBBVal = Some(upperBB(2))
			  
			  
			  List(l2h, l2l, l2c, l1h, l1l, l1c, thish, thisl, thisc, minLow, maxHigh, l2LowerBBVal, l2MiddBBVal, l2UpperBBVal, l1LowerBBVal, l1MiddBBVal, l1UpperBBVal, thisLowerBBVal, thisMiddBBVal, thisUpperBBVal)
		  } else {
			  List(None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)
		  }
		  logger.debug("^^^^^^^^^^^^ data = {}", result)
	      conn.close
	      result
		  
	}
  
  
}

/**
 * This actor will do read-only operations on kdb database
 */
class ReadKdbActor(tableId: String) extends Actor with ActorLogging {
  
	import ReadKdbActor._
	import TttsStrategyMessages._
	import Strategy._
	
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


//	def getStrategyData(): StrategyKdbType = {
//	      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
//		  log.debug("Connected to KDB server. Retrieving data")
//		  val res = conn.k(s"select from strategy$tableId")
//		  val tabres: Flip = res.asInstanceOf[Flip]
//		  val colNames = tabres.x
//		  val colData = tabres.y
//		  
//		  val funds: Double = (c.at(colData(0), 0)).asInstanceOf[Double]
//		  val balance: Double = (c.at(colData(1), 0)).asInstanceOf[Double]
//		  val transnum: Long = (c.at(colData(2), 0)).asInstanceOf[Long]
//		  val intrade: Boolean = (c.at(colData(3), 0)).asInstanceOf[Boolean]
//		  val possize: Long = (c.at(colData(4), 0)).asInstanceOf[Long]
//		//      val kdb: KdbType = (c.at(colData(0), 0).asInstanceOf[Double], c.at(colData(1), 0).asInstanceOf[Double], c.at(colData(2), 0).asInstanceOf[Int], c.at(colData(3), 0).asInstanceOf[Boolean], c.at(colData(4), 0).asInstanceOf[Int])
//		  val kdb: StrategyKdbType = (funds, balance, transnum, intrade, possize)
//		  log.debug("^^^^^^^^^^^^ data = {}", kdb)
//	      conn.close
//	      kdb
//	}

	def getQuotesData(): List[Option[Double]] = {
	  
		  val numberOfTicks = 4 
	  
	      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
		  log.debug("Connected to KDB server. Retrieving data")
		  
		  // Select last $numberOfTicks ticks' high low and close prices
		  val res = conn.k(s"reverse select [-$numberOfTicks] high, low, close from quotes$tableId")
		  val tabres: Flip = res.asInstanceOf[Flip]
		  val colNames = tabres.x
		  val colData = tabres.y
		  
//		  Array.getLength(colData(0))
		  
//		  log.debug("^^^^^^^^^^^^ colData.size = {}", colData.size)
//		  log.debug("^^^^^^^^^^^^ colData.length = {}", colData.length)
//		  log.debug("^^^^^^^^^^^^ Array.getLength(colData(0)) = {}", Array.getLength(colData(0)))
//		  log.debug("^^^^^^^^^^^^ Array.getLength(colData(1)) = {}", Array.getLength(colData(1)))
//		  log.debug("^^^^^^^^^^^^ Array.getLength(colData(2)) = {}", Array.getLength(colData(2)))
		  
		  /*
		   * == Buy signal:
		   * If last tick close above max of last $numberOfTicks high
		   * Select max of last $numberOfTicks highs minus the very last one.
		   * Because the last close is never above the last high :)
		   * 
		   * == Sell signal:
		   * If last tick close is below previous tick low
		   */ 
		  val result= if(java.lang.reflect.Array.getLength(colData(0)) > (numberOfTicks - 1)) {
			  val l2h: Option[Double] = Some((c.at(colData(0), 1)).asInstanceOf[Double])
			  val l2l: Option[Double] = Some((c.at(colData(1), 1)).asInstanceOf[Double])
			  val l2c: Option[Double] = Some((c.at(colData(2), 1)).asInstanceOf[Double])
			  val l1h: Option[Double] = Some((c.at(colData(0), 0)).asInstanceOf[Double])
			  val l1l: Option[Double] = Some((c.at(colData(1), 0)).asInstanceOf[Double])
			  val l1c: Option[Double] = Some((c.at(colData(2), 0)).asInstanceOf[Double])
			  
			  // Select max high of last $numberOfTicks
			  val resMax = conn.k(s"select [-${numberOfTicks-1}] max high from reverse select [-$numberOfTicks] high from quotes$tableId")
			  val tabresMax: Flip = resMax.asInstanceOf[Flip]
			  val colNamesMax = tabresMax.x
			  val colDataMax = tabresMax.y
			  val maxHigh: Option[Double] = Some((c.at(colDataMax(0), 0)).asInstanceOf[Double])

			  val resMin = conn.k(s"select [-${numberOfTicks-1}] min low from reverse select [-$numberOfTicks] low from quotes$tableId")
			  val tabresMin: Flip = resMax.asInstanceOf[Flip]
			  val colNamesMin = tabresMin.x
			  val colDataMin = tabresMin.y
			  val minLow: Option[Double] = Some((c.at(colDataMin(0), 0)).asInstanceOf[Double])
			  
//			  log.info("^^^^^^^^^^^^ List(l2h, l2l, l2c, l1h, l1l, l1c, maxHigh) = {}", List(l2h, l2l, l2c, l1h, l1l, l1c, maxHigh))
			  
//			  val kdb: KdbType = (c.at(colData(0), 0).asInstanceOf[Double], c.at(colData(1), 0).asInstanceOf[Double], c.at(colData(2), 0).asInstanceOf[Int], c.at(colData(3), 0).asInstanceOf[Boolean], c.at(colData(4), 0).asInstanceOf[Int])
		      List(l2h, l2l, l2c, l1h, l1l, l1c, minLow, maxHigh)
//			  log.debug("^^^^^^^^^^^^ data = {}", result)
//		      conn.close
//		      result
		  } else {
			  List(None, None, None, None, None, None, None, None)
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
