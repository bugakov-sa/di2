import java.nio.file.Paths

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{HttpApp, Route}
import com.typesafe.scalalogging.StrictLogging
import spray.json.DefaultJsonProtocol._

object Server extends HttpApp with StrictLogging {

  final case class SaveRecordRequest(text:String)

  implicit val saveRecordRequestFormat = jsonFormat1(SaveRecordRequest)
  implicit val recordFormat = jsonFormat2(Record)

  override def routes: Route =
    get {
      path("") {
        redirect("/page/main.html", Found)
      } ~ pathPrefix("page") {
        getFromDirectory(Paths.get(
          System.getProperty("user.dir"),
          "web"
        ).toString)
      }
    } ~ get {
      path("api" / "records") {
        complete(Repository.listRecords)
      }
    } ~ get {
      path("api" / "save") {
        parameters('text.as[String]) { text =>
          Repository.saveRecord(System.currentTimeMillis, text)
          redirect("/page/main.html", Found)
        }
      }
    }
}

object ServerStarter extends App {
  Server.startServer(Settings.host, Settings.port)
}