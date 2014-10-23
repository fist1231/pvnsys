package com.pvnsys.ttts.feed

import akka.actor.ActorSystem
import akka.io.{IO, Tcp}
import java.net.InetSocketAddress
import spray.can.Http

import com.pvnsys.ttts.feed.mq.KafkaConsumerActor

object TttsFeedMS extends App {
  implicit lazy val system = ActorSystem("ttts-feed-service")
  
  val groupId: Option[String] = {
    if(args.length > 0) Some(args(0)) else None
  }

  val kafkaConsumerActor = system.actorOf(KafkaConsumerActor.props(new InetSocketAddress("127.0.0.1", 5672),  groupId ))
}  

object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("ttts-feed-ms.host")
  val portHttp = config.getInt("ttts-feed-ms.ports.http")
  val portWs   = config.getInt("ttts-feed-ms.ports.ws")
  
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
