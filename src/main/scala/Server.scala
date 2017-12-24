import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.{HttpApp, Route}
import spray.json.DefaultJsonProtocol._

import scala.util.Random

object Server extends HttpApp {

  final case class UserSession(id: String, userName: String)

  val rnd = Random
  val sessions = new ConcurrentHashMap[String, UserSession]()

  final case class StatResponse(usersCount: Int, recordsCount: Int)

  implicit val statResponseFormat = jsonFormat2(StatResponse)

  override def routes: Route =
    get {
      path("api" / "stat") {
        complete(StatResponse(Repository.usersCount, Repository.recordsCount))
      }
    } ~ get {
      path("api" / "login") {
        parameters('login.as[String]) { inputLogin =>
          val newSessionId = rnd.nextLong.toString
          sessions.put(newSessionId, UserSession(newSessionId, inputLogin))
          setCookie(HttpCookie("sessionId", newSessionId)) {
            redirect("/page/main.html", StatusCodes.PermanentRedirect)
          }
        }
      }
    } ~ delete {
      path("api" / "logout") {
        cookie("sessionId") { id => {
          sessions.remove(id.value)
          deleteCookie("sessionId") {
            complete("Success logout")
          }
        }
        }
      }
    } ~ get {
      path("api" / "login" / "current") {
        cookie("sessionId") { id => {
          Option(sessions.get(id.value)) match {
            case None => complete("Unknown")
            case Some(session) => complete(session.userName)
          }
        }
        }
      }
    } ~ get {
      pathPrefix("page") {
        getFromDirectory(Paths.get(
          System.getProperty("user.dir"),
          "web"
        ).toString)
      }
    }
}

object ServerStarter extends App {
  Server.startServer(Settings.host, Settings.port)
}