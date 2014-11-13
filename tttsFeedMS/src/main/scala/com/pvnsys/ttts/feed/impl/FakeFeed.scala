package com.pvnsys.ttts.feed.impl

import com.pvnsys.ttts.feed.messages.TttsFeedMessages
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.feed.util.Utils
import scala.io.Source
import java.util.Date

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
		    	val payload = getQuoteFromFile(initSize, count)
		        payload match {
		        	case Some(feedPayload) => {
//				    	val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//				    	val datetime = sdf.format(new Date)
		        		ResponseFeedFacadeTopicMessage(messageTraits._1, FEED_RESPONSE_MESSAGE_TYPE, msg.client, Some(feedPayload), messageTraits._2, s"$count")
		        	}
		        	case None => // "Nothing"
		    	}
			}
			case msg: RequestFeedServicesTopicMessage => {
//			  counter += 1
		    	val payload = getQuoteFromFile(initSize, count)
		        payload match {
		        	case Some(feedPayload) => {
//				    	val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//				    	val datetime = sdf.format(new Date)
		        		ResponseFeedServicesTopicMessage(messageTraits._1, FEED_RESPONSE_MESSAGE_TYPE, msg.client, Some(feedPayload), messageTraits._2, s"$count", msg.serviceId)
		        	}
		        	case None => // "Nothing"
		    	}
			}
		    case _ =>  {
		      logger.error("FakeFeed Received unsupported message type")
		      msg
		    }
	    }

  }

  
	// returns close price from file one at a time. When reaches the EOF, restarts from the beginning. Skips first 'headers' line
	final def getQuoteFromFile(z: Int, count: Int): Option[FeedPayload] = {
		  val filename = "in/quotes.csv"
		  val iter = Source.fromFile(filename).getLines
          val n = count%z
          n match {
		    case 0 => None
		    case _ => {
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
              Some(FeedPayload(line(0), "ABX", line(1).toDouble, line(2).toDouble, line(3).toDouble, line(4).toDouble, line(5).toLong, line(6).toDouble, line(8).toLong, "tbd"))
		    }
		  }
//          if(n != 0) {
//          }
    
  }
  
}
  
