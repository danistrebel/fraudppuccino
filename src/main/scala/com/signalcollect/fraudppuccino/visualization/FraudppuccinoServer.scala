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

object FraudppuchinoServer extends App {

  /**
   * Static file handling
   */

  val actorConfig = """
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

  val actorSystem = ActorSystem("FileUploadExampleActorSystem", ConfigFactory.parseString(actorConfig))

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
  webServer.start()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run { webServer.stop() }
  })

  
  def sendResults(components: Map[Int, Iterable[RepeatedAnalysisVertex[_]]]) {
    webSocketBroadcaster ! WebSocketBroadcastText("updateReports")
  }

  System.out.println("Open your browser and navigate to http://localhost:8888")

}