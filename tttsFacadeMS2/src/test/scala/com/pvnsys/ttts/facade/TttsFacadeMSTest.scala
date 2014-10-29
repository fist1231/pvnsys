package com.pvnsys.ttts.facade

import akka.actor.ActorSystem
import java.net.URI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.pvnsys.ttts.facade.server.TttsFacadeMSServer
import com.pvnsys.ttts.facade.api.{MainActors, TttsFacadeApi}
import org.java_websocket.WebSocket
import com.pvnsys.ttts.facade.mq.KafkaConsumerActor
import java.net.InetSocketAddress
import scala.collection._


@RunWith(classOf[JUnitRunner])
class TttsFacadeMSTest extends FunSuite with MainActors with TttsFacadeApi {
  implicit lazy val system = ActorSystem("ttts-facade-service")
  sys.addShutdownHook({system.shutdown})
  test("websocket connection") {
    val rs = new TttsFacadeMSServer(Configuration.portWs)
    rs.forResource("/feed/ws", Some(feed))
    rs.start
//    find ! FindActor.Clear

    Thread.sleep(2000L) // wait for all servers to be cleanly started
    val kafkaConsumerActor = system.actorOf(KafkaConsumerActor.props(new InetSocketAddress("127.0.0.1", 5672)))

    var i = 1 
    val clients = mutable.Map[String, WebSocketClient]()
    while(i <= 1000) {
       var wsmsg = ""
	    val webSocket = new WebSocketClient(URI.create(s"ws://localhost:${Configuration.portWs}/feed/ws")) {
	      override def onMessage(msg : String) {
	        wsmsg = msg
	        println(s" ==> 444444444444444444444444444444444", msg)
	      }
	      override def onOpen(hs : ServerHandshake) {
	//	    var i = 1
	//	    val msg = s"TEST Client $i says: Omg, wtf ?"
	//	    i += 1
	//	    
	//	    send(msg)
	      }
	      override def onClose(code : Int, reason : String, intentional : Boolean) {}
	      override def onError(ex : Exception) {println(ex.getMessage)}
	    }
       
       
       clients += (s"$i" -> webSocket)
       i += 1
       
//    	runClient(i)
    }
    
//    clients.foreach((a) => println(s"=== ${a._1} ===> ${a._2.getRemoteSocketAddress()} ==="))
    
    clients.map {
      case (key, client) =>
      println(s"=== $key ===> ${client} ===")
      (key -> client)
    }.
    map {
      case (key, client) =>
      client.connect
      println(s"++++ Connecting client $key ++++")
      Thread.sleep(1000L)
      (key -> client)
    }.
    map {
      case (key, client) =>
      if(client.isOpen()) {
        println(s"~~~~ sending message from client $key")
    	  client.send(s"Let's roll client: ${key}")
      }
    }
    
    while(true) {
    	println("***************** Endless loop in progress, please use the hummer to break the glass ... *****************")
    	Thread.sleep(100000L)
    }

  }
  
//     private def runClient(cnt: Int) = {
//
////	    Thread.sleep(1000L)
//	    webSocket.connect
//	//    webSocket.`
////	    Thread.sleep(1000L)
//	//    
//	    var i = 1
//	    val msg = s"TEST Client $i says: Omg, wtf ?"
//	    i += 1
//	//    
//	    webSocket.send(msg)
////	    Thread.sleep(1000L) 
//	//    assert(None != first.findFirstIn(wsmsg))
//	//    webSocket.close
//	//    Thread.sleep(1000L)
//	//    rs.stop
//      
//    }

}
