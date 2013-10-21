package com.signalcollect.fraudppuccino.visualization

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
import com.signalcollect.fraudppuccino.componentdetection.ComponentHandler
import com.signalcollect.fraudppuccino.componentdetection.ComponentResultHandler

/**
 * Visualization component to visually represent findings within the graph.
 *
 * The Component is responsible for serving the client side libraries in a static file server
 * and broadcast new reports to all interested clients through a websocket connection.
 *
 */
case class FraudppuccinoServer(componentHandler: ComponentHandler = null) extends ComponentResultHandler {
  
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
      my-static-content-handler {
		    root-file-paths="/Users/strebel/Documents/workspace/PatternDetective/visualizer"
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

  object MyStaticHandlerConfig extends ExtensionId[StaticContentHandlerConfig] with ExtensionIdProvider {
    override def lookup = MyStaticHandlerConfig
    override def createExtension(system: ExtendedActorSystem) =
      new StaticContentHandlerConfig(system.settings.config, "my-static-content-handler")
  }

  val actorSystem = ActorSystem("VisualizationActorSystem", ConfigFactory.parseString(visualizationActorConfig))

  val handlerConfig = MyStaticHandlerConfig(actorSystem)

  val webSocketBroadcaster = actorSystem.actorOf(Props[WebSocketBroadcaster], "webSocketBroadcaster")

  val staticContentHandlerRouter = actorSystem.actorOf(Props(new StaticContentHandler(handlerConfig))
    .withRouter(FromConfig()).withDispatcher("my-pinned-dispatcher"), "static-file-router")

  val routes = Routes({
    case HttpRequest(request) => request match {
      case GET(Path(file)) => {
        staticContentHandlerRouter ! new StaticFileRequest(request, new File("/Users/strebel/Documents/workspace/PatternDetective/visualizer" + file))
      }
    }
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
      actorSystem.actorOf(Props[WebSocketHandler]) ! wsFrame
    }

  })

  val webServer = new WebServer(WebServerConfig(), routes, actorSystem)

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run { webServer.stop() }
  })

  webServer.start()
  System.out.println("Open your browser and navigate to http://localhost:8888")
  
  /**
   * Broadcasts the result along all registered websockets
   */ 
  def processResult(jsonData: String) {
    webSocketBroadcaster ! WebSocketBroadcastText(jsonData)
  }

}