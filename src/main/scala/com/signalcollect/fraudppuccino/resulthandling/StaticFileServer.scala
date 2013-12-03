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
import com.signalcollect.fraudppuccino.componentanalysis.ComponentHandler
import sys.process._


object StaticFileServer extends App {

  
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
		    root-file-paths="/Users/strebel/Documents/workspace/PatternDetective/src/main/resources/visualizer/,/home/user/strebel"
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
  
  val actorSystem = ActorSystem("StaticFileServerActorSystem", ConfigFactory.parseString(visualizationActorConfig))
  
  val handlerConfig = MyStaticHandlerConfig(actorSystem)

  val staticContentHandlerRouter = actorSystem.actorOf(Props(new StaticContentHandler(handlerConfig))
    .withRouter(FromConfig()).withDispatcher("my-pinned-dispatcher"), "static-file-router")

  val routes = Routes({
    case HttpRequest(request) => request match {
      case GET(Path(file)) => {
        println(new File("src/main/resources/visualizer/index.html").getAbsolutePath)
        if (file.size <= 1) {
          staticContentHandlerRouter ! new StaticFileRequest(request, new File("src/main/resources/visualizer/index.html"))
        } else {
          staticContentHandlerRouter ! new StaticFileRequest(request, new File("src/main/resources/visualizer" + file))
        }
      }
    }
    
    case _ => 
  })

  val webServer = new WebServer(WebServerConfig(hostname = "0.0.0.0", port = 8880), routes, actorSystem)

  //try to open the default browser showing the evaluation front end
  try {
    "open http://localhost:8880/index.html" !
  } catch {
    case t: Throwable => // Fail silently
  }
  
  webServer.start()
  System.out.println("Open your browser and navigate to http://localhost:8880")
}