package com.pvnsys.ttts.engine.flows

import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.pvnsys.ttts.engine.generator.EngineService
import org.reactivestreams.api.Producer
import akka.actor.{ActorRef, ActorContext, Props}
import scala.collection.mutable
import scala.collection.mutable.Map
import com.pvnsys.ttts.engine.generator.EngineExecutorActor
import com.pvnsys.ttts.engine.messages.TttsEngineMessages
import com.pvnsys.ttts.engine.impl.SimulatorEngineImpl
import com.pvnsys.ttts.engine.mq.{KafkaServicesTopicProducerActor, KafkaFacadeTopicProducerActor}


object ServicesMessageFlow extends LazyLogging {

  import TttsEngineMessages._
  type servicesMessageFlowOutDuctType = (String, Producer[TttsEngineMessage])
  
  def apply(context: ActorContext, serviceUniqueID: String): Duct[TttsEngineMessage, servicesMessageFlowOutDuctType] = Duct[TttsEngineMessage].
	    // acknowledge and pass on
	    map { msg =>
	      val messageType = msg match {
	        case x: RequestEngineServicesTopicMessage => x.msgType 
	        case x: ResponseStrategyFacadeTopicMessage => x.msgType 
	        case x: ResponseStrategyServicesTopicMessage => x.msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.debug("*******>> Step 0: EngineFMS ServicesMessageFlow Initialized. Received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      	  
	          logger.debug("*******>> Step 1: Engine ServicesMessageFlow Creating schema for first Strategy Response message {}", msg)
		      msg match {
		        case x: RequestEngineServicesTopicMessage => // Nothing for Engine messages 
		        case x: ResponseStrategyFacadeTopicMessage => {
		          x.sequenceNum match {
		            case "1" => new SimulatorEngineImpl(context).createSchema(serviceUniqueID, msg)
		            case _ => // Do nothing
		          }
 
		        }
		        case x: ResponseStrategyServicesTopicMessage => {
		          x.sequenceNum match {
		            case "1" => new SimulatorEngineImpl(context).createSchema(serviceUniqueID, msg)
		            case _ => // Do nothing
		          }
 
		        }
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.debug("*******>> Step 2: Engine ServicesMessageFlow Apply Engine logic to the Strategy feed {}", msg)
		      val outp = msg match {
		        case x: RequestEngineServicesTopicMessage => msg
		        case x: ResponseStrategyFacadeTopicMessage => {
		            new SimulatorEngineImpl(context).applyEngine(serviceUniqueID, msg)
		        }
		        case x: ResponseStrategyServicesTopicMessage => {
		            new SimulatorEngineImpl(context).applyEngine(serviceUniqueID, msg)
		        }
		        case _ => msg
		      }
	          outp
	    }.

	    map {
	        logger.debug("*******>> Step 3: Engine ServicesMessageFlow Convert Service messages")
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
	      case msg: ResponseEngineFacadeTopicMessage => {
	        logger.debug("*******>> Step 5: Engine ServicesMessageFlow Groupby operation on ResponseEngineFacadeTopicMessage")
	        "EngineFacadeResponse"
	      }
	      case msg: ResponseEngineServicesTopicMessage => {
	        logger.debug("*******>> Step 5: Engine ServicesMessageFlow Groupby operation on ResponseEngineServicesTopicMessage")
	        "EngineServicesResponse"
	      }
	      case _ => "Trash"
	    }
  
}


class ServicesMessageFlow(engineServicesActor: ActorRef, serviceUniqueID: String)(implicit context: ActorContext) extends EngineServiceFlow with LazyLogging {
  
	import ServicesMessageFlow._
	import TttsEngineMessages._

	implicit val executor = context.dispatcher
    val materializer = FlowMaterializer(MaterializerSettings())
	
	
    val engineServicesConsumer = ActorProducer(engineServicesActor)
  	val servicesMessageDuct = ServicesMessageFlow(context, serviceUniqueID)
  	
	val executors = mutable.Map[String, ActorRef]()
	val producers = mutable.Map[String, ActorRef]()
	
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
							    executors += (x.asInstanceOf[RequestEngineServicesTopicMessage].client -> engineExecutorActor)
							    
							    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
							    producers += (x.asInstanceOf[RequestEngineServicesTopicMessage].client -> kafkaServicesTopicProducerActor)
							    
							    engineExecutorActor ! x
						    }
						    case ENGINE_STOP_REQUEST_MESSAGE_TYPE => {
						    	logger.debug("Got ENGINE_STOP_REQUEST_MESSAGE_TYPE. Key {}", x.asInstanceOf[RequestEngineServicesTopicMessage].client)	
	//					    	feeds.foreach { case (key, value) => logger.debug(" key: {} ==> value: {}", key, value) }
						    	
						        executors.get(x.asInstanceOf[RequestEngineServicesTopicMessage].client) match {
								  case Some(engineExecActor) => {
								    logger.debug("Stopping EngineExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[RequestEngineServicesTopicMessage].client, engineExecActor)
								    engineExecActor ! x
//								    engineExecActor ! StopEngineExecutorMessage
								    executors -= x.asInstanceOf[RequestEngineServicesTopicMessage].client 
								    producers -= x.asInstanceOf[RequestEngineServicesTopicMessage].client 
								  }
								  case None => logger.debug("No such Engine to stop. Key {}", x.asInstanceOf[RequestEngineServicesTopicMessage].client)
								}
						    }
						    case _ =>
					  } 
					}
			        case x: ResponseEngineFacadeTopicMessage => {
				        val producerActor = producers.get(x.asInstanceOf[ResponseEngineFacadeTopicMessage].client) match {
						  case Some(existingProducerActor) => {
							logger.debug("~~~~~~~~ Got existingProducerActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseEngineFacadeTopicMessage].client, existingProducerActor)
							existingProducerActor
						  }
						  case None => {
						    logger.debug("~~~~~~~~~ Not found producer, creating a new one. Key {}", x.asInstanceOf[ResponseEngineFacadeTopicMessage].client)
						    val newProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
						    producers += (x.asInstanceOf[ResponseEngineFacadeTopicMessage].client -> newProducerActor)
						    newProducerActor
						  }
						}
			           producerActor ! x
			        }
			        case x: ResponseEngineServicesTopicMessage => {
			          
				        val producerActor = producers.get(x.asInstanceOf[ResponseEngineServicesTopicMessage].client) match {
						  case Some(existingProducerActor) => {
							logger.debug("~~~~~~~~ Got existingProducerActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseEngineServicesTopicMessage].client, existingProducerActor)
							existingProducerActor
						  }
						  case None => {
						    logger.debug("~~~~~~~~ Not found producerctor, creating a new one. Key {}", x.asInstanceOf[ResponseEngineServicesTopicMessage].client)
						    val newProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
						    producers += (x.asInstanceOf[ResponseEngineServicesTopicMessage].client -> newProducerActor)
						    newProducerActor
						  }
						}
			           producerActor ! x
			        }
					
//			        case x: ResponseStrategyFacadeTopicMessage => {
//			          x.asInstanceOf[ResponseStrategyFacadeTopicMessage].msgType match {
//						    case STRATEGY_RESPONSE_MESSAGE_TYPE => {
//						    	logger.debug("Got STRATEGY_RESPONSE_MESSAGE_TYPE. Key {}", x.asInstanceOf[ResponseStrategyFacadeTopicMessage].client)	          
//						    	val engineExecutorActor = context.actorOf(EngineExecutorActor.props(serviceUniqueID))
//							    logger.debug("Starting EngineExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseStrategyFacadeTopicMessage].client, engineExecutorActor)
//	//						    strategies += (msg.client -> engineExecutorActor)
//							    engineExecutorActor ! x
//						    }
//						    case _ =>
//			          }
//			        }
//			        case x: ResponseStrategyServicesTopicMessage => {
//			          x.asInstanceOf[ResponseStrategyServicesTopicMessage].msgType match {
//						    case STRATEGY_RESPONSE_MESSAGE_TYPE => {
//						    	logger.debug("Got STRATEGY_RESPONSE_MESSAGE_TYPE. Key {}", x.asInstanceOf[ResponseStrategyServicesTopicMessage].client)	          
//						    	val engineExecutorActor = context.actorOf(EngineExecutorActor.props(serviceUniqueID))
//							    logger.debug("Starting EngineExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseStrategyServicesTopicMessage].client, engineExecutorActor)
//	//						    strategies += (msg.client -> engineExecutorActor)
//							    engineExecutorActor ! x
//						    }
//						    case _ =>
//			          }
//			        }
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