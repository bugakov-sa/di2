package sb.scala.di2

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import scala.io.StdIn

object Conf {
  val port = 8080
  val host = "127.0.0.1"
}

/*
гредл
https://docs.gradle.org/current/userguide/scala_plugin.html

акторы
https://doc.akka.io/docs/akka/2.5.8/guide/tutorial_1.html

сессии
https://softwaremill.com/client-side-sessions-akka-http/

авторизация
https://doc.akka.io/docs/akka-http/current/scala/http/routing-dsl/directives/security-directives/authenticateOrRejectWithChallenge.html

бд
http://scalikejdbc.org
 */
object Application extends App {

  private val log = Logger("Application")

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val route =
    get {
      path("hello") {
        complete("hello")
      }
    }

  val bindingFuture = Http().bindAndHandle(route, Conf.host, Conf.port)

  log.info(s"Server online at http://${Conf.host}:${Conf.port}/hello")
  log.info("Press ENTER to stop...")

  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
