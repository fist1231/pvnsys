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
  type servicesMessageFlowOutDuctType = (String, Producer[TttsStrategyMessage])
  
  def apply(): Duct[TttsStrategyMessage, servicesMessageFlowOutDuctType] = Duct[TttsStrategyMessage].
	    // acknowledge and pass on
	    map { msg =>
	      val messageType = msg match {
	        case x: RequestStrategyServicesTopicMessage => x.asInstanceOf[RequestStrategyServicesTopicMessage].msgType 
	        case x: ResponseFeedFacadeTopicMessage => x.asInstanceOf[ResponseFeedFacadeTopicMessage].msgType 
	        case x: ResponseFeedServicesTopicMessage => x.asInstanceOf[ResponseFeedServicesTopicMessage].msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.debug("StrategyMS ServicesMessageFlow duct step 1; received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      val messageClient = msg match {
	        case x: RequestStrategyServicesTopicMessage => x.asInstanceOf[RequestStrategyServicesTopicMessage].client 
	        case x: ResponseFeedFacadeTopicMessage => x.asInstanceOf[ResponseFeedFacadeTopicMessage].client
	        case x: ResponseFeedServicesTopicMessage => x.asInstanceOf[ResponseFeedServicesTopicMessage].client
	        case _ => "UNKNOWN"
	      }
	      logger.debug("Strategy's ServicesMessageFlow duct step 2; converting ServicesTopicMessage to RequestStrategyServicesTopicMessage for Client is: {}", messageClient)
	      msg
	    }.
	
	    map {
	        logger.debug("Strategy's ServicesMessageFlow duct step 3; starting strategy")
	        /*
	         * Returns either:
	         * - RequestStrategyServicesTopicMessage of type TttsStrategyMessage - Strategy intended for Services Topic
	         * - ResponseFeedFacadeTopicMessage of type TttsStrategyMessage - Feed intended for Facade Topic
	         * - ResponseFeedServicesTopicMessage of type TttsStrategyMessage - Feed intended for Services Topic
	         */
	        StrategyService.convertServicesMessage
	    }.
	    
	    groupBy {
	      // Splits stream of Messages by message type and returns map(String -> org.reactivestreams.api.Producer[TttsStrategyMessage]) 
	      case msg: ResponseFeedFacadeTopicMessage => "FeedFacade"
	      case msg: ResponseFeedServicesTopicMessage => "FeedServices"
	      case msg: RequestStrategyServicesTopicMessage => "StrategyServices"
	      case _ => "Garbage"
	    }
  
}


class ServicesMessageFlow(strategyServicesActor: ActorRef, serviceUniqueID: String)(implicit context: ActorContext) extends StrategyServiceFlow with LazyLogging {
  
	import ServicesMessageFlow._
	import TttsStrategyMessages._

	implicit val executor = context.dispatcher
    val materializer = FlowMaterializer(MaterializerSettings())
	
	
    val strategyServicesConsumer = ActorProducer(strategyServicesActor)
  	val servicesMessageDuct = ServicesMessageFlow()
  	
	val strategies = mutable.Map[String, ActorRef]()
	val producerDuct: Duct[TttsStrategyMessage, Unit] = 
			Duct[TttsStrategyMessage] foreach {msg =>
				  /*
				   * For every new feed request add client -> feedActor to the Map
				   * For every feed termination request, find feedActor in the Map, stop it and remove entry from the Map 
				   */
				msg match {
					case x: RequestStrategyServicesTopicMessage => {
					  x.asInstanceOf[RequestStrategyServicesTopicMessage].msgType match {
						    case STRATEGY_REQUEST_MESSAGE_TYPE => {
						    	logger.debug("Got STRATEGY_REQUEST_MESSAGE_TYPE. Key {}", x.asInstanceOf[RequestStrategyServicesTopicMessage].client)	          
							    val strategyExecutorActor = context.actorOf(StrategyExecutorActor.props(serviceUniqueID))
							    logger.debug("Starting StrategyExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[RequestStrategyServicesTopicMessage].client, strategyExecutorActor)
							    strategies += (x.asInstanceOf[RequestStrategyServicesTopicMessage].client -> strategyExecutorActor)
							    strategyExecutorActor ! x
						    }
						    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => {
						    	logger.debug("Got STRATEGY_STOP_REQUEST_MESSAGE_TYPE. Key {}", x.asInstanceOf[RequestStrategyServicesTopicMessage].client)	
	//					    	feeds.foreach { case (key, value) => logger.debug(" key: {} ==> value: {}", key, value) }
						    	
						        strategies.get(x.asInstanceOf[RequestStrategyServicesTopicMessage].client) match {
								  case Some(strategyExecActor) => {
								    logger.debug("Stopping StrategyExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[RequestStrategyServicesTopicMessage].client, strategyExecActor)
								    strategyExecActor ! x
//								    strategyExecActor ! StopStrategyExecutorMessage
								    strategies -= x.asInstanceOf[RequestStrategyServicesTopicMessage].client 
								  }
								  case None => logger.debug("No such Strategy to stop. Key {}", x.asInstanceOf[RequestStrategyServicesTopicMessage].client)
								}
						    }
						    case _ =>
					  } 
					}
			        case x: ResponseFeedFacadeTopicMessage => {
			          x.asInstanceOf[ResponseFeedFacadeTopicMessage].msgType match {
						    case FEED_RESPONSE_MESSAGE_TYPE => {
						    	logger.debug("Got FEED_RESPONSE_MESSAGE_TYPE. Key {}", x.asInstanceOf[ResponseFeedFacadeTopicMessage].client)	          
							    val strategyExecutorActor = context.actorOf(StrategyExecutorActor.props(serviceUniqueID))
							    logger.debug("Starting StrategyExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseFeedFacadeTopicMessage].client, strategyExecutorActor)
	//						    strategies += (msg.client -> strategyExecutorActor)
							    strategyExecutorActor ! x
						    }
						    case _ =>
			          }
			        }
			        case x: ResponseFeedServicesTopicMessage => {
			          x.asInstanceOf[ResponseFeedServicesTopicMessage].msgType match {
						    case FEED_RESPONSE_MESSAGE_TYPE => {
						    	logger.debug("Got FEED_RESPONSE_MESSAGE_TYPE. Key {}", x.asInstanceOf[ResponseFeedServicesTopicMessage].client)	          
							    val strategyExecutorActor = context.actorOf(StrategyExecutorActor.props(serviceUniqueID))
							    logger.debug("Starting StrategyExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseFeedServicesTopicMessage].client, strategyExecutorActor)
	//						    strategies += (msg.client -> strategyExecutorActor)
							    strategyExecutorActor ! x
						    }
						    case _ =>
			          }
			        }
			        case _ =>
				}
	}

  	
//	val feedServicesDuct: Duct[TttsStrategyMessage, Unit] =  
//	val strategyServicesDuct: Duct[TttsStrategyMessage, Unit] =  
  	
  	
	override def startFlow() = Flow(strategyServicesConsumer) append servicesMessageDuct map {
		case (str, producer) => 
//		  	log.debug("~~~~~~~ And inside the main Flow is: {}", producer)
		// start a new flow for each message type
		Flow(producer)
			// extract the client
			//	          .map(_.client) 
			// add the outbound publishing duct
			.append(producerDuct)
			// and start the flow
			.consume(materializer)
	    
	} consume(materializer)
  	

}