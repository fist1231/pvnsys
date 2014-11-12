package com.pvnsys.ttts.engine

import akka.actor.{ActorSystem, AllForOneStrategy, Props}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.engine.messages.TttsEngineMessages.StartEngineServiceMessage
import com.pvnsys.ttts.engine.service.TttsEngineFService

object MyDomainProcessing extends LazyLogging {
}


object TttsEngineFMS extends App with LazyLogging {
  
  implicit val tttsEngineFActorSystem = ActorSystem("ttts-engineF-service")
  
  val groupId: Option[String] = {
    if(args.length > 0) Some(args(0)) else None
  }

  val engineFService = tttsEngineFActorSystem.actorOf(Props(classOf[TttsEngineFService]), "engineFService")
  engineFService ! StartEngineServiceMessage
  
  tttsEngineFActorSystem.registerOnTermination {
    tttsEngineFActorSystem.log.info("TttsEngineFMS shutdown.")
  }
  
}  

object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("ttts-engineF-ms.host")
  val portHttp = config.getInt("ttts-engineF-ms.ports.http")
  val portWs   = config.getInt("ttts-engineF-ms.ports.ws")

  val facadeTopic = config.getString("ttts-engineF-ms.facade-topic")
  val facadeGroupId = config.getString("ttts-engineF-ms.facade-group-id")
  val servicesTopic = config.getString("ttts-engineF-ms.services-topic")
  val servicesGroupId = config.getString("ttts-engineF-ms.services-group-id")

  val metadataBrokerListProducer = config.getString("kafka-producer.metadata-broker-list")
  val serializerClassProducer = config.getString("kafka-producer.serializer-class")
  val zookeeperConnectionProducer = config.getString("kafka-producer.zookeeper-connection")
//  val groupIdProducer = config.getString("kafka-producer.group-id")

  val zookeeperConnectionConsumer = config.getString("kafka-consumer.zookeeper-connection")
//  val groupIdConsumer = config.getString("kafka-consumer.group-id")
  val socketBufferSizeConsumer = config.getString("kafka-consumer.socket-buffer-size")
  val fetchSizeConsumer = config.getString("kafka-consumer.fetch-size")
  val autoCommitConsumer = config.getString("kafka-consumer.auto-commit")
  val autocommitIntervalConsumer = config.getString("kafka-consumer.autocommit-interval-ms")
  val autooffsetResetConsumer = config.getString("kafka-consumer.autooffset-reset")
  
  
  val kdbHost = config.getString("kdb.host")
  val kdbPort = config.getString("kdb.port")

}
