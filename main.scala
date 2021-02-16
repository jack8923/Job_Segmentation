package Job_Segmentation

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import Job_Segmentation.ServiceApi._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.io.StdIn

object WebServer extends App {

  implicit val system: ActorSystem = ActorSystem("web-app")
  private implicit val materialize: ActorMaterializer = ActorMaterializer()
  private implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  val apiRoutes = {
    {
      routes
    }
  }

  val port = 8080
  val serverFuture = Http().bindAndHandle(apiRoutes, "localhost", port)
  println(s"Server is online at port = $port, PRESS ENTER TO EXIT")
  StdIn.readLine()
  serverFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}