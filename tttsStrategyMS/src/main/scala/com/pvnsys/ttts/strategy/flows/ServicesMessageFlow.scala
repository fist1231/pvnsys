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
import com.pvnsys.ttts.strategy.generator.StrategyExecutorActor
import com.pvnsys.ttts.strategy.messages.TttsStrategyMessages
import com.pvnsys.ttts.strategy.impl.AbxStrategyImpl
import com.pvnsys.ttts.strategy.mq.{KafkaServicesTopicProducerActor, KafkaFacadeTopicProducerActor}
import com.pvnsys.ttts.strategy.mq.StrategyActor


object ServicesMessageFlow extends LazyLogging {

  import TttsStrategyMessages._
  type servicesMessageFlowOutDuctType = (String, Producer[TttsStrategyMessage])
  
  def apply(context: ActorContext, serviceUniqueID: String): Duct[TttsStrategyMessage, servicesMessageFlowOutDuctType] = Duct[TttsStrategyMessage].
	    // acknowledge and pass on
	    map { msg =>
	      val messageType = msg match {
	        case x: RequestStrategyServicesTopicMessage => x.asInstanceOf[RequestStrategyServicesTopicMessage].msgType 
	        case x: ResponseFeedFacadeTopicMessage => x.asInstanceOf[ResponseFeedFacadeTopicMessage].msgType 
	        case x: ResponseFeedServicesTopicMessage => x.asInstanceOf[ResponseFeedServicesTopicMessage].msgType 
	        case _ => "UNKNOWN"
	      }
	      logger.info("*******>> Step 0: StrategyMS ServicesMessageFlow Initialized. Received Message Type is: {}", messageType)
	      msg
	    }.
	    
	    map { msg =>
	      	  
	          logger.info("*******>> Step 1: Strategy ServicesMessageFlow Creating schema for first Feed Response message {}", msg)
		      msg match {
		        case x: RequestStrategyServicesTopicMessage => // Nothing for Strategy messages 
		        case x: ResponseFeedFacadeTopicMessage => {
		          x.asInstanceOf[ResponseFeedFacadeTopicMessage].sequenceNum match {
		            case "1" => new AbxStrategyImpl(context).createSchema(serviceUniqueID, msg)
		            case _ => // Do nothing
		          }
 
		        }
		        case x: ResponseFeedServicesTopicMessage => {
		          x.asInstanceOf[ResponseFeedServicesTopicMessage].sequenceNum match {
		            case "1" => new AbxStrategyImpl(context).createSchema(serviceUniqueID, msg)
		            case _ => // Do nothing
		          }
 
		        }
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.info("*******>> Step 2: Strategy ServicesMessageFlow Write quotes feed data to db {}", msg)
		      msg match {
		        case x: RequestStrategyServicesTopicMessage => // Nothing for Strategy messages 
		        case x: ResponseFeedFacadeTopicMessage => {
		            new AbxStrategyImpl(context).writeQuotesData(serviceUniqueID, msg)
		        }
		        case x: ResponseFeedServicesTopicMessage => {
		            new AbxStrategyImpl(context).writeQuotesData(serviceUniqueID, msg)
		        }
		        case _ => "UNKNOWN"
		      }
	          msg
	          
	    }.

	    map { msg =>
	      	  
	          logger.info("*******>> Step 3: Strategy ServicesMessageFlow Apply strategy logic to the quotes feed {}", msg)
		      val outp = msg match {
		        case x: RequestStrategyServicesTopicMessage => msg
		        case x: ResponseFeedFacadeTopicMessage => {
		            new AbxStrategyImpl(context).applyStrategy(serviceUniqueID, msg)
		        }
		        case x: ResponseFeedServicesTopicMessage => {
		            new AbxStrategyImpl(context).applyStrategy(serviceUniqueID, msg)
		        }
		        case _ => msg
		      }
	          outp
	    }.

	    map {
	        logger.info("*******>> Step 4: Strategy ServicesMessageFlow Convert Service messages")
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
	      case msg: ResponseStrategyFacadeTopicMessage => {
	        logger.info("*******>> Step 5: Strategy ServicesMessageFlow Groupby operation on ResponseStrategyFacadeTopicMessage")
	        "StrategyFacadeResponse"
	      }
	      case msg: ResponseStrategyServicesTopicMessage => {
	        logger.info("*******>> Step 5: Strategy ServicesMessageFlow Groupby operation on ResponseStrategyServicesTopicMessage")
	        "StrategyServicesResponse"
	      }
	      case _ => "Garbage"
	    }
  
}


class ServicesMessageFlow(strategyServicesActor: ActorRef, serviceUniqueID: String)(implicit context: ActorContext) extends StrategyServiceFlow with LazyLogging {
  
	import ServicesMessageFlow._
	import TttsStrategyMessages._
	import StrategyExecutorActor._
	import StrategyActor._

	implicit val executor = context.dispatcher
    val materializer = FlowMaterializer(MaterializerSettings())
	
	
    val strategyServicesConsumer = ActorProducer(strategyServicesActor)
//    val strategyImpl = new AbxStrategyImpl(context)
  	val servicesMessageDuct = ServicesMessageFlow(context, serviceUniqueID)
  	
	val executors = mutable.Map[String, ActorRef]()
	val strategies = mutable.Map[String, ActorRef]()
	val producers = mutable.Map[String, ActorRef]()
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
							    executors += (x.asInstanceOf[RequestStrategyServicesTopicMessage].client -> strategyExecutorActor)
							    
//							    val strategyActor = context.actorOf(Props(classOf[AbxStrategyActor]))
//							    strategies += (x.asInstanceOf[RequestStrategyServicesTopicMessage].client -> strategyActor)
							    
							    val kafkaServicesTopicProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
							    producers += (x.asInstanceOf[RequestStrategyServicesTopicMessage].client -> kafkaServicesTopicProducerActor)
							    
							    
							    strategyExecutorActor ! x
						    }
						    case STRATEGY_STOP_REQUEST_MESSAGE_TYPE => {
						    	logger.debug("Got STRATEGY_STOP_REQUEST_MESSAGE_TYPE. Key {}", x.asInstanceOf[RequestStrategyServicesTopicMessage].client)	
	//					    	feeds.foreach { case (key, value) => logger.debug(" key: {} ==> value: {}", key, value) }
						    	
						        executors.get(x.asInstanceOf[RequestStrategyServicesTopicMessage].client) match {
								  case Some(strategyExecActor) => {
								    logger.debug("Stopping StrategyExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[RequestStrategyServicesTopicMessage].client, strategyExecActor)
								    strategyExecActor ! x
//								    strategyExecActor ! StopStrategyExecutorMessage
								    executors -= x.asInstanceOf[RequestStrategyServicesTopicMessage].client 
//								    strategies -= x.asInstanceOf[RequestStrategyServicesTopicMessage].client 
								    producers -= x.asInstanceOf[RequestStrategyServicesTopicMessage].client 
								  }
								  case None => logger.debug("No such Strategy to stop. Key {}", x.asInstanceOf[RequestStrategyServicesTopicMessage].client)
								}
						    }
						    case _ =>
					  } 
					}
			        case x: ResponseStrategyFacadeTopicMessage => {
				        val producerActor = producers.get(x.asInstanceOf[ResponseStrategyFacadeTopicMessage].client) match {
						  case Some(existingProducerActor) => {
							logger.info("~~~~~~~~ Got existingProducerActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseStrategyFacadeTopicMessage].client, existingProducerActor)
							existingProducerActor
						  }
						  case None => {
						    logger.info("~~~~~~~~~ Not found producer, creating a new one. Key {}", x.asInstanceOf[ResponseStrategyFacadeTopicMessage].client)
						    val newProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
						    producers += (x.asInstanceOf[ResponseStrategyFacadeTopicMessage].client -> newProducerActor)
						    newProducerActor
						  }
						}
			          
//			           val newProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
			           producerActor ! x
//			           producerActor ! StopMessage
			        }
			        case x: ResponseStrategyServicesTopicMessage => {
			          
				        val producerActor = producers.get(x.asInstanceOf[ResponseStrategyServicesTopicMessage].client) match {
						  case Some(existingProducerActor) => {
							logger.debug("~~~~~~~~ Got existingProducerActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseStrategyServicesTopicMessage].client, existingProducerActor)
							existingProducerActor
						  }
						  case None => {
						    logger.debug("~~~~~~~~ Not found producerctor, creating a new one. Key {}", x.asInstanceOf[ResponseStrategyServicesTopicMessage].client)
						    val newProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
						    producers += (x.asInstanceOf[ResponseStrategyServicesTopicMessage].client -> newProducerActor)
						    newProducerActor
						  }
						}
			          
//			           val newProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
			           producerActor ! x
//			           producerActor ! StopMessage
			        }
//			        case x: ResponseFeedFacadeTopicMessage => {
//			          x.asInstanceOf[ResponseFeedFacadeTopicMessage].msgType match {
//						    case FEED_RESPONSE_MESSAGE_TYPE => {
//						    	logger.debug("Got FEED_RESPONSE_MESSAGE_TYPE. Key {}", x.asInstanceOf[ResponseFeedFacadeTopicMessage].client)	          
////							    val strategyExecutorActor = context.actorOf(StrategyExecutorActor.props(serviceUniqueID))
//						        val strategyActor = strategies.get(x.asInstanceOf[ResponseFeedFacadeTopicMessage].client) match {
//								  case Some(existingStrategyActor) => {
//									logger.debug("Got strategycActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseFeedFacadeTopicMessage].client, existingStrategyActor)
//									existingStrategyActor
//								  }
//								  case None => {
//								    logger.debug("Not found strategycActor, creating a new one. Key {}", x.asInstanceOf[ResponseFeedFacadeTopicMessage].client)
//								    val newStrategyActor = context.actorOf(Props(classOf[AbxStrategyActor]))
//								    strategies += (x.asInstanceOf[ResponseFeedFacadeTopicMessage].client -> newStrategyActor)
//								    newStrategyActor
//								  }
//								}
//						        val producerActor = producers.get(x.asInstanceOf[ResponseFeedFacadeTopicMessage].client) match {
//								  case Some(existingProducerActor) => {
//									logger.debug("Got existingProducerActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseFeedFacadeTopicMessage].client, existingProducerActor)
//									existingProducerActor
//								  }
//								  case None => {
//								    logger.debug("Not found producer, creating a new one. Key {}", x.asInstanceOf[ResponseFeedFacadeTopicMessage].client)
//								    val newProducerActor = context.actorOf(Props(classOf[KafkaFacadeTopicProducerActor]))
//								    producers += (x.asInstanceOf[ResponseFeedFacadeTopicMessage].client -> newProducerActor)
//								    newProducerActor
//								  }
//								}
//
//						    	
//						    	executors.get(x.asInstanceOf[ResponseFeedFacadeTopicMessage].client) match {
//								  case Some(strategyExecActor) => {
//									logger.debug("Starting StrategyExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseFeedFacadeTopicMessage].client, strategyExecActor)
//								    strategyExecActor ! StartStrategyFeedMessage(x, strategyActor, producerActor)
//								  }
//								  case None => {
//								    logger.debug("Not found executor for ResponseFeedFacadeTopicMessage, creating a new one. Key {}", x.asInstanceOf[ResponseFeedFacadeTopicMessage].client)
//								    val newStrategyExecutorActor = context.actorOf(StrategyExecutorActor.props(serviceUniqueID))
//								    executors += (x.asInstanceOf[ResponseFeedFacadeTopicMessage].client -> newStrategyExecutorActor)
//								    newStrategyExecutorActor ! StartStrategyFeedMessage(x, strategyActor, producerActor)
//								  }
//								}
////							    logger.debug("Starting StrategyExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseFeedFacadeTopicMessage].client, strategyExecutorActor)
////	//						    executors += (msg.client -> strategyExecutorActor)
////							    strategyExecutorActor ! x
//						    }
//						    case _ =>
//			          }
//			        }
//			        case x: ResponseFeedServicesTopicMessage => {
//			          x.asInstanceOf[ResponseFeedServicesTopicMessage].msgType match {
//						    case FEED_RESPONSE_MESSAGE_TYPE => {
//						    	logger.debug("Got FEED_RESPONSE_MESSAGE_TYPE. Key {}", x.asInstanceOf[ResponseFeedServicesTopicMessage].client)	          
////							    val strategyExecutorActor = context.actorOf(StrategyExecutorActor.props(serviceUniqueID))
//
//						        val strategyActor = strategies.get(x.asInstanceOf[ResponseFeedServicesTopicMessage].client) match {
//								  case Some(existingStrategyActor) => {
//									logger.debug("Got strategycActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseFeedServicesTopicMessage].client, existingStrategyActor)
//									existingStrategyActor
//								  }
//								  case None => {
//								    logger.debug("Not found strategycActor, creating a new one. Key {}", x.asInstanceOf[ResponseFeedServicesTopicMessage].client)
//								    val newStrategyActor = context.actorOf(Props(classOf[AbxStrategyActor]))
//								    strategies += (x.asInstanceOf[ResponseFeedServicesTopicMessage].client -> newStrategyActor)
//								    newStrategyActor
//								  }
//								}
//						        val producerActor = producers.get(x.asInstanceOf[ResponseFeedServicesTopicMessage].client) match {
//								  case Some(existingProducerActor) => {
//									logger.debug("Got existingProducerActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseFeedServicesTopicMessage].client, existingProducerActor)
//									existingProducerActor
//								  }
//								  case None => {
//								    logger.debug("Not found producerctor, creating a new one. Key {}", x.asInstanceOf[ResponseFeedServicesTopicMessage].client)
//								    val newProducerActor = context.actorOf(Props(classOf[KafkaServicesTopicProducerActor]))
//								    producers += (x.asInstanceOf[ResponseFeedServicesTopicMessage].client -> newProducerActor)
//								    newProducerActor
//								  }
//								}
//						    	
//						    	
//						    	executors.get(x.asInstanceOf[ResponseFeedServicesTopicMessage].client) match {
//								  case Some(strategyExecActor) => {
//									logger.debug("Starting StrategyExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseFeedServicesTopicMessage].client, strategyExecActor)
//								    strategyExecActor ! StartStrategyFeedMessage(x, strategyActor, producerActor)
//								  }
//								  case None => logger.debug("No such Strategy to stop. Key {}", x.asInstanceOf[ResponseFeedServicesTopicMessage].client)
//								}
////							    logger.debug("Starting StrategyExecutorActor. Key {}; ActorRef {}", x.asInstanceOf[ResponseFeedServicesTopicMessage].client, strategyExecutorActor)
////	//						    executors += (msg.client -> strategyExecutorActor)
////							    strategyExecutorActor ! x
//						    }
//						    case _ =>
//			          }
//			        }
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