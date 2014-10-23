package com.pvnsys.ttts.facade

import com.pvnsys.ttts.facade.api.{MainActors, TttsFacadeApi}
import com.pvnsys.ttts.facade.server.TttsFacadeMSServer
import akka.actor.ActorSystem
import akka.io.{IO, Tcp}
import java.net.InetSocketAddress
import spray.can.Http

object TttsFacadeMS extends App with MainActors with TttsFacadeApi {
  implicit lazy val system = ActorSystem("ttts-facade-service")
  private val rs = new TttsFacadeMSServer(Configuration.portWs)
  rs.forResource("/feed/ws", Some(feed))
  rs.start
  sys.addShutdownHook({system.shutdown;rs.stop})
  IO(Http) ! Http.Bind(rootService, Configuration.host, port = Configuration.portHttp)
}  

object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("ttts-facade-ms.host")
  val portHttp = config.getInt("ttts-facade-ms.ports.http")
  val portWs   = config.getInt("ttts-facade-ms.ports.ws")
  
  val metadataBrokerListProducer = config.getString("kafka-producer.metadata-broker-list")
  val serializerClassProducer = config.getString("kafka-producer.serializer-class")
  val topicProducer = config.getString("kafka-producer.topic")
  val zookeeperConnectionProducer = config.getString("kafka-producer.zookeeper-connection")
  val groupIdProducer = config.getString("kafka-producer.group-id")

  val topicConsumer = config.getString("kafka-consumer.topic")
  val zookeeperConnectionConsumer = config.getString("kafka-consumer.zookeeper-connection")
  val groupIdConsumer = config.getString("kafka-consumer.group-id")
  val socketBufferSizeConsumer = config.getString("kafka-consumer.socket-buffer-size")
  val fetchSizeConsumer = config.getString("kafka-consumer.fetch-size")
  val autoCommitConsumer = config.getString("kafka-consumer.auto-commit")
  val autocommitIntervalConsumer = config.getString("kafka-consumer.autocommit-interval-ms")
  val autooffsetResetConsumer = config.getString("kafka-consumer.autooffset-reset")

}
