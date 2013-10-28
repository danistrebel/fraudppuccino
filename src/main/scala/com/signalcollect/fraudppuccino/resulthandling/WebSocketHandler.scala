package com.signalcollect.fraudppuccino.resulthandling

import org.mashupbots.socko.events.HttpRequestEvent

import org.mashupbots.socko.events.WebSocketFrameEvent

import akka.actor.Actor
import akka.event.Logging
import com.signalcollect.fraudppuccino.querylanguage.FRAUDPPUCCINO._


class WebSocketHandler extends Actor {

  def receive = {
    case event: HttpRequestEvent =>
      // Return the HTML page to setup web sockets in the browser
      println("HttpRequestEvent " + event)
    case event: WebSocketFrameEvent => println(event.readText);execute(event.readText)
      
    case _ => {
      println("received unknown message of type: ")
    }
    context.stop(self)

  }
}