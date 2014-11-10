package com.pvnsys.ttts.engine.impl

import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.engine.util.Utils
import java.sql._
import kx.c
import kx.c._
import kx.c.Flip

object FakeEngine {
}

/**
 * Example of some engine.
 * 
 */
class SimulatorEngine extends Engine with LazyLogging {

  import TttsEngineMessages._
  import Engine._

  var resultStatus = false
  
  type KdbType = (Float, Float, Int, Boolean)

  override def process(msg: TttsEngineMessage, isInTrade: Boolean): EngineType = {
    
    /*
     * Do Engine processing, create ResponseEngineFacadeTopicMessage (reply to FacadeMS)
     * 
     */ 
    resultStatus = isInTrade

//try{c c=new c("localhost",5001);                    // connect                                    
// Object[]x={new Time(System.currentTimeMillis()%86400000),"xx",new Double(93.5),new Integer(300)};
// c.k("insert","trade",x);                           // insert                                     
// Object r=c.k("select sum size by sym from trade"); // select                                     
//}catch(Exception e){}                                                                             
 
      val c: c = new c("localhost", 5555)
      val res = c.k("select from engine")
      println(s"+++++++++++++++++++++++ res = $res")
      val tabres: Flip = res.asInstanceOf[Flip]
      val colNames = tabres.x
      colNames.foreach(println)
//      println(s"+++++++++++++++++++++++ colNames = $colNames")
      val colData = tabres.y
      val firstRowData = colData(0)
//      firstRowData.at
      colData.foreach(println)
//      println(s"+++++++++++++++++++++++ colData = $colData")
    
      
      println(s"+++++++++++++++++++++++ firstRowData.asInstanceOf[Float] = ${firstRowData.asInstanceOf[Float]}") 
      println(s"+++++++++++++++++++++++ colData(1) = ${colData(1).asInstanceOf[Float]}") 
      println(s"+++++++++++++++++++++++ colData(2) = ${colData(2).asInstanceOf[Int]}") 
      println(s"+++++++++++++++++++++++ colData(3) = ${colData(3).asInstanceOf[Boolean]}") 
      
//      println(s"+++++++++++++++++++++++ tabres.at(colNames(0)) = ${tabres.at(colNames(0))}") 
//      println(s"+++++++++++++++++++++++ tabres.at(colNames(1)) = ${tabres.at(colNames(1))}") 
//      println(s"+++++++++++++++++++++++ tabres.at(colNames(2)) = ${tabres.at(colNames(2))}") 
//      println(s"+++++++++++++++++++++++ tabres.at(colNames(3)) = ${tabres.at(colNames(3))}") 
      
//      val kdb: KdbType = (colData(0).asInstanceOf[Float], colData(1).asInstanceOf[Float], colData(2).asInstanceOf[Int], colData(3).asInstanceOf[Boolean])
//      println(s"+++++++++++++++++++++++ kdb = $kdb")

      c close
    
    
//    val kdb: KdbType = "select from engine"
    
    
    // 1. Do some fake Engine processing here. Replace with real code.
//    val fraction = msg.asInstanceOf[ResponseEngineFacadeTopicMessage].payload.toDouble - msg.asInstanceOf[ResponseEngineFacadeTopicMessage].payload.toDouble.intValue
//    val signal = fraction match {
//      case x if(x < 0.2) => "BUY"
//      case x if(x > 0.8) => "SELL"
//      case _ => "HOLD"
//    }
    
    // 2. Create ResponseEngineFacadeTopicMessage

    // Generate unique message ID, timestamp and sequence number to be assigned to every message.
    val messageTraits = Utils.generateMessageTraits


    /*
     * - Assign unique message id and timestamp generated by Utils.generateMessageTraits.
     * - Pass along client, payload and sequenceNum from Engine message.
     * - Add signal field
     * - Return ResponseEngineFacadeTopicMessage or ResponseEngineServiceTopicMessage, depending on incoming message type.
     */ 
    msg match {
	    case x: ResponseStrategyFacadeTopicMessage => {
	       val payload = s"${x.payload} ==> ${play(x.signal)}"
	       (ResponseEngineFacadeTopicMessage(messageTraits._1, ENGINE_RESPONSE_MESSAGE_TYPE, x.client, payload, messageTraits._2, x.sequenceNum, x.signal), resultStatus)
	    }
	    case x: ResponseStrategyServicesTopicMessage => {
	       val payload = s"${x.payload} ==> ${play(x.signal)}"
	       (ResponseEngineServicesTopicMessage(messageTraits._1, ENGINE_RESPONSE_MESSAGE_TYPE, x.client, payload, messageTraits._2, x.sequenceNum, x.signal, x.serviceId), resultStatus)	      
	    }
	    case _ =>  {
	      logger.error("FakeEngine Received unsupported message type")
	      (msg, resultStatus)
	    }
    }

  }
  
  def play(signal: String) = {
    signal match {
      case "BUY" => if(!resultStatus) {
        resultStatus = true
        "BOUGHT"
      	} else {
      	  "POSITION OPENED ALREADY"
      	}
      case "SELL" => if(resultStatus) {
        resultStatus = false
        "SOLD"
      	} else {
      	  "NOT IN A POSITION"
      	}
      case "HOLD" => "PASS"
      case _ => "Nothing"
    }
    
  }

}
  
