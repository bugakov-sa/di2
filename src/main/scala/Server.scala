import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
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
    }
}

object ServerStarter extends App {
  Server.startServer(Settings.host, Settings.port)
}