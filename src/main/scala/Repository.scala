import org.flywaydb.core.Flyway
import scalikejdbc._
import Settings._

case class Record(time:Long, text:String)

object Repository {

  Class.forName("org.h2.Driver")
  ConnectionPool.singleton(dbUrl, dbUser, dbPassword)
  implicit val session = AutoSession

  private val flyway = new Flyway
  flyway.setDataSource(dbUrl, dbUser, dbPassword)
  flyway.migrate()

  def recordsCount = sql"select count(*) from records".map(_.int(1)).single.apply.get

  def listRecords = Array(
    Record(0, "qwert"),
    Record(1, "äsdfg"),
    Record(2, "zxcvb")
  )

  def saveRecord(time:Long, text:String) = {
    System.out.println("save")
  }
}