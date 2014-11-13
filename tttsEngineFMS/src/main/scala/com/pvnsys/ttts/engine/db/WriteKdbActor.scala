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

object WriteKdbActor {
  
  import SimulatorEngineActor._
  def props(serviceId: String) = Props(new WriteKdbActor(serviceId))
  sealed trait WriteKdbMessages
  case class WriteEngineKdbMessage(data: EngineKdbType) extends WriteKdbMessages
  case class WriteTransactionKdbMessage(data: TransactionKdbType) extends WriteKdbMessages
  case object StopWriteKdbActor extends WriteKdbMessages
}

/**
 * This actor will do create/insert/update/delete operations on kdb database
 */
class WriteKdbActor(serviceId: String) extends Actor with ActorLogging {
  
	import WriteKdbActor._
	import TttsEngineMessages._
    import SimulatorEngineActor._
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("WriteKdbActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	override def receive = {
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

  def setEngineData(data: EngineKdbType) = {
      val conn: c = new c(Configuration.kdbHost, Configuration.kdbPort.toInt)
//      val res = conn.k(s"update engine set funds=${data._1}, balance=${data._2}, transnum=${data._3}, intrade=${data._4}, possize=${data._5}")
      
      var intradeStr = "0b"
      if(data._4) {
        intradeStr = "1b"
      }
      
      val updateStr = s"engine:update funds:${data._1},balance:${data._2},transnum:${data._3},intrade:${intradeStr},possize:${data._5} from engine" 
      conn.k(updateStr)
 	  log.debug("WriteKdbActor updated engine data with: {}", updateStr)

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
      val updateStr = s"`trade insert(${data._1};`${data._2};${data._3};${data._4};`${data._5};${data._6})" 
 	  log.debug("WriteKdbActor updating TRADE table data with: {}", updateStr)
      conn.k(updateStr)
      conn close
  }
  
	override def postStop() = {
	   
	}
}
