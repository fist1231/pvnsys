package com.pvnsys.ttts.feed.impl

import com.pvnsys.ttts.feed.messages.TttsFeedMessages
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.feed.util.Utils
import scala.io.Source

object FakeFeed {
}

/**
 * Example of some strategy.
 * 
 */
class FakeFeed extends Feed with LazyLogging {

  import TttsFeedMessages._

  override def process(msg: TttsFeedMessage) = {
    msg
  }
  
  def process(msg: TttsFeedMessage, initSize: Int, count: Int) = {
    
//	  	var counter = count
    
        logger.debug(s"FakeFeed, processing message: {}", msg)
	    
        val messageTraits = Utils.generateMessageTraits
	    msg match {
			case msg: RequestFeedFacadeTopicMessage => {
//			  counter += 1
		    	val fakeQuote = getQuoteFromFile(initSize, count)
			    ResponseFeedFacadeTopicMessage(messageTraits._1, FEED_RESPONSE_MESSAGE_TYPE, msg.client , s"$fakeQuote", messageTraits._2, s"$count")
			}
			case msg: RequestFeedServicesTopicMessage => {
//			  counter += 1
		    	val fakeQuote = getQuoteFromFile(initSize, count)
			    ResponseFeedServicesTopicMessage(messageTraits._1, FEED_RESPONSE_MESSAGE_TYPE, msg.client , s"$fakeQuote", messageTraits._2, s"$count", msg.serviceId)
			}
		    case _ =>  {
		      logger.error("FakeStrategy Received unsupported message type")
		      msg
		    }
	    }

  }

  
	// returns close price from file one at a time. When reaches the EOF, restarts from the beginning. Skips first 'headers' line
	final def getQuoteFromFile(z: Int, count: Int) = {
		  val filename = "in/quotes.csv"
		  val iter = Source.fromFile(filename).getLines
          var n = count%z
          if(n != 0) {
			  val line = iter.drop(n).next.toString.split(",")
//			  println(line)
//			  print(s"date: ${line(0)}    ")
//			  print(s"open: ${line(1)}    ")
//			  print(s"high: ${line(2)}    ")
//			  print(s"low: ${line(3)}     ")
//			  print(s"close: ${line(4)}   ")
//			  println(s"vol: ${line(5)}")
			  val is = iter.size
			  val closePrice = line(4)
//			  println(is)
//		      Thread.sleep(1000)
		      is match {
		        case 0 => Source.fromFile(filename).reset
		        case _ => 
		      }
	//	      n += 1
              closePrice
          }
    
  }
  
}
  
