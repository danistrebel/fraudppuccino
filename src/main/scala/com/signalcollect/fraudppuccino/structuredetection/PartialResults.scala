package com.signalcollect.fraudppuccino.structuredetection

import scala.collection.mutable.ArrayBuffer

case class PartialInput(members: Array[Int], sum: Long, latestTime: Long) {
  val extensions = ArrayBuffer[PartialInput]()
  def extend(newInput: TransactionInput): Unit = {
    extensions.foreach(_.extend(newInput))
    extensions += (this.copy(members = members :+ newInput.transactionID, sum = sum + newInput.value, latestTime=Math.max(latestTime, newInput.time)))
  }
}

case class PartialOutput(members: Array[Int], sum: Long, earliestTime: Long) {
  val extensions = ArrayBuffer[PartialOutput]()

  def extend(newOutput: TransactionOutput, testForOutput: PartialOutput => Iterable[PartialInput]): Unit = {
    val newExtension = this.copy(members = members :+ newOutput.transactionID, sum = sum + newOutput.value, earliestTime = Math.min(earliestTime, newOutput.time))
    testForOutput(newExtension) match {
      case Nil => extensions.foreach(_.extend(newOutput))
      case _ => //extensions.foreach(_.extend(newOutput, testForOutput))
    }
    extensions += (newExtension)
  }

  def extend(newOutput: TransactionOutput): Unit = {
    val newExtension = this.copy(members = members :+ newOutput.transactionID, sum = sum + newOutput.value, earliestTime = Math.min(earliestTime, newOutput.time))
    extensions.foreach(_.extend(newOutput))
    extensions += (newExtension)
  }
}