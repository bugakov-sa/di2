import java.nio.file.Paths

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{HttpApp, Route}
import com.typesafe.scalalogging.StrictLogging
import spray.json.DefaultJsonProtocol._

object Controller extends HttpApp with StrictLogging {

  final case class RequestSaveRecord(text:String)
  final case class ResponseFoodReport(time: Long, paragraphs: Vector[Vector[String]], kcalPerDay:Int, eatPerDay:Int)

  implicit val saveRecordRequestFormat = jsonFormat1(RequestSaveRecord)
  implicit val responseFoodReportFormat = jsonFormat4(ResponseFoodReport)

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
        complete(Service.listDayFoodReports.map(r => ResponseFoodReport(r.time, r.paragraphs, r.kcalPerDay, r.eatPerDay)))
      }
    } ~ get {
      path("api" / "save") {
        parameters('text.as[String]) { text =>
          Service.saveFoodReport(text)
          redirect("/page/main.html", Found)
        }
      }
    }
}

object Application extends App {
  Controller.startServer(Settings.host, Settings.port)
}