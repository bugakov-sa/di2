import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import com.softwaremill.session.CsrfDirectives._
import com.softwaremill.session.CsrfOptions._
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import com.softwaremill.session._
import com.typesafe.scalalogging.StrictLogging
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn
import scala.util.Try

case class UserSession(username: String)

object UserSession {
  implicit def serializer: SessionSerializer[UserSession, String] = new SingleValueSessionSerializer(
    _.username,
    (un: String) => Try {
      UserSession(un)
    })
}

object Server extends App with StrictLogging {

  implicit val system = ActorSystem("server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val sessionConfig = SessionConfig.default("c05ll3lesrinf39t7mc5h6un6r0c69lgfno69dsak3vabeqamouq4328cuaekros401ajdpkh60rrtpd8ro24rbuqmgtnd1ebag6ljnb65i8a55d482ok7o0nch0bfbe")
  implicit val sessionManager = new SessionManager[UserSession](sessionConfig)
  implicit val refreshTokenStorage = new InMemoryRefreshTokenStorage[UserSession] {
    def log(msg: String) = logger.info(msg)
  }

  def mySetSession(v: UserSession) = setSession(refreshable, usingCookies, v)

  val myRequiredSession = requiredSession(refreshable, usingCookies)
  val myInvalidateSession = invalidateSession(refreshable, usingCookies)

  final case class StatResponse(usersCount: Int, recordsCount: Int)

  implicit val statResponseFormat = jsonFormat2(StatResponse)

  val routes =
    randomTokenCsrfProtection(checkHeader) {
      get {
        path("") {
          redirect("/page/login.html", Found)
        } ~ pathPrefix("page") {
          getFromDirectory(Paths.get(
            System.getProperty("user.dir"),
            "web"
          ).toString)
        }
      } ~ get {
        path("api" / "stat") {
          complete(StatResponse(Repository.usersCount, Repository.recordsCount))
        }
      } ~ get {
        path("api" / "login") {
          parameters('login.as[String]) { username =>
            mySetSession(UserSession(username)) {
              setNewCsrfToken(checkHeader) { ctx => ctx.redirect("/page/main.html", Found) }
            }
          }
        }
      } ~ get {
        path("api" / "logout") {
          myRequiredSession { session =>
            myInvalidateSession { ctx =>
              logger.info(s"Logging out $session")
              ctx.redirect("/page/login.html", Found)
            }
          }
        }
      } ~ get {
        path("api" / "login" / "current") {
          myRequiredSession { session => ctx =>
            logger.info("Current session: " + session)
            ctx.complete(session.username)
          }
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

  println("Server started, press enter to stop. Visit http://localhost:8080 to see the demo.")
  StdIn.readLine()

  import system.dispatcher

  bindingFuture
    .flatMap(_.unbind())
    .onComplete { _ =>
      system.terminate()
      println("Server stopped")
    }
}