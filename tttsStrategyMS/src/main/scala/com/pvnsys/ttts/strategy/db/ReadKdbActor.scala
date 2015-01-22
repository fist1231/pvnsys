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
	  
		  val numberOfTicks = 100
//		  val numberOfCloseTicksForBB = 20
		  val numberOfBB20Ticks = 20
		  val numberOfBB35Ticks = 35
		  val numberOfBB60Ticks = 60

		  val numberOfSma100Ticks = 100
		  
		  val minLowTicks = 40
		  val maxHighTicks = 20

	      val begin = new MInteger();
	      val length = new MInteger();
	
	      val upperBB20 = new Array[Double](numberOfBB20Ticks)
	      val middBB20 = new Array[Double](numberOfBB20Ticks)
	      val lowerBB20 = new Array[Double](numberOfBB20Ticks)

	      val upperBB35 = new Array[Double](numberOfBB35Ticks)
	      val middBB35 = new Array[Double](numberOfBB35Ticks)
	      val lowerBB35 = new Array[Double](numberOfBB35Ticks)

	      val upperBB60 = new Array[Double](numberOfBB60Ticks)
	      val middBB60 = new Array[Double](numberOfBB60Ticks)
	      val lowerBB60 = new Array[Double](numberOfBB60Ticks)

	      val sma100 = new Array[Double](numberOfSma100Ticks)
	      
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
		  val result= if(java.lang.reflect.Array.getLength(colData(0)) >= numberOfTicks) {
		    
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
			  val core = new Core
			  
			  // -2 - to get prev 2 days data (l1 and l2)
			  val closePrice20 = for(i <- (numberOfTicks - numberOfBB20Ticks - 2) to (numberOfTicks - 1)) yield ((c.at(colDataBB(2), i)).asInstanceOf[Double])
			  val closePriceArray20 = closePrice20.toArray
	          val retCode20 = core.bbands(0, closePriceArray20.length - 1, closePriceArray20, numberOfBB20Ticks, 2.0, 2.0, MAType.Ema, begin, length, upperBB20, middBB20, lowerBB20);

			  val closePrice35 = for(i <- (numberOfTicks - numberOfBB35Ticks - 2) to (numberOfTicks - 1)) yield ((c.at(colDataBB(2), i)).asInstanceOf[Double])
			  val closePriceArray35 = closePrice35.toArray
	          val retCode35 = core.bbands(0, closePriceArray35.length - 1, closePriceArray35, numberOfBB35Ticks, 2.0, 2.0, MAType.Sma, begin, length, upperBB35, middBB35, lowerBB35);

			  val closePrice60 = for(i <- (numberOfTicks - numberOfBB60Ticks - 2) to (numberOfTicks - 1)) yield ((c.at(colDataBB(2), i)).asInstanceOf[Double])
			  val closePriceArray60 = closePrice60.toArray
	          val retCode60 = core.bbands(0, closePriceArray60.length - 1, closePriceArray60, numberOfBB60Ticks, 2.0, 2.0, MAType.Sma, begin, length, upperBB60, middBB60, lowerBB60);

			  val closePrice100 = for(i <- 0 to (numberOfSma100Ticks - 1)) yield ((c.at(colDataBB(2), i)).asInstanceOf[Double])
			  val closePriceArray100 = closePrice100.toArray
	          val retCodeSma100 = core.sma(0, closePriceArray100.length - 1, closePriceArray100, numberOfSma100Ticks, begin, length, sma100);
			  
//			  closePrice20.foreach(println)
//			  println("=================")
//			  lowerBB20.foreach(println)
//			  println("=================")
//			  upperBB20.foreach(println)
			  

			  val l2LowerBB20Val = Some(lowerBB20(0))
			  val l2MiddBB20Val = Some(middBB20(0))
			  val l2UpperBB20Val = Some(upperBB20(0))

			  val l1LowerBB20Val = Some(lowerBB20(1))
			  val l1MiddBB20Val = Some(middBB20(1))
			  val l1UpperBB20Val = Some(upperBB20(1))
			  
			  val thisLowerBB20Val = Some(lowerBB20(2))
			  val thisMiddBB20Val = Some(middBB20(2))
			  val thisUpperBB20Val = Some(upperBB20(2))

			  
			  val l2LowerBB35Val = Some(lowerBB35(0))
			  val l2MiddBB35Val = Some(middBB35(0))
			  val l2UpperBB35Val = Some(upperBB35(0))

			  val l1LowerBB35Val = Some(lowerBB35(1))
			  val l1MiddBB35Val = Some(middBB35(1))
			  val l1UpperBB35Val = Some(upperBB35(1))
			  
			  val thisLowerBB35Val = Some(lowerBB35(2))
			  val thisMiddBB35Val = Some(middBB35(2))
			  val thisUpperBB35Val = Some(upperBB35(2))

			  
			  val l2LowerBB60Val = Some(lowerBB60(0))
			  val l2MiddBB60Val = Some(middBB60(0))
			  val l2UpperBB60Val = Some(upperBB60(0))

			  val l1LowerBB60Val = Some(lowerBB60(1))
			  val l1MiddBB60Val = Some(middBB60(1))
			  val l1UpperBB60Val = Some(upperBB60(1))
			  
			  val thisLowerBB60Val = Some(lowerBB60(2))
			  val thisMiddBB60Val = Some(middBB60(2))
			  val thisUpperBB60Val = Some(upperBB60(2))

			  val thisSma100Val = Some(sma100(0))
			  
			  
			  List(l2h, l2l, l2c, l1h, l1l, l1c, thish, thisl, thisc, minLow, maxHigh,
			      l2LowerBB20Val, l2MiddBB20Val, l2UpperBB20Val, l1LowerBB20Val, l1MiddBB20Val, l1UpperBB20Val, thisLowerBB20Val, thisMiddBB20Val, thisUpperBB20Val,
			      l2LowerBB35Val, l2MiddBB35Val, l2UpperBB35Val, l1LowerBB35Val, l1MiddBB35Val, l1UpperBB35Val, thisLowerBB35Val, thisMiddBB35Val, thisUpperBB35Val,
			      l2LowerBB60Val, l2MiddBB60Val, l2UpperBB60Val, l1LowerBB60Val, l1MiddBB60Val, l1UpperBB60Val, thisLowerBB60Val, thisMiddBB60Val, thisUpperBB60Val, thisSma100Val)
		  } else {
			  List(None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)
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
