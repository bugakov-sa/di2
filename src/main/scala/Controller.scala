import java.nio.file.Paths

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{HttpApp, Route}
import com.typesafe.scalalogging.StrictLogging
import spray.json.DefaultJsonProtocol._

object Controller extends HttpApp with StrictLogging {

  final case class RequestSaveRecord(text:String)
  final case class ResponseFoodReport(id:Long, time: Long, paragraphs: Vector[Vector[String]], kcalPerDay:Int, eatPerDay:Int)

  implicit val saveRecordRequestFormat = jsonFormat1(RequestSaveRecord)
  implicit val responseFoodReportFormat = jsonFormat5(ResponseFoodReport)

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
        complete(Service.listDayFoodReports)
      }
    } ~ get {
      path("api" / "save") {
        parameters('text.as[String]) { text =>
          Service.saveFoodReport(text)
          redirect("/page/main.html", Found)
        }
      }
    } ~ get {
      path("api" / "delete") {
        parameters('id.as[Long]) { id =>
          Repository.delete(id)
          redirect("/page/main.html", Found)
        }
      }
    } ~ get {
      path("api" / "trends" / "kcal") {
        complete(Repository.listKcalTrend)
      }
    }
}