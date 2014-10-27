package com.pvnsys.ttts.feed

import akka.actor.ActorSystem
import akka.io.{IO, Tcp}
import java.net.InetSocketAddress
import spray.can.Http
import com.pvnsys.ttts.feed.mq.KafkaConsumerActor
import akka.actor.Props
import com.typesafe.scalalogging.slf4j.LazyLogging
import akka.util.Timeout
import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import scala.concurrent.duration._


object MyDomainProcessing extends LazyLogging {

  def apply(): Duct[KafkaReceivedMessage, String] = { Duct[KafkaReceivedMessage].
  
    // acknowledge and pass on
    map { msg =>
      val z = msg.message.toUpperCase
      logger.debug("------- In da duck 1: {}", z)
      msg
    }.
    
    map { msg =>
      val x = msg.key
      x
    }
  }
  
}


object TttsFeedMS extends App with LazyLogging {
  
  implicit val tttsFeedActorSystem = ActorSystem("ttts-feed-service")
  
  val groupId: Option[String] = {
    if(args.length > 0) Some(args(0)) else None
  }

  
  implicit val timeout = Timeout(2 seconds)
  implicit val executor = tttsFeedActorSystem.dispatcher
  
  val materializer = FlowMaterializer(MaterializerSettings())
  
  
  val feedActor = tttsFeedActorSystem.actorOf(Props(classOf[FeedActor]), "feedConsumer")
  logger.debug("+++++++ feedActor is: {}", feedActor)
  val feedConsumer = ActorProducer(feedActor)

  val kafkaConsumerActor = tttsFeedActorSystem.actorOf(KafkaConsumerActor.props(feedActor), "kafkaConsumer")
  logger.debug("+++++++ KafkaConsumerActor tttsFeedActorSystem is: {}", kafkaConsumerActor)
  kafkaConsumerActor ! KafkaStartListeningMessage
  
  
  val domainProcessingDuct = MyDomainProcessing()

      Flow(feedConsumer) append domainProcessingDuct map { msg =>

	  	logger.debug("~~~~~~~ And inside the main Flow is: {}", msg)
    
//        // start a new flow for each message type
//        Flow(producer)
//        
//          // extract the message
//          .map(_.message) 
//          
//          // add the outbound publishing duct
//          .append(publisherDuct(exchange))
//          
//          // and start the flow
//          .consume(materializer)
        
    } consume(materializer)

  
  
  
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
