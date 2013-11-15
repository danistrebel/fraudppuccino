package com.signalcollect.fraudppuccino.resulthandling

import org.mashupbots.socko.events.HttpRequestEvent
import org.mashupbots.socko.events.WebSocketFrameEvent
import akka.actor.Actor
import akka.event.Logging
import com.signalcollect.fraudppuccino.querylanguage.FRAUDPPUCCINO._

class WebSocketHandler extends Actor {

  def receive = {
    case event: HttpRequestEvent =>
    case event: WebSocketFrameEvent => {
      val msg = event.readText
      if (msg.equalsIgnoreCase("getpreviousresults")) {
        val previousMessages = "[" + FraudppuccinoServer.previouslySentReports.mkString(",") + "]"
        event.writeText(previousMessages)
      } else {
        execute(msg)
      }
    }

    case _ =>
      {
        println("received unknown message of type: ")
      }
   
      context.stop(self)

  }
}