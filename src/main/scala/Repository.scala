import Settings._
import com.typesafe.scalalogging.StrictLogging
import org.flywaydb.core.Flyway
import scalikejdbc._

import scala.collection.mutable

final case class RecordDbo(id: Long, time: Long, text: String, code: Int)

final case class MetricDbo(code: Int, time: Long, value: String, recordId: Long)

final case class FullRecordDbo(id: Long, time: Long, text: String, code: Int, metrics: Map[Int, String])

object Repository extends StrictLogging {

  Class.forName("org.h2.Driver")
  ConnectionPool.singleton(dbUrl, dbUser, dbPassword)
  implicit val session = AutoSession

  private val flyway = new Flyway
  flyway.setDataSource(dbUrl, dbUser, dbPassword)
  flyway.migrate()

  def listDayFoodReports = {

    val records = new mutable.HashMap[Long, RecordDbo]()
    val metrics = new mutable.HashMap[Long, Map[Int, String]]().withDefaultValue(Map())

    sql"""
      select r.id as id, r.time as time, r.text as text, r.code as code, m.code as m_code, m.value as value
      from record r left join metric m on r.id = m.record_id
      where r.code = ${Codes.FOOD_REPORT}
    """.foreach(rs => {
      logger.debug(s"row ${rs.toMap()}")
      val recordId = rs.long("id")
      records(rs.long("id")) = RecordDbo(recordId, rs.long("time"), rs.string("text"), rs.int("code"))
      Option(rs.int("m_code")) match {
        case Some(_) => metrics(recordId) = metrics(recordId) + (rs.int("m_code") -> rs.string("value"))
      }
    })

    for ((recordId, record) <- records) yield FullRecordDbo(
      recordId, record.time, record.text, record.code, metrics(recordId))
  }

  def save(rec: RecordDbo) = {
    sql"insert into record values(${rec.id}, ${rec.time}, ${rec.text}, ${rec.code})".update().apply()
  }

  def save(met: MetricDbo) = {
    sql"insert into metric values(${met.code}, ${met.time}, ${met.value}, ${met.recordId})".update().apply()
  }
}