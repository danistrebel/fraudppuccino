package com.signalcollect.fraudppuccino.componentdetection

import akka.actor.Actor
import com.signalcollect.configuration.ActorSystemRegistry
import akka.actor.Props

class ComponentHandler extends Actor {
    
  def receive = {
    case msg => println(msg)
  }
}