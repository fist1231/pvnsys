package com.pvnsys.ttts.strategy

import akka.actor.{ActorSystem, AllForOneStrategy, Props}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.StartStrategyServiceMessage
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.TttsStrategyMessage
import com.pvnsys.ttts.strategy.service.TttsStrategyService

object MyDomainProcessing extends LazyLogging {
}


object TttsStrategyMS extends App with LazyLogging {
  
  implicit val tttsStrategyActorSystem = ActorSystem("ttts-strategy-service")
  
  val groupId: Option[String] = {
    if(args.length > 0) Some(args(0)) else None
  }

  val strategyService = tttsStrategyActorSystem.actorOf(Props(classOf[TttsStrategyService]), "strategyService")
  strategyService ! StartStrategyServiceMessage
  
  tttsStrategyActorSystem.registerOnTermination {
    tttsStrategyActorSystem.log.info("TttsStrategyMS shutdown.")
  }
  
}  


object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("ttts-strategy-ms.host")
  val portHttp = config.getInt("ttts-strategy-ms.ports.http")
  val portWs   = config.getInt("ttts-strategy-ms.ports.ws")

  val facadeTopic = config.getString("ttts-strategy-ms.facade-topic")
  val facadeGroupId = config.getString("ttts-strategy-ms.facade-group-id")
  val servicesTopic = config.getString("ttts-strategy-ms.services-topic")
  val servicesGroupId = config.getString("ttts-strategy-ms.services-group-id")

  val metadataBrokerListProducer = config.getString("kafka-producer.metadata-broker-list")
  val serializerClassProducer = config.getString("kafka-producer.serializer-class")
  val zookeeperConnectionProducer = config.getString("kafka-producer.zookeeper-connection")

  val zookeeperConnectionConsumer = config.getString("kafka-consumer.zookeeper-connection")
  val socketBufferSizeConsumer = config.getString("kafka-consumer.socket-buffer-size")
  val fetchSizeConsumer = config.getString("kafka-consumer.fetch-size")
  val autoCommitConsumer = config.getString("kafka-consumer.auto-commit")
  val autocommitIntervalConsumer = config.getString("kafka-consumer.autocommit-interval-ms")
  val autooffsetResetConsumer = config.getString("kafka-consumer.autooffset-reset")

  val kdbHost = config.getString("kdb.host")
  val kdbPort = config.getString("kdb.port")
  
  
}
