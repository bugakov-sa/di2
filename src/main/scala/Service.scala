import java.time.ZonedDateTime
import java.util.Calendar

import Controller.ResponseFoodReport

final case class FoodReport(time: Long, paragraphs: Vector[Vector[String]], kcalPerDay: Int, eatPerDay: Int)

object Codes {

  val FOOD_REPORT = 0
  val WEIGHTING_REPORT = 1
  val FREE_RECORD = 2

  val KCAL_PER_DAY = 0
  val EATING_PER_DAY = 1
  val MASS = 2
  val FAT_PERCENT = 3
}

object Service {

  private val datePattern = "([0-9]{1,2})[ ]+([а-я]{3,4})".r

  def saveFoodReport(text: String) = {

    val lines = text.trim.split("\n")
    val (id, time, savingText) = lines.head.trim match {
      case datePattern(day, month) => {
        val calendar = Calendar.getInstance()
        calendar.set(ZonedDateTime.now.getYear, monthIndex(month), day.toInt)
        (
          System.currentTimeMillis,
          calendar.getTimeInMillis,
          lines.tail.reduce(_ + "\n" + _)
        )
      }
      case _ => {
        val currMillis = System.currentTimeMillis
        (currMillis, currMillis, text)
      }
    }

    Repository.save(RecordDbo(id, time, savingText, Codes.FOOD_REPORT))
    Repository.save(MetricDbo(Codes.KCAL_PER_DAY, time, kcalCount(savingText).toString, id))
    Repository.save(MetricDbo(Codes.EATING_PER_DAY, time, parseParagraphs(savingText).length.toString, id))
  }

  private def monthIndex(month: String) = month match {
    case "янв" => 0
    case "фев" => 1
    case "март" => 2
    case "апр" => 3
    case "май" => 4
    case "июнь" => 5
    case "июль" => 6
    case "авг" => 7
    case "сент" => 8
    case "окт" => 9
    case "нояб" => 10
    case "дек" => 11
  }

  private def kcalCount(text: String) = text
    .split("\n")
    .map(_.trim)
    .filter(!_.isEmpty)
    .filter(_.split(" ").length > 0)
    .map(_.split(" ").head)
    .filter(_.matches("[0-9]{2,4}"))
    .map(_.toInt)
    .sum

  private def parseParagraphs(text: String) = text.trim
    .replaceAll("[\\w]*\n[\\w]*\n[\\w]*", "\n\n")
    .split("\n\n")
    .map(_.trim.split("\n").toVector)
    .toVector

  private def map(fullRecord: FullRecordDbo) = ResponseFoodReport(
    fullRecord.time,
    Service.parseParagraphs(fullRecord.text),
    fullRecord.metrics(Codes.KCAL_PER_DAY).toInt,
    fullRecord.metrics(Codes.EATING_PER_DAY).toInt
  )

  def listDayFoodReports = Repository.listDayFoodReports.map(map _).toVector.sortBy(-_.time)
}