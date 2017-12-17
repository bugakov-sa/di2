import java.nio.file.Paths

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.{HttpApp, Route}
import spray.json.DefaultJsonProtocol._

object Server extends HttpApp {

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
          redirect("/page/main.html", StatusCodes.PermanentRedirect)
        }
      }
    } ~ get {
      path("page") {
        getFromFile(Paths.get(
          System.getProperty("user.dir"),
          "web",
          "login.html"
        ).toFile, ContentTypes.`text/html(UTF-8)`)
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