package com.pvnsys.ttts.strategy

import akka.actor.{ActorSystem, AllForOneStrategy, Props}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.StartStrategyServiceMessage
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.TttsStrategyMessage
import com.pvnsys.ttts.strategy.service.TttsStrategyService
import akka.stream.actor.ActorConsumer
import com.pvnsys.ttts.strategy.mq.KafkaServicesTopicProducerActor
import com.pvnsys.ttts.strategy.mq.GlobalConsumerActor
import akka.stream.MaterializerSettings
import akka.stream.FlowMaterializer
import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import com.pvnsys.ttts.strategy.mq.StrategyActor
import com.pvnsys.ttts.strategy.mq.KafkaServicesTopicConsumerActor
import com.pvnsys.ttts.strategy.util.Utils
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.pvnsys.ttts.strategy.mq.StrategyProducerActor

object MyDomainProcessing extends LazyLogging {
}


object TttsStrategyMS extends App with LazyLogging {
  
  implicit val tttsStrategyActorSystem = ActorSystem("ttts-strategy-service")
  
  val groupId: Option[String] = {
    if(args.length > 0) Some(args(0)) else None
  }

  
//		import TttsStrategyMessages._
//		val serviceUniqueID = Utils.generateUuid
//		implicit val executor = tttsStrategyActorSystem.dispatcher
//	    val materializer = FlowMaterializer(MaterializerSettings())
//		
//		val strategyServicesActor = tttsStrategyActorSystem.actorOf(Props(classOf[StrategyActor]))
//		// Start Kafka consumer actor for incoming messages from Services Topic
//		val kafkaServicesTopicConsumerActor = tttsStrategyActorSystem.actorOf(KafkaServicesTopicConsumerActor.props(strategyServicesActor, serviceUniqueID), "strategyKafkaServicesConsumer")
//		kafkaServicesTopicConsumerActor ! StartListeningServicesTopicMessage
//	    
//	    
//	    val strategyProducerActor = tttsStrategyActorSystem.actorOf(Props(classOf[StrategyProducerActor]))
//	    val queueConsumer = ActorConsumer[TttsStrategyMessage](tttsStrategyActorSystem.actorOf(GlobalConsumerActor.props(strategyProducerActor)))
//		Flow(ActorProducer(strategyServicesActor)).produceTo(materializer, queueConsumer)
  
  
  val strategyService = tttsStrategyActorSystem.actorOf(Props(classOf[TttsStrategyService]), "strategyService")
  strategyService ! StartStrategyServiceMessage
//  strategyService ! StartBPStrategyServiceMessage(strategyProducerActor)
  
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
