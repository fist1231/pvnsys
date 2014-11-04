package com.pvnsys.ttts.strategy.mq

import akka.actor._
import java.net.InetSocketAddress
import java.util.Properties
import com.pvnsys.ttts.strategy.Configuration
import kafka.consumer.ConsumerConfig
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConnector

object KafkaConnectionActor {
  case object Connect
  def props(address: InetSocketAddress) = Props(new KafkaConnectionActor(address))
}

/**
 * Manager of Kafka connections.
 * 
 * Initiates a new connection on a Connect message and returns it to the sender.
 * Takes care of closing the connections on system close. 
 */
class KafkaConnectionActor(address: InetSocketAddress) extends Actor with ActorLogging {

  import KafkaConnectionActor._
  
//  val factory = new ConnectionFactory()
//  factory.setHost(address.getHostName())
//  factory.setPort(address.getPort())
  
  var connectors: List[ConsumerConnector] = Nil
  
  def receive = {
    case Connect => {
    	var groupId = Configuration.strategyGroupId
		val prps = new Properties()
		prps.put("group.id", groupId)
		prps.put("socket.buffer.size", Configuration.socketBufferSizeConsumer)
		prps.put("fetch.size", Configuration.fetchSizeConsumer)
		prps.put("auto.commit", Configuration.autoCommitConsumer)
		prps.put("autocommit.interval.ms", Configuration.autocommitIntervalConsumer)
		prps.put("autooffset.reset", Configuration.autooffsetResetConsumer)
		prps.put("zookeeper.connect", Configuration.zookeeperConnectionConsumer)
		
		val config = new ConsumerConfig(prps)
		val connector = Consumer.create(config)
		log.info("Connected to Kafka server on {}", Configuration.metadataBrokerListProducer)

		val client = sender()
	    connectors = connector :: connectors
	    client ! connector
    }
    case msg => log.error(s"Received unknown message $msg")
  }
  
  override def postStop() = {
    connectors foreach { connector => 
	    log.info("Closing connectors")
	    connector.shutdown
    }
  }

}
