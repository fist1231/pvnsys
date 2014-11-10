package com.pvnsys.ttts.engine.flows

import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import com.pvnsys.ttts.engine.messages.TttsEngineMessages.{TttsEngineMessage, ServicesTopicMessage, RequestEngineServicesTopicMessage}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.engine.generator.EngineService
import org.reactivestreams.api.Producer
import akka.actor.{ActorRef, ActorContext, Props}
import scala.collection.mutable
import scala.collection.mutable.Map
import com.pvnsys.ttts.engine.generator.EngineExecutorActor.StopEngineExecutorMessage
import com.pvnsys.ttts.engine.generator.EngineExecutorActor
import com.pvnsys.ttts.engine.messages.TttsEngineMessages


object ServicesMessageFlow extends LazyLogging {

  import TttsEngineMessages._
  type servicesMessageFlowOutDuctType = (String, Producer[TttsEngineMessage])
  
  def apply(): Duct[TttsEngineMessage, servicesMessageFlowOutDuctType] = Duct[TttsEngineMessage].
	    // acknowledge and pass on
	    map { msg =>
	      val messageType = msg match {
	        case x: RequestEngineServicesTopicMessage => x.asInstanceOf[RequestEngineServicesTopicMessage].msgType 
	        case x: ResponseStrategyFacadeTopicMessage => x.asInstanceOf[ResponseStrategyFacadeTopicMessage].msgType 
	        case x: ResponseStrategyServicesTopicMessage => x.asInstanceOf[ResponseStrategyServicesTopicMessage].msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.debug("EngineFMS ServicesMessageFlow duct step 1; received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      val messageClient = msg match {
	        case x: RequestEngineServicesTopicMessage => x.asInstanceOf[RequestEngineServicesTopicMessage].client 
	        case x: ResponseStrategyFacadeTopicMessage => x.asInstanceOf[ResponseStrategyFacadeTopicMessage].client
	        case x: ResponseStrategyServicesTopicMessage => x.asInstanceOf[ResponseStrategyServicesTopicMessage].client
	        case _ => "UNKNOWN"
	      }
	      logger.debug("Engine's ServicesMessageFlow duct step 2; converting ServicesTopicMessage to RequestEngineServicesTopicMessage for Client is: {}", messageClient)
	      msg
	    }.
	
	    map {
	        logger.debug("Engine's ServicesMessageFlow duct step 3; starting engine")
	        /*
	         * Returns either:
	         * - RequestEngineServicesTopicMessage of type TttsEngineMessage - Engine intended for Services Topic
	         * - ResponseStrategyFacadeTopicMessage of type TttsEngineMessage - Strategy intended for Facade Topic
	         * - ResponseStrategyServicesTopicMessage of type TttsEngineMessage - Strategy intended for Services Topic
	         */
	        EngineService.convertServicesMessage
	    }.
	    
	    groupBy {
	      // Splits stream of Messages by message type and returns map(String -> org.reactivestreams.api.Producer[TttsEngineMessage]) 
	      case msg: ResponseStrategyFacadeTopicMessage => "StrategyFacade"
	      case msg: ResponseStrategyServicesTopicMessage => "StrategyServices"
	      case msg: RequestEngineServicesTopicMessage => "EngineServices"
	      case _ => "Trash"
	    }
  
}


class ServicesMessageFlow(engineServicesActor: ActorRef, serviceUniqueID: String)(implicit context: ActorContext) extends EngineServiceFlow with LazyLogging {
  
	import ServicesMessageFlow._
	import TttsEngineMessages._

	implicit val executor = context.dispatcher
    val materializer = FlowMaterializer(MaterializerSettings())
	
	
    val engineServicesConsumer = ActorProducer(engineServicesActor)
  	val servicesMessageDuct = ServicesMessageFlow()
  	
	val strategies = mutable.Map[String, ActorRef]()
	val producerDuct: Duct[TttsEngineMessage, Unit] = 
			Duct[TttsEngineMessage] foreach {msg =>
				  /*
				   * For every new engine request add client -> engineActor to the Map
				   * For every engine termination request, find engineActor in the Map, stop it and remove entry from the Map 
				   */
				msg match {
					case x: RequestEngineServicesTopicMessage => {
					  x.asInstanceOf[RequestEngineServicesTopicMessage].msgType match {
						    case ENGINE_REQUEST_MESSAGE_TYPE => {
						    	logger.debug("Got ENGINE_REQUEST_MESSAGE_TYPE. Key {}", x.asInstanceOf[RequestEngineServicesTopicMessage].client)	          
							    val engineExecutorActor = context.actorOf(EngineExecutorActor.props(serviceUniqueID))
							    logger.debug("Starting EngineExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[RequestEngineServicesTopicMessage].client, engineExecutorActor)
							    strategies += (x.asInstanceOf[RequestEngineServicesTopicMessage].client -> engineExecutorActor)
							    engineExecutorActor ! x
						    }
						    case ENGINE_STOP_REQUEST_MESSAGE_TYPE => {
						    	logger.debug("Got ENGINE_STOP_REQUEST_MESSAGE_TYPE. Key {}", x.asInstanceOf[RequestEngineServicesTopicMessage].client)	
	//					    	feeds.foreach { case (key, value) => logger.debug(" key: {} ==> value: {}", key, value) }
						    	
						        strategies.get(x.asInstanceOf[RequestEngineServicesTopicMessage].client) match {
								  case Some(engineExecActor) => {
								    logger.debug("Stopping EngineExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[RequestEngineServicesTopicMessage].client, engineExecActor)
								    engineExecActor ! x
//								    engineExecActor ! StopEngineExecutorMessage
								    strategies -= x.asInstanceOf[RequestEngineServicesTopicMessage].client 
								  }
								  case None => logger.debug("No such Engine to stop. Key {}", x.asInstanceOf[RequestEngineServicesTopicMessage].client)
								}
						    }
						    case _ =>
					  } 
					}
			        case x: ResponseStrategyFacadeTopicMessage => {
			          x.asInstanceOf[ResponseStrategyFacadeTopicMessage].msgType match {
						    case STRATEGY_RESPONSE_MESSAGE_TYPE => {
						    	logger.debug("Got STRATEGY_RESPONSE_MESSAGE_TYPE. Key {}", x.asInstanceOf[ResponseStrategyFacadeTopicMessage].client)	          
							    val engineExecutorActor = context.actorOf(EngineExecutorActor.props(serviceUniqueID))
							    logger.debug("Starting EngineExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseStrategyFacadeTopicMessage].client, engineExecutorActor)
	//						    strategies += (msg.client -> engineExecutorActor)
							    engineExecutorActor ! x
						    }
						    case _ =>
			          }
			        }
			        case x: ResponseStrategyServicesTopicMessage => {
			          x.asInstanceOf[ResponseStrategyServicesTopicMessage].msgType match {
						    case STRATEGY_RESPONSE_MESSAGE_TYPE => {
						    	logger.debug("Got STRATEGY_RESPONSE_MESSAGE_TYPE. Key {}", x.asInstanceOf[ResponseStrategyServicesTopicMessage].client)	          
							    val engineExecutorActor = context.actorOf(EngineExecutorActor.props(serviceUniqueID))
							    logger.debug("Starting EngineExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseStrategyServicesTopicMessage].client, engineExecutorActor)
	//						    strategies += (msg.client -> engineExecutorActor)
							    engineExecutorActor ! x
						    }
						    case _ =>
			          }
			        }
			        case _ =>
				}
	}

  	
//	val feedServicesDuct: Duct[TttsEngineMessage, Unit] =  
//	val engineServicesDuct: Duct[TttsEngineMessage, Unit] =  
  	
  	
	override def startFlow() = Flow(engineServicesConsumer) append servicesMessageDuct map {
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