import java.time.ZonedDateTime
import java.util.Calendar

import Controller.ResponseFoodReport

final case class FoodReport(time: Long, paragraphs: Vector[Vector[String]], kcalPerDay: Int, eatPerDay: Int)

final object RecordCode extends Enumeration {
  val FOOD_REPORT = Value(0)
  val WEIGHTING_REPORT = Value(1)
  val FREE_RECORD = Value(2)
}

final object MetricCode extends Enumeration {
  val KCAL_PER_DAY = Value(0)
  val EATING_PER_DAY = Value(1)
  val MASS = Value(2)
  val FAT_PERCENT = Value(3)
}

object AnaliticUtil {
   def kcalCount(foodReportText: String) = foodReportText
    .split("\n")
    .map(_.trim)
    .filter(!_.isEmpty)
    .filter(_.split(" ").length > 0)
    .map(_.split(" ").head)
    .filter(_.matches("[0-9]{2,4}"))
    .map(_.toInt)
    .sum

  def parseParagraphs(foodReportText: String) = foodReportText.trim
    .replaceAll("[\\w]*\n[\\w]*\n[\\w]*", "\n\n")
    .split("\n\n")
    .map(_.trim.split("\n").toVector)
    .toVector

  def eatingCount(foodReportText: String) = parseParagraphs(foodReportText).length
}

object Service {

  import AnaliticUtil._

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

    Repository.save(RecordDbo(id, time, savingText, RecordCode.FOOD_REPORT.id))
    Repository.save(MetricDbo(MetricCode.KCAL_PER_DAY.id, time, kcalCount(savingText).toString, id))
    Repository.save(MetricDbo(MetricCode.EATING_PER_DAY.id, time, eatingCount(savingText).toString, id))
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

  private def map(fullRecord: FullRecordDbo) = ResponseFoodReport(
    fullRecord.time,
    AnaliticUtil.parseParagraphs(fullRecord.text),
    fullRecord.metrics(MetricCode.KCAL_PER_DAY.id).toInt,
    fullRecord.metrics(MetricCode.EATING_PER_DAY.id).toInt
  )

  def listDayFoodReports = Repository.listDayFoodReports.map(map _).toVector.sortBy(-_.time)
}