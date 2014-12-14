package com.pvnsys.ttts.strategy.db

import akka.actor.ActorSystem
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.collection._
import scala.io.Source


@RunWith(classOf[JUnitRunner])
class ReadKdbActorTest extends FunSuite {
  implicit lazy val system = ActorSystem("ttts-strategy-service")
  var n = 1
  sys.addShutdownHook({system.shutdown})
  test("input file read") {

	  // copy existing id from 127.0.0.1:4444
	  val tableId = "_test"
    
	  val data = ReadKdbActor.getAbxQuotesWithBBData(tableId) 
	  data.foreach(println)
  }
  
}
