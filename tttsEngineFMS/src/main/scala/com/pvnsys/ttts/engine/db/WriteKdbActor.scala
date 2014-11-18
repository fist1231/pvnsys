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
import com.pvnsys.ttts.engine.impl.SimulatorEngineActor
import kx.c
import kx.c._
import kx.c.Flip
import com.typesafe.scalalogging.slf4j.LazyLogging

object WriteKdbActor extends LazyLogging {
  
  import SimulatorEngineActor._
  def props(tableId: String) = Props(new WriteKdbActor(tableId))
  sealed trait WriteKdbMessages
  case class WriteEngineKdbMessage(data: EngineKdbType) extends WriteKdbMessages
  case class WriteTransactionKdbMessage(data: TransactionKdbType) extends WriteKdbMessages
  case object StopWriteKdbActor extends WriteKdbMessages
  case object ResetEngineKdbMessage extends WriteKdbMessages
  
  def resetEngineData(tableId: String) = {

      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
      
      val funds = 5000.00
      val balance = 5000.00
      val transnum = 0L
      var intradeStr = "0b"
      val possize = 0L
      val price = 0.00
      
      val createEngineStr = s"engine$tableId" + """:([]funds:`float$();balance:`float$();transnum:`long$();intrade:`boolean$();possize:`long$();price:`float$())"""
      val createTradeStr = s"trade$tableId" + """:([]dts:`datetime$();sym:`symbol$();price:`float$();size:`long$();oper:`symbol$();cost:`float$())"""

 	  logger.debug("============== 1")
      conn.k(createEngineStr)
 	  logger.debug("WriteKdbActor created ENGINE table: {}", createEngineStr)
      
 	  val insertEngineDataStr = s"`engine$tableId insert($funds;$balance;$transnum;$intradeStr;$possize;$price)"
 	  conn.k(insertEngineDataStr)
 	  logger.debug("WriteKdbActor populated ENGINE initial data: {}", insertEngineDataStr)

      conn.k(createTradeStr)
 	  logger.debug("WriteKdbActor created TRADE table: {}", createTradeStr)
 	  
      conn close
  }	
  
  
  def setEngineData(tableId: String, data: EngineKdbType) = {
      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
//      val res = conn.k(s"update engine set funds=${data._1}, balance=${data._2}, transnum=${data._3}, intrade=${data._4}, possize=${data._5}")
      var intradeStr = "0b"
      if(data._4) {
        intradeStr = "1b"
      }
      val updateStr = s"engine$tableId:update funds:${data._1},balance:${data._2},transnum:${data._3},intrade:${intradeStr},possize:${data._5},price:${data._6} from engine$tableId" 
      conn.k(updateStr)
 	  logger.debug("WriteKdbActor updated ENGINE data with: {}", updateStr)
      conn close
  }

  
  def setTransactionData(tableId: String, data: TransactionKdbType) = {
      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
      val updateStr = s"`trade$tableId insert(${data._1};`${data._2};${data._3};${data._4};`${data._5};${data._6})" 
 	  logger.debug("WriteKdbActor updating TRADE table data with: {}", updateStr)
      conn.k(updateStr)
      conn close
  }
  
}

/**
 * This actor will do create/insert/update/delete operations on kdb database
 */
class WriteKdbActor(tableId: String) extends Actor with ActorLogging {
  
	import WriteKdbActor._
	import TttsEngineMessages._
    import SimulatorEngineActor._
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("WriteKdbActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	override def receive = {
		case ResetEngineKdbMessage => {
			log.debug("WriteKdbActor received ResetEngineKdbMessage")
			resetEngineData()
		}
		case msg: WriteEngineKdbMessage => {
			log.debug("WriteKdbActor received WriteEngineKdbMessage")
			setEngineData(msg.data)
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

	
  def resetEngineData() = {

      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
      
      val funds = 5000.00
      val balance = 5000.00
      val transnum = 0L
      var intradeStr = "0b"
      val possize = 0L
      
      val createEngineStr = s"engine$tableId" + """:([]funds:`float$();balance:`float$();transnum:`long$();intrade:`boolean$();possize:`long$();price:`float$())"""
      val createTradeStr = s"trade$tableId" + """:([]dts:`datetime$();sym:`symbol$();price:`float$();size:`long$();oper:`symbol$();cost:`float$())"""

 	  log.debug("============== 1")
      conn.k(createEngineStr)
 	  log.debug("WriteKdbActor created ENGINE table: {}", createEngineStr)
      
 	  val insertEngineDataStr = s"`engine$tableId insert($funds;$balance;$transnum;$intradeStr;$possize)"
 	  conn.k(insertEngineDataStr)
 	  log.debug("WriteKdbActor populated ENGINE initial data: {}", insertEngineDataStr)

      conn.k(createTradeStr)
 	  log.debug("WriteKdbActor created TRADE table: {}", createTradeStr)
 	  
 	  
//      val purgeStr = s"delete from trade" 
// 	  log.debug("WriteKdbActor purged TRADE table data")
//      conn.k(purgeStr)
      
      conn close
  }	
	
  def setEngineData(data: EngineKdbType) = {
      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
//      val res = conn.k(s"update engine set funds=${data._1}, balance=${data._2}, transnum=${data._3}, intrade=${data._4}, possize=${data._5}")
      
      var intradeStr = "0b"
      if(data._4) {
        intradeStr = "1b"
      }
      
      val updateStr = s"engine$tableId:update funds:${data._1},balance:${data._2},transnum:${data._3},intrade:${intradeStr},possize:${data._5},price:${data._6} from engine$tableId" 
      conn.k(updateStr)
 	  log.debug("WriteKdbActor updated ENGINE data with: {}", updateStr)

//    try {
//
//      val res = conn.k("select from engine")
//      val tabres: Flip = res.asInstanceOf[Flip]
//      val colNames = tabres.x
//      val colData = tabres.y
//
//      
////      val newColnames: Array[String] = Array("funds", "balance", "transnum", "intrade", "possize")
////      val newData: Array[Object] = Array(data._1.asInstanceOf[Object], data._2.asInstanceOf[Object], data._3.asInstanceOf[Object], data._4.asInstanceOf[Object], data._5.asInstanceOf[Object])
////      val newTabres: Flip = new Flip(new Dict(newColnames, newData))
////      val updStatement: Array[Object] = Array(".u.upd", "engine", newTabres)
////      conn.k(updStatement)
//
//      val newColnames = Array("funds", "balance", "transnum", "intrade", "possize")
//      val newData = Array(data._1, data._2, data._3, data._4, data._5)
//      val newTabres = new Flip(new Dict(newColnames, newData))
//      val updStatement = Array(".u.upd", "engine", newTabres)
//      conn.k(updStatement)
//      
//      
////      conn.ks(updStatement)
//      getEngineData()	
//      
//      c.set(colData(0), 0, data._1)
//      c.set(colData(1), 0, data._2)
//      c.set(colData(2), 0, data._3)
//      c.set(colData(3), 0, data._4)
//      c.set(colData(4), 0, data._5)
//      
//
//	} catch {
//	  case e: Throwable => logger.error("################## setEngineData Error updating engine: " + e)
//	  e.printStackTrace()
//	}
      conn close
      
  }

  
  /*
   * `trade insert(09:30:00.000;`a;10.75;100)
   * 
   * (String, String, Double, Long, String, Double)
   * 
   * select sum size by sym from trade
   * 
   */
  
  def setTransactionData(data: TransactionKdbType) = {
      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
      val updateStr = s"`trade$tableId insert(${data._1};`${data._2};${data._3};${data._4};`${data._5};${data._6})" 
 	  log.debug("WriteKdbActor updating TRADE table data with: {}", updateStr)
      conn.k(updateStr)
      conn close
  }
  
	override def postStop() = {
	   
	}
}
