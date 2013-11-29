package com.signalcollect.fraudppuccino.util.btc.datasetpreparation

import scala.io.Source
import java.io.FileWriter

/**
 * Generates Cash and 
 */
object FlagGenerator extends App {
  val inputFilePath = "/Volumes/Data/BTC_August2013/user-user-tx.csv"
  val outputFilePath = "/Volumes/Data/BTC_August2013/bitcoinExtended.csv"
  
  val outputFile = new FileWriter(outputFilePath, true)

  val flags = List(("xCountry",0), ("cash", 1))
  
  Source.fromFile(inputFilePath).getLines.foreach(line => {
    val splits = line.split(",")
    val input = splits(2).toInt
    val output = splits(3).toInt
    val newLine = line + "," + flags.map(flag => if(input%10==(output+flag._2)%10) 1 else 0).mkString(",") + "\n"
    outputFile.write(newLine)
  })
  println("done")
  
  outputFile.close
}