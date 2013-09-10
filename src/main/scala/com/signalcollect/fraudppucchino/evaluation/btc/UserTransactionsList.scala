package com.signalcollect.fraudppucchino.evaluation.btc

import scala.io.Source
import java.io.ObjectInputStream
import java.util.zip.GZIPInputStream
import java.io.FileInputStream
import com.signalcollect.GraphBuilder
import java.io.BufferedReader
import java.io.FileReader
import scala.reflect.io.Path

object UserTransactionsList extends App {
  val pathTransactionIns = "/Volumes/Data/BTC_August2013/tx-user.csv"
  val pathTransactionOuts = "/Volumes/Data/BTC_August2013/txout-user.csv"
  val pathBTCUserTransactions = "/Volumes/Data/BTC_August2013/user-user-tx.csv"

  val transactionInsReader = new BufferedReader(new FileReader(pathTransactionIns))
  val transactionOutsReader = new BufferedReader(new FileReader(pathTransactionOuts))
  val userTransactionsWriter = Path(pathBTCUserTransactions).toFile.writer

  val graph = GraphBuilder.build

  var currentTransactionIn = parseNext(transactionInsReader.readLine)
  var currentTransactionOut = parseNext(transactionOutsReader.readLine)

  var transactionID = 0

  while (currentTransactionIn != null) {
    if (currentTransactionIn._1 % 250000 == 0) {
      println("loaded " + currentTransactionIn._1)
    }
    while (currentTransactionOut != null && currentTransactionOut._1 == currentTransactionIn._1) {
      userTransactionsWriter.write(transactionID + ",") //Unique Transaction Identifier
      userTransactionsWriter.write(currentTransactionIn._1 + ",") // BTC transaction ID
      userTransactionsWriter.write(currentTransactionIn._2 + ",") // Input address ID
      userTransactionsWriter.write(currentTransactionOut._2 + ",") // Output address ID
      userTransactionsWriter.write(currentTransactionOut._3 + ",") // Value
      userTransactionsWriter.write(currentTransactionIn._3 + "\n") // Time

      transactionID += 1
      currentTransactionOut = parseNext(transactionOutsReader.readLine)
    }
    currentTransactionIn = parseNext(transactionInsReader.readLine)

  }

  transactionInsReader.close
  transactionOutsReader.close
  userTransactionsWriter.close

  println("done")

  def parseNext(line: String): (Int, Int, Long) = {
    if (line == null) {
      return null
    }
    val splitted = line.split(",")
    (splitted(0).toInt, splitted(1).toInt, splitted(3).toLong)
  }
}