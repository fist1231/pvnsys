package com.pvnsys.ttts.feed

import akka.actor.ActorSystem
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.collection._
import scala.io.Source


@RunWith(classOf[JUnitRunner])
class TttsFMSTest extends FunSuite {
  implicit lazy val system = ActorSystem("ttts-feed-service")
  var n = 1
  sys.addShutdownHook({system.shutdown})
  test("input file read") {

  	  val filename = "in/quotes.csv"
//  	  var iter = Source.fromFile(filename).getLines
//  	  iter.drop(1).foreach(x=>println(x))
  	  val initSize = Source.fromFile(filename).getLines.length
  	    var count = 0
	  while(true) {
	    getLine(filename, initSize, increment(count))
	    
//	    getLine(filename)
	  } 
    
  }
  
  private def increment(i: Int) = {
    val res = i + 1
    res
  }
  
  
  final def getLine(filename: String, z: Int, count: Int) = {
//    println(z)
		  val iter = Source.fromFile(filename).getLines
		  println(s"z=$z")
		  println(s"count=$count")
          var n = count%z
		  println(s"n=$n")
          if(n != 0) {
			  val line = iter.drop(n).next.toString.split(",")
	//		  val line = iter.take(1).next.toString.split(",")
			  println(line)
			  print(s"date: ${line(0)}    ")
			  print(s"open: ${line(1)}    ")
			  print(s"high: ${line(2)}    ")
			  print(s"low: ${line(3)}     ")
			  print(s"close: ${line(4)}   ")
			  println(s"vol: ${line(5)}")
			  val is = iter.size
			  println(is)
		      Thread.sleep(1000)
		      is match {
		        case 0 => {
		          val k = iter.length
	//	          println(s"Making n=0 when n = $n and iter.size = ${k}")
	//	          n = 0
		          Source.fromFile(filename).reset
		        }
		        case _ => 
		      }
	//	      n += 1
            
          }
    
  }
  
  final def getLine(filename: String) = {
//          var n = 1
		  val iter = Source.fromFile(filename).getLines
		  val line = iter.drop(n).next.toString.split(",")
		  println(line)
		  println(s"date: ${line(0)}")
		  println(s"open: ${line(1)}")
		  println(s"high: ${line(2)}")
		  println(s"low: ${line(3)}")
		  println(s"close: ${line(4)}")
		  println(s"vol: ${line(5)}")
		  val is = iter.size
	      Thread.sleep(1000)
	      is match {
	        case 0 => {
	          val k = iter.length
//	          println(s"Making n=0 when n = $n and iter.size = ${k}")
	          n = 0
	          Source.fromFile(filename).reset
	        }
	        case _ => 
	      }
	      n += 1
    
  }
  
  
}
