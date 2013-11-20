package com.signalcollect.fraudppuccino.resulthandling

import org.mashupbots.socko.routes._
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.webserver._
import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config._
import akka.actor._
import org.mashupbots.socko.handlers._
import akka.routing.FromConfig
import java.io.File
import org.mashupbots.socko.events.WebSocketHandshakeEvent
import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import com.signalcollect.fraudppuccino.structuredetection.DownstreamTransactionPatternEdge
import com.signalcollect.fraudppuccino.componentanalysis.ComponentHandler
import sys.process._
import scala.collection.mutable.ArrayBuffer

/**
 * Visualization component to visually represent findings within the graph.
 *
 * The Component is responsible for serving the client side libraries in a static file server
 * and broadcast new reports to all interested clients through a websocket connection.
 *
 */
case object FraudppuccinoServer extends ComponentResultHandler {

  /*
   * Web Server Configuration
   * 
   * Uses a Socko Server for lightweight file serving and handling websocket connections to the clients
   */
  val visualizationActorConfig = """
           
      my-pinned-dispatcher {
        type=PinnedDispatcher
        executor=thread-pool-executor
      }
      akka {
        event-handlers = ["akka.event.slf4j.Slf4jEventHandler"]
        loglevel=DEBUG
        actor {
          deployment {
            /static-file-router {
              router = round-robin
              nr-of-instances = 5
            }
          }
        }
      }"""

  val actorSystem = ActorSystem("VisualizationActorSystem", ConfigFactory.parseString(visualizationActorConfig))

  val webSocketBroadcaster = actorSystem.actorOf(Props[WebSocketBroadcaster], "webSocketBroadcaster")

  val routes = Routes({
    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      //Add the client to the list of broadcast subscribers
      case Path("/websocket/") => {
        wsHandshake.authorize(onComplete = Some((event: WebSocketHandshakeEvent) => {
          webSocketBroadcaster ! new WebSocketBroadcasterRegistration(event)
        }))
      }
    }

    case WebSocketFrame(wsFrame) => {
      // Process websocket frames sent from the client
      val webSocketHandler = actorSystem.actorOf(Props[WebSocketHandler])
      webSocketHandler ! wsFrame
    }

    case _ =>

  })

  val webServer = new WebServer(WebServerConfig(hostname = "0.0.0.0"), routes, actorSystem)
  webServer.start()

  
  val previouslySentReports = ArrayBuffer[String]()

  
  /**
   * Broadcasts the result along all registered web sockets
   */
  def processResult(jsonData: String) {
    previouslySentReports += jsonData
    webSocketBroadcaster ! WebSocketBroadcastText(jsonData)
  }

  /**
   * Broadcasts the computation state along all registered web sockets
   */
  override def processStatusMessage(jsonStatus: String) = {
    webSocketBroadcaster ! WebSocketBroadcastText(jsonStatus)
  }

}