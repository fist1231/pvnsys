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
import scala.sys.process._


object KdbActor {
  def props(serviceId: String) = Props(new KdbActor(serviceId))
  sealed trait KdbMessages
  case object StartKdbMessage extends KdbMessages
  case object StopKdbMessage extends KdbMessages
}



/**
 * This actor will register itself to consume messages from the Kafka server. 
 */
class KdbActor(serviceId: String) extends Actor with ActorLogging {
  
	import KdbActor._
	import TttsEngineMessages._
	
//	override val log = Logging(context.system, this)
	
    override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case e: Exception =>
      log.error("KdbActor Unexpected failure: {}", e.getMessage)
      Restart
  	}
	
	override def receive = {
		case StopKdbMessage => {
			log.debug("KdbActor StopMessage")
			//self ! PoisonPill
		}
		case StartKdbMessage => {
			log.debug(s"Start kdb in KdbActor")
			startKdb()
		}

		case _ => log.error("KdbActor Received unknown message")
	}
	

	final private def startKdb() = {
		  log.debug("KdbActor starting kdb server ...")
		  lazy val cmd = Seq("db/kdb/w32/q.exe", "db/kdb/engine.q", "-p", Configuration.kdbPort)
		  cmd.lineStream
		  
//		  proc = Process(cmd).run(ProcessLogger(line => (), err => println("Uh-oh: "+err)))
//		  proc.destroy
		  
		  log.debug(s"KdbActor started kdb server on port ${Configuration.kdbPort}")
	}
	
   
	override def postStop() = {
	   
	}
}
