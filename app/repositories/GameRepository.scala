package repositories

import anorm.JodaParameterMetaData._
import anorm.SqlParser.{get, scalar}
import anorm._
import javax.inject.Inject
import models.{Board, Game}
import org.joda.time.DateTime
import play.api.db.DBApi

@javax.inject.Singleton
class GameRepository @Inject()(dbapi: DBApi)(userRepository: UserRepository)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  val parser: RowParser[Game] = {
    get[Long]("game.id") ~
    get[Long]("game.player_id") ~
    get[Int]("game.row_count") ~
    get[Int]("game.column_count") ~
    get[Int]("game.mine_count") ~
    get[String]("game.board") ~
    get[Int]("game.total_time") ~
    get[DateTime]("game.created_at") ~
    get[DateTime]("game.latest_interaction_at") ~
    get[Boolean]("game.is_suspended") ~
    get[Boolean]("game.is_finished") ~
    get[Boolean]("game.is_winner") map {
      case id ~ playerId ~ rowCount ~ columnCount ~ mineCount ~ boardContents ~ totalTime ~ createdAt ~ latestInteractionAt ~ isSuspended ~ isFinished ~ isWinner =>
        Game(
          id,
          userRepository.findById(playerId).getOrElse(throw new RuntimeException(s"player.not.found {$playerId}")),
          rowCount,
          columnCount,
          mineCount,
          Board(rowCount, columnCount, boardContents),
          totalTime,
          createdAt,
          latestInteractionAt,
          isFinished,
          isWinner,
          isSuspended
        )
    }
  }

  def insert(game: Game): Either[String, Game] = {
    db.withConnection { implicit connection =>
      try {
        val gameId = SQL(
          """
          insert into game(player_id, row_count, column_count, mine_count, total_time, created_at, latest_interaction_at, is_suspended, is_finished, is_winner, board)
          values (
            {playerId}, {rowCount}, {columnCount}, {mineCount}, {totalTime}, {createdAt}, {latestInteractionAt}, {isSuspended}, {isFinished}, {isWinner}, {board}
          )
        """).on(
          'playerId -> game.player.id,
          'rowCount -> game.rowCount,
          'columnCount -> game.columnCount,
          'mineCount -> game.mineCount,
          'totalTime -> game.totalTime,
          'createdAt -> game.createdAt,
          'latestInteractionAt -> game.latestInteractionAt,
          'isSuspended -> game.isSuspended,
          'isFinished -> game.isFinished,
          'isWinner -> game.isWinner,
          'board -> game.board.toString)
          .executeInsert(scalar[Long].single)
        Right(getById(gameId))
      } catch {
        case e: Exception => {
          Left(e.getMessage)
        }
      }
    }
  }

  def update(game: Game): Game = {
    db.withConnection { implicit connection =>
      SQL(
        """
          update game set
          player_id = {playerId},
          row_count = {rowCount},
          column_count = {columnCount},
          mine_count = {mineCount},
          total_time = {totalTime},
          created_at = {createdAt},
          latest_interaction_at = {latestInteractionAt},
          is_suspended = {isSuspended},
          is_finished = {isFinished},
          is_winner = {isWinner},
          board = {board}
          where id = {gameId}
        """).on(
          'playerId -> game.player.id,
          'rowCount -> game.rowCount,
          'columnCount -> game.columnCount,
          'mineCount -> game.mineCount,
          'totalTime -> game.totalTime,
          'createdAt -> game.createdAt,
          'latestInteractionAt -> game.latestInteractionAt,
          'isSuspended -> game.isSuspended,
          'isFinished -> game.isFinished,
          'isWinner -> game.isWinner,
          'board -> game.board.toString,
          'gameId -> game.id).executeUpdate()
      getById(game.id)
    }
  }

  def findById(gameId: Long): Option[Game] = {
    db.withConnection { implicit connection =>
      val game = SQL(
        """
          select * from game
          where id = {gameId}
        """).on(
        'gameId -> gameId).as(parser.singleOpt)
      game
    }
  }

  def getById(gameId: Long): Game = {
    db.withConnection { implicit connection =>
      val game = SQL(
        """
        select * from game
        where id = {gameId}
      """).on(
        'gameId -> gameId).as(parser.single)
      game
    }
  }

  def listByPlayerUserId(userId: Long): List[Game] = {
    db.withConnection { implicit connection =>
      val game = SQL(
        """
          select * from game
          where player_id = {playerId}
        """).on(
        'playerId -> userId).as(parser.*)
      game
    }
  }
}
