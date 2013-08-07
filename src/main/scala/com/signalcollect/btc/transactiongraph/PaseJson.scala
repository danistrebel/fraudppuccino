package com.signalcollect.btc.transactiongraph

import scala.util.parsing.json.JSON
import scala.io.Source
import scala.language.dynamics
import java.io.File


object PaseJson extends App {
  val file = new File("/Users/strebel/Google Drive/Uni/Masterthesis/bitcoin")
  val inputs = file.listFiles().filter(_.getName().endsWith(".json"))
  
  for (input <- inputs) {
  val path = input.getAbsolutePath()
  println(path)
  val data = JsonElement.parse(Source.fromFile(path) mkString).get
  val transaction = data.tx.foreach(t => {
    print(data.time.asString + ";")
    print(t.hash.asString + ";")
    for (transactionInput <- t.in) {
      print(transactionInput)
    }
  })
  
  }
  
}

/**
 * Parser adapted from https://gist.github.com/piotrga/5233317
 */
trait JsonElement extends Dynamic{ self =>
  def selectDynamic(field: String) : JsonElement = EmptyElement
  def applyDynamic(field: String)(i: Int) : JsonElement = EmptyElement
  def toList : List[JsonElement] = sys.error(s"$this is not a list.")
  def asString: String = sys.error(s"$this has no string representation.")
  def length$ : Int = sys.error(s"$this has no length")
}

object JsonElement{
 
  def ^(s: String) = {
    require(!s.isEmpty, "Element is empty")
    s
  }
 
  implicit def toString(e: JsonElement) : String = e.asString
  implicit def toBoolean(e: JsonElement) : Boolean = (^(e.asString)).toBoolean
  implicit def toBigDecimal(e: JsonElement) : BigDecimal = BigDecimal(^(e.asString))
  implicit def toDouble(e: JsonElement) : Double = ^(e.asString).toDouble
  implicit def toFloat(e: JsonElement) : Float = ^(e.asString).toFloat
  implicit def toByte(e: JsonElement) : Byte = ^(e.asString).stripSuffix(".0").toByte
  implicit def toShort(e: JsonElement) : Short = ^(e.asString).stripSuffix(".0").toShort
  implicit def toInt(e: JsonElement) : Int = ^(e.asString).stripSuffix(".0").toInt
  implicit def toLong(e: JsonElement) : Long = ^(e.asString).stripSuffix(".0").toLong
  implicit def toList(e: JsonElement) : List[JsonElement] = e.toList
 
 
 
  def parse(json: String) = JSON.parseFull(json) map (JsonElement(_))
 
  def apply(any : Any) : JsonElement = any match {
    case x : Seq[Any] => new ArrayElement(x)
    case x : Map[String, Any] => new ComplexElement(x)
    case x => new PrimitiveElement(x)
  }
}
 
case class PrimitiveElement(x: Any) extends JsonElement{
  override def asString = x.toString
}
 
case object EmptyElement extends JsonElement{
  override def asString = ""
  override def toList = Nil
}
 
case class ArrayElement(private val x: Seq[Any]) extends JsonElement{
  private lazy val elements = x.map((JsonElement(_))).toArray
 
  override def applyDynamic(field: String)(i: Int) : JsonElement = elements.lift(i).getOrElse(EmptyElement)
  override def toList : List[JsonElement] = elements toList
  override def length$ : Int = elements.length
}
 
case class ComplexElement(private val fields : Map[String, Any]) extends JsonElement{
  override def selectDynamic(field: String) : JsonElement = fields.get(field) map(JsonElement(_)) getOrElse(EmptyElement)
}

