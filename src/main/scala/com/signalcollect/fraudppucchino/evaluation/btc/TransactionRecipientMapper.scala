package com.signalcollect.fraudppucchino.evaluation.btc

import scala.collection.mutable.HashMap
import scala.io.Source
import java.io.ObjectOutputStream
import java.util.zip.GZIPOutputStream
import java.io.FileOutputStream
import scala.reflect.io.Path

object TransactionRecipientMapper extends App {
  val addressToUserMapper = HashMap[Int, Int]()

  for (line <- Source.fromFile("/Volumes/Data/BTC_August2013/address-user.csv").getLines) {
    val splitted = line.split(",")
    val address = splitted(0).toInt
    val user = splitted(1).toInt
    addressToUserMapper += ((address, user))
  }

  println("loaded map of size " + addressToUserMapper.size)

  val userTransactions = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream("txout-user.gz")))
  val userTransactionsPlainText = Path("txout-user.csv").toFile.writer
  val unparsableReceivers = Path("unparsableOuts.csv").toFile.writer
  var sumUnparsable = 0l

  println("started writing user file")

  for (line <- Source.fromFile("/Volumes/Data/BTC_August2013/txout.csv").getLines) {
    val splitted = line.split(",")
    val tx = splitted(0).toInt
    val value = splitted(3).toLong
    try {
      val outAddress = splitted(1).toInt
      val userId = addressToUserMapper.get(outAddress).getOrElse(outAddress)
      val hashCode = splitted(2).toString().substring(1, splitted(2).length - 1)
      userTransactions.writeInt(tx)
      userTransactions.writeInt(userId)
      userTransactions.writeUTF(hashCode)
      userTransactions.writeLong(value)
      userTransactionsPlainText.write(tx + "," + userId + "," + hashCode + "," + value + "\n")
    } catch {
      case exception: Throwable => unparsableReceivers.write(tx + "," + value + "\n"); sumUnparsable += value
    }
  }

  userTransactions.close
  userTransactionsPlainText.close
  unparsableReceivers.close

  println("Done")
  println("sum of unparsable outputs" + sumUnparsable)

}