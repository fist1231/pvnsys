package com.pvnsys.ttts.strategy.flows

import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages.{TttsStrategyMessage, ServicesTopicMessage, RequestStrategyServicesTopicMessage, ResponseFeedServicesTopicMessage}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.strategy.generator.StrategyService
import org.reactivestreams.api.Producer
import akka.actor.{ActorRef, ActorContext, Props}
import scala.collection.mutable
import scala.collection.mutable.Map
import com.pvnsys.ttts.strategy.generator.StrategyExecutorActor.StopStrategyExecutorMessage
import com.pvnsys.ttts.strategy.generator.StrategyExecutorActor
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages


object ServicesMessageFlow extends LazyLogging {

  import TttsStrategyMessages._
  type servicesMessageFlowOutDuctType = (String, Producer[ResponseFeedServicesTopicMessage])
  
  def apply(): Duct[ResponseFeedServicesTopicMessage, servicesMessageFlowOutDuctType] = Duct[ResponseFeedServicesTopicMessage].
	    // acknowledge and pass on
	    map { msg =>
	      val z = msg.msgType 
	      logger.debug("Strategy's ServicesMessageFlow duct step 1; message Type is: {}", z)
	      msg
	    }.
	    
	    map { msg =>
	      val x = msg.client 
	      logger.debug("Strategy's ServicesMessageFlow duct step 2; converting ServicesTopicMessage to RequestStrategyServicesTopicMessage for Client is: {}", x)
	      msg
	    }.
	
	    map {
	        logger.debug("Strategy's ServicesMessageFlow duct step 3; starting strategy")
	        StrategyService.convertServicesMessage
	    }.
	    
	    groupBy {
	      case msg: ResponseFeedServicesTopicMessage => "Outloop"
	    }
  
}


class ServicesMessageFlow(strategyServicesActor: ActorRef)(implicit context: ActorContext) extends StrategyServiceFlow with LazyLogging {
  
	import ServicesMessageFlow._
	import TttsStrategyMessages._

	implicit val executor = context.dispatcher
    val materializer = FlowMaterializer(MaterializerSettings())
	
	
    val strategyServicesConsumer = ActorProducer(strategyServicesActor)
  	val servicesMessageDuct = ServicesMessageFlow()
  	
	val strategies = mutable.Map[String, ActorRef]()
	val strategyPublisherDuct: Duct[ResponseFeedServicesTopicMessage, Unit] = 
			Duct[ResponseFeedServicesTopicMessage] foreach {msg =>
				  /*
				   * For every new feed request add client -> feedActor to the Map
				   * For every feed termination request, find feedActor in the Map, stop it and remove entry from the Map 
				   */
				  msg.msgType match {
					    case STRATEGY_REQUEST_MESSAGE_TYPE => {
					    	logger.debug("Got STRATEGY_REQUEST_MESSAGE_TYPE. Key {}", msg.client)	          
						    val strategyExecutorActor = context.actorOf(Props(classOf[StrategyExecutorActor]))
						    logger.debug("Starting StrategyExecutorActor. Key {}; ActorRef {}", msg.client, strategyExecutorActor)
						    strategies += (msg.client -> strategyExecutorActor)
						    strategyExecutorActor ! msg
					    }
					    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => {
					    	logger.debug("Got STRATEGY_STOP_REQUEST_MESSAGE_TYPE. Key {}", msg.client)	
//					    	feeds.foreach { case (key, value) => logger.debug(" key: {} ==> value: {}", key, value) }
					    	
					        strategies.get(msg.client) match {
							  case Some(strategyExecActor) => {
							    logger.debug("Stopping StrategyExecutorActor. Key {}; ActorRef {}", msg.client, strategyExecActor)
							    strategyExecActor ! msg
							    strategyExecActor ! StopStrategyExecutorMessage
							    strategies -= msg.client 
							  }
							  case None => logger.debug("No such Strategy to stop. Key {}", msg.client)
							}
					    }
					    case FEED_RESPONSE_MESSAGE_TYPE => {
					    	logger.debug("Got FEED_RESPONSE_MESSAGE_TYPE. Key {}", msg.client)	          
						    val strategyExecutorActor = context.actorOf(Props(classOf[StrategyExecutorActor]))
						    logger.debug("Starting StrategyExecutorActor. Key {}; ActorRef {}", msg.client, strategyExecutorActor)
//						    strategies += (msg.client -> strategyExecutorActor)
						    strategyExecutorActor ! msg
					    }
				  }
			}
  	
  	
  	
	override def startFlow() = Flow(strategyServicesConsumer) append servicesMessageDuct map {
		case (str, producer) => 
	//		  	log.debug("~~~~~~~ And inside the main Flow is: {}", producer)
	// For every message start new Flow that produces feed
	//		  	val kafkaProducerActor = context.actorOf(KafkaProducerActor.props(new InetSocketAddress("127.0.0.1", 5672)))
	//		  	kafkaProducerActor ! msg
		
		// start a new flow for each message type
		Flow(producer)
			// extract the client
			//	          .map(_.client) 
			// add the outbound publishing duct
			.append(strategyPublisherDuct)
			// and start the flow
			.consume(materializer)
	    
	} consume(materializer)
  	

}