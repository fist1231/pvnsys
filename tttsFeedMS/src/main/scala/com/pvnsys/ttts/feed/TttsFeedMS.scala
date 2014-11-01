package com.pvnsys.ttts.feed

import akka.actor.{ActorSystem, AllForOneStrategy, Props}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.feed.messages.TttsFeedMessages.StartFeedServiceMessage

object MyDomainProcessing extends LazyLogging {
}


object TttsFeedMS extends App with LazyLogging {
  
  implicit val tttsFeedActorSystem = ActorSystem("ttts-feed-service")
  
  val groupId: Option[String] = {
    if(args.length > 0) Some(args(0)) else None
  }

  val feedService = tttsFeedActorSystem.actorOf(Props(classOf[TttsFeedService]), "feedService")
  feedService ! StartFeedServiceMessage
  
  tttsFeedActorSystem.registerOnTermination {
    tttsFeedActorSystem.log.info("TttsFeedMS shutdown.")
  }
  
}  

object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("ttts-feed-ms.host")
  val portHttp = config.getInt("ttts-feed-ms.ports.http")
  val portWs   = config.getInt("ttts-feed-ms.ports.ws")

  val facadeTopic = config.getString("ttts-feed-ms.facade-topic")
  val servicesTopic = config.getString("ttts-feed-ms.services-topic")

  val metadataBrokerListProducer = config.getString("kafka-producer.metadata-broker-list")
  val serializerClassProducer = config.getString("kafka-producer.serializer-class")
  val zookeeperConnectionProducer = config.getString("kafka-producer.zookeeper-connection")
  val groupIdProducer = config.getString("kafka-producer.group-id")

  val zookeeperConnectionConsumer = config.getString("kafka-consumer.zookeeper-connection")
  val groupIdConsumer = config.getString("kafka-consumer.group-id")
  val socketBufferSizeConsumer = config.getString("kafka-consumer.socket-buffer-size")
  val fetchSizeConsumer = config.getString("kafka-consumer.fetch-size")
  val autoCommitConsumer = config.getString("kafka-consumer.auto-commit")
  val autocommitIntervalConsumer = config.getString("kafka-consumer.autocommit-interval-ms")
  val autooffsetResetConsumer = config.getString("kafka-consumer.autooffset-reset")

}
