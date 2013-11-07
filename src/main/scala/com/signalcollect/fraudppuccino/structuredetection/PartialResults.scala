package com.signalcollect.fraudppuccino.structuredetection

import scala.collection.mutable.ArrayBuffer

case class PartialInput(members: Array[TransactionInput], sum: Long) {  
  def extend(newInput: TransactionInput) = this.copy(members = members:+newInput, sum = sum+newInput.value)
  def earliestTime = members.head.time
  def latestTime = members.last.time
}

case class PartialOutput(members: Array[TransactionOutput], sum: Long) {  
  def extend(newOutput: TransactionOutput) = this.copy(members = members:+newOutput, sum = sum+newOutput.value)
  def earliestTime = members.head.time
  def latestTime = members.last.time
}