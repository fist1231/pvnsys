package com.pvnsys.ttts.engine.impl

import com.pvnsys.ttts.engine.messages.TttsEngineMessages

object Engine {
  
  object StrategySignal extends Enumeration {
    type StrategySignal = Value
    val Buy, Sell, HoldLong, Short, Cover, HoldShort, SellStop, CoverStop, NotAvailabe = Value
  }

  /*
   * Engine table row Tuple: 
   *   funds - initial balance; 
   *   balance - current balance; 
   *   transnum - number of transactions; 
   *   intrade - is trade in progress; 
   *   possize - trade in progress position size
   *   
   *   engine:([]funds:`float$();balance:`float$();transnum:`long$();intrade:`boolean$();possize:`long$();price:`float$())
   */ 
  type EngineKdbType = (Double, Double, Long, Boolean, Long, Double)
  
  // trade:([]time:`time$();sym:`symbol$();price:`float$();size:`long$();oper:`symbol$();cost:`float$();balance:`float$();transnum:`long$()) 
  type TransactionKdbType = (String, String, Double, Long, String, Double, Double, Long)
  
  implicit def engineSignal2String(i: StrategySignal.Value) = i.toString
}

trait Engine {
  import TttsEngineMessages._

  def applyEngine(serviceId: String, message: TttsEngineMessage): TttsEngineMessage
  
}

