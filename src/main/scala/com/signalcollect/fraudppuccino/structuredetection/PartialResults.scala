package com.signalcollect.fraudppuccino.structuredetection

import scala.collection.mutable.ArrayBuffer

case class PartialInput(members: Array[TransactionInput], sum: Long) {
  val extensions = ArrayBuffer[PartialInput]()
  def extend(newInput: TransactionInput): Unit = {
    extensions.foreach(_.extend(newInput))
    extensions += (this.copy(members = members :+ newInput, sum = sum + newInput.value))
  }
  def earliestTime = members.head.time
  def latestTime = members.last.time
}

case class PartialOutput(members: Array[TransactionOutput], sum: Long) {
  val extensions = ArrayBuffer[PartialOutput]()

  def extend(newOutput: TransactionOutput, testForOutput: PartialOutput => Iterable[TransactionInput]): Unit = {
    val newExtension = this.copy(members = members :+ newOutput, sum = sum + newOutput.value)
    testForOutput(newExtension) match {
      case Nil => extensions.foreach(_.extend(newOutput))
      case _ => extensions.foreach(_.extend(newOutput, testForOutput))
    }
    extensions += (newExtension)
  }

  def extend(newOutput: TransactionOutput): Unit = {
    val newExtension = this.copy(members = members :+ newOutput, sum = sum + newOutput.value)
    extensions.foreach(_.extend(newOutput))
    extensions += (newExtension)
  }
  def earliestTime = members.head.time
  def latestTime = members.last.time
}