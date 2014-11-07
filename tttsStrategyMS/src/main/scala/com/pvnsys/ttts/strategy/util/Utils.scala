package com.pvnsys.ttts.strategy.util


object Utils {
  
  // Unique traits assigned to every incoming and outgoing message: uuid, timestamp, sequence number
  type MessageTraits = (String, String, String)
  
  def generateMessageTraits: MessageTraits = {
    
    val uuid = java.util.UUID.randomUUID.toString
    
    val sdf = new java.text.SimpleDateFormat("ddMMyyyyhhmmssSSS")
    val timestamp = sdf.format(new java.util.Date())
    
    val seqNum = 0
    
    (uuid, timestamp, s"$seqNum")
  }

  
    def generateUuid: String = java.util.UUID.randomUUID.toString

}


