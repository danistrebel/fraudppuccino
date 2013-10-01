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

  
  def sendResults(jsonData: String) {
    webSocketBroadcaster ! WebSocketBroadcastText(jsonData)
  }
  
  for(i <- 0 to 10) {
    readLine
    var value = """{
"components":[
{
	"start":1380470421000,
	"flow":300,
	"depth":3,
	"members":[{"id":1,"value":300.00,"time":1328530643,"successor":[1]},
	{"id":2,"value":300.00,"time":1328530643,"successor":[2,3]},
	{"id":3,"value":100.00,"time":1328530643,"successor":[]},
	{"id":4,"value":100.00,"time":1328530643,"successor":[]}]	
},
{
	"start":1380230421000,
	"flow":8000"""+i+""",
	"depth":5,
	"members":[{"id":1,"value":8000.00,"time":1328530643,"successor":[1]},
	{"id":2,"value":8000.00,"time":1328530643,"successor":[2]},
	{"id":3,"value":8000.00,"time":1328530643,"successor":[3]},
	{"id":4,"value":8000.00,"time":1328530643,"successor":[4]},	
	{"id":5,"value":8000.00,"time":1328530643,"successor":[]}]	
	
}
]
}""".replaceAll("\n", "").replaceAll("\r", "")
	sendResults(value)
	println("sent")
  }

  System.out.println("Open your browser and navigate to http://localhost:8888")

}