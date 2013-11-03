package com.signalcollect.fraudppuccino.resulthandling

import com.mongodb.casbah.MongoClient
import java.util.Date
import java.text.SimpleDateFormat
import com.mongodb.DBObject

/**
 * Stores the received reports in a local MongoDB instance
 * Assumes that MongoDB is already installed on this machine.
 * 
 * The reported components can later be found in the 'fraudppuccino'
 * database in in the collection 'reports<DATETIME OF EXECUTION>'
 */ 
object MongoDBResultHandler extends ComponentResultHandler {

  val mongoClient = MongoClient()
  val collection = mongoClient("fraudppuccino")("results" + getTimeStamp)

  def processResult(jsonData: String): Unit = {
    collection.save(com.mongodb.util.JSON.parse(jsonData).asInstanceOf[DBObject])
  }

  def getTimeStamp: String = {
    val dateFormat = new SimpleDateFormat("yyyyMMddHHmmss")
    val now = new Date
    dateFormat.format(now)
  }
}