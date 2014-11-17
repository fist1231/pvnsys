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
import com.typesafe.scalalogging.slf4j.LazyLogging

object WriteKdbActor extends LazyLogging {
  
  import AbxStrategyActor._
  def props(tableId: String) = Props(new WriteKdbActor(tableId))
  sealed trait WriteKdbMessages
  case class WriteStrategyKdbMessage(data: StrategyKdbType) extends WriteKdbMessages
  case class WriteTransactionKdbMessage(data: TransactionKdbType) extends WriteKdbMessages
  case object StopWriteKdbActor extends WriteKdbMessages
  case object ResetStrategyKdbMessage extends WriteKdbMessages
  case class ResponseResetStrategyKdbMessage(msg: String) extends WriteKdbMessages
  case class ResponseWriteTransactionKdbMessage(msg: String) extends WriteKdbMessages
  
  
  def resetStrategyData(tableId: String) = {

      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
      
      val funds = 5000.00
      val balance = 5000.00
      val transnum = 0L
      var intradeStr = "0b"
      val possize = 0L
 	  logger.info("============== 1  tableID= {}", tableId)

      val createStrategyStr = s"strategy$tableId" + """:([]funds:`float$();balance:`float$();transnum:`long$();intrade:`boolean$();possize:`long$())"""
      val createQuotesStr = s"quotes$tableId" + """:([]dts:`datetime$();sym:`symbol$();open:`float$();high:`float$();low:`float$();close:`float$();volume:`long$();wap:`float$();size:`long$())"""

      conn.k(createStrategyStr)
 	  logger.info("WriteKdbActor created STRATEGY table: {}", createStrategyStr)
      
 	  val insertStrategyDataStr = s"`strategy$tableId insert($funds;$balance;$transnum;$intradeStr;$possize)"
 	  conn.k(insertStrategyDataStr)
 	  logger.info("WriteKdbActor populated STRATEGY initial data: {}", insertStrategyDataStr)
      
//      val updateStr = s"strategy:update funds:$funds,balance:$balance,transnum:$transnum,intrade:$intradeStr,possize:$possize from strategy" 
//      conn.k(updateStr)
// 	  log.debug("WriteKdbActor reset STRATEGY data with: {}", updateStr)

      conn.k(createQuotesStr)
 	  logger.info("WriteKdbActor created TRADE table: {}", createQuotesStr)
 	  
 	  
//      val purgeStr = s"delete from quotes" 
// 	  log.debug("WriteKdbActor purged QUOTES table data")
//      conn.k(purgeStr)
      
      conn close
  }	
  
  def setTransactionData(tableId: String, data: TransactionKdbType) = {
      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
//	  quotes:([]dts:`datetime$();sym:`symbol$();open:`float$();high:`float$();low:`float$();close:`float$();volume:`long$();wap:`float$();size:`long$())
	  val updateStr = s"`quotes$tableId insert(${data._1};`${data._2};${data._3};${data._4};${data._5};${data._6};${data._7};${data._8};${data._9})" 
 	  logger.info("WriteKdbActor updating TRADE table data with: {}", updateStr)
      conn.k(updateStr)
      
      conn close
      
  }
  
  
}

/**
 * This actor will do create/insert/update/delete operations on kdb database
 */
class WriteKdbActor(tableId: String) extends Actor with ActorLogging {
  
	import WriteKdbActor._
	import TttsStrategyMessages._
    import AbxStrategyActor._
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("WriteKdbActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	override def receive = {
		case ResetStrategyKdbMessage => {
			log.debug("WriteKdbActor received ResetStrategyKdbMessage")
			val client = sender 
			resetStrategyData(client)
		}
		case msg: WriteStrategyKdbMessage => {
			log.debug("WriteKdbActor received WriteStrategyKdbMessage")
			setStrategyData(msg.data)
		}
		case msg: WriteTransactionKdbMessage => {
			log.debug("WriteKdbActor received WriteTransactionKdbMessage")
			val client = sender 
			setTransactionData(client, msg.data)
		}
		case StopWriteKdbActor => {
			log.debug("WriteKdbActor received StopMessage")
			//self ! PoisonPill
			context stop self
		}
		case _ => log.error("WriteKdbActor Received unknown message")
	}

	
	
  def resetStrategyData(client: ActorRef) = {

      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
      
      val funds = 5000.00
      val balance = 5000.00
      val transnum = 0L
      var intradeStr = "0b"
      val possize = 0L
 	  log.info("============== 1  tableID= {}", tableId)

      val createStrategyStr = s"strategy$tableId" + """:([]funds:`float$();balance:`float$();transnum:`long$();intrade:`boolean$();possize:`long$())"""
      val createQuotesStr = s"quotes$tableId" + """:([]dts:`datetime$();sym:`symbol$();open:`float$();high:`float$();low:`float$();close:`float$();volume:`long$();wap:`float$();size:`long$())"""

      conn.k(createStrategyStr)
 	  log.info("WriteKdbActor created STRATEGY table: {}", createStrategyStr)
      
 	  val insertStrategyDataStr = s"`strategy$tableId insert($funds;$balance;$transnum;$intradeStr;$possize)"
 	  conn.k(insertStrategyDataStr)
 	  log.info("WriteKdbActor populated STRATEGY initial data: {}", insertStrategyDataStr)
      
//      val updateStr = s"strategy:update funds:$funds,balance:$balance,transnum:$transnum,intrade:$intradeStr,possize:$possize from strategy" 
//      conn.k(updateStr)
// 	  log.debug("WriteKdbActor reset STRATEGY data with: {}", updateStr)

      conn.k(createQuotesStr)
 	  log.info("WriteKdbActor created TRADE table: {}", createQuotesStr)
 	  
 	  
//      val purgeStr = s"delete from quotes" 
// 	  log.debug("WriteKdbActor purged QUOTES table data")
//      conn.k(purgeStr)
      
      conn close
      
      client ! ResponseResetStrategyKdbMessage("success")
  }	
	
	
	
  def setStrategyData(data: StrategyKdbType) = {
      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
//      val res = conn.k(s"update strategy set funds=${data._1}, balance=${data._2}, transnum=${data._3}, intrade=${data._4}, possize=${data._5}")
      
      var intradeStr = "0b"
      if(data._4) {
        intradeStr = "1b"
      }
      
      val updateStr = s"strategy$tableId:update funds:${data._1},balance:${data._2},transnum:${data._3},intrade:${intradeStr},possize:${data._5} from strategy$tableId" 
      conn.k(updateStr)
 	  log.info("WriteKdbActor updated STRATEGY data with: {}", updateStr)

      conn close
      
  }

  
  /*
   * 
   * (String, String, Double, Double, Double, Double, Long, Double, Long)
   * 
   * select sum size by sym from quotes
   * 
   */
  
  def setTransactionData(client: ActorRef, data: TransactionKdbType) = {
      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
//	  quotes:([]dts:`datetime$();sym:`symbol$();open:`float$();high:`float$();low:`float$();close:`float$();volume:`long$();wap:`float$();size:`long$())
	  val updateStr = s"`quotes$tableId insert(${data._1};`${data._2};${data._3};${data._4};${data._5};${data._6};${data._7};${data._8};${data._9})" 
 	  log.info("WriteKdbActor updating TRADE table data with: {}", updateStr)
      conn.k(updateStr)
      
      conn close
      
      client ! ResponseWriteTransactionKdbMessage("success")
  }
  
	override def postStop() = {
	   
	}
}
