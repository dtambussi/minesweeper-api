package repositories

import anorm.SqlParser.{get, scalar}
import anorm._
import models.User
import javax.inject.Inject
import play.api.db.DBApi

@javax.inject.Singleton
class UserRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  val parser: RowParser[User] = {
      get[Long]("user.id") ~
      get[String]("user.nickname") map {
      case id ~ nickname => User(id, nickname)
    }
  }

  def insert(user: User): Either[String, User] = {
    db.withConnection { implicit connection =>
      try {
        val userId = SQL(
          """
            insert into user(nickname) values ({nickname})
          """).on(
          'nickname -> user.nickname)
          .executeInsert(scalar[Long].single)
        Right(getById(userId))
      } catch {
        case e: Exception => {
          Left(s"game.create.error {${e.getMessage}}")
        }
      }
    }
  }

  def getById(userId: Long): User = {
    db.withConnection { implicit connection =>
      val user = SQL(
        """
          select * from user
          where id = {userId}
        """).on(
        'userId -> userId).as(parser.single)
      user
    }
  }

  def findById(userId: Long): Option[User] = {
    db.withConnection { implicit connection =>
      val user = SQL(
        """
          select * from user
          where id = {userId}
        """).on(
        'userId -> userId).as(parser.singleOpt)
      user
    }
  }
}
