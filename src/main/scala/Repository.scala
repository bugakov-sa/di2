import org.flywaydb.core.Flyway
import scalikejdbc._
import Settings._

object Repository {

  Class.forName("org.h2.Driver")
  ConnectionPool.singleton(dbUrl, dbUser, dbPassword)
  implicit val session = AutoSession

  private val flyway = new Flyway
  flyway.setDataSource(dbUrl, dbUser, dbPassword)
  flyway.migrate()

  def usersCount = sql"select count(*) from users".map(_.int(1)).single.apply.get

  def recordsCount = sql"select count(*) from records".map(_.int(1)).single.apply.get
}