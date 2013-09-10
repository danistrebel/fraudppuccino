package com.signalcollect.fraudppucchino.evaluation.btc

import scala.collection.mutable.HashMap
import scala.io.Source
import java.io.ObjectOutputStream
import java.util.zip.GZIPOutputStream
import java.io.FileOutputStream
import scala.reflect.io.Path

object TransactionInputUserMapper extends App {
  val transactionToUserMapper = HashMap[Int, Int]()

  for (line <- Source.fromFile("/Volumes/Data/BTC_August2013/tx-inuser.csv").getLines) {
    val splitted = line.split(",")
    val txid = splitted(0).toInt * -1
    val user = splitted(1).toInt
    transactionToUserMapper += ((txid, user))
  }

  println("loaded map of size " + transactionToUserMapper.size)

  val userTransactions = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream("tx-user.gz")))
  val userTransactionsPlainText = Path("tx-user.csv").toFile.writer

  println("started writing user file")

  for (line <- Source.fromFile("/Volumes/Data/BTC_August2013/tx.csv").getLines) {
    val splitted = line.split(",")
    val tx = splitted(0).toInt
    val hashCode = splitted(1).toString().substring(1, splitted(1).length - 1)
    val time = splitted(2).toLong
    try {
      val inputUserId = transactionToUserMapper.get(tx).get
      userTransactions.writeInt(tx)
      userTransactions.writeInt(inputUserId)
      userTransactions.writeUTF(hashCode)
      userTransactions.writeLong(time)
      userTransactionsPlainText.write(tx + "," + inputUserId + "," + hashCode + "," + time + "\n")
    } catch {
      case _: Throwable => println(tx + " " + hashCode)
    }
  }

  userTransactions.close
  userTransactionsPlainText.close

  println("Done")

}