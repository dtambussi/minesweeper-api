package models

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json._
import services.BoardGenerator

case class NewGameRequest(userId: Long, rowCount: Int, columnCount: Int, mineCount: Int)

object NewGameRequest {
  implicit val formatter = Json.format[NewGameRequest]
}

case class SuspensionRequest(userId: Long)
object SuspensionRequest {
  implicit val formatter = Json.format[SuspensionRequest]
}

case class ResumeRequest(userId: Long)
object ResumeRequest {
  implicit val formatter = Json.format[ResumeRequest]
}

case class MoveRequest(userId: Long, moveType: String, rowIndex: Int, colIndex: Int)
object MoveRequest {
  implicit val formatter = Json.format[MoveRequest]
}

sealed trait MoveType
case object Reveal extends MoveType
case object FlagAsMine extends MoveType
case object QuestionMark extends MoveType
case object Unknown extends MoveType

object MoveType {
  def fromString(value: String): MoveType = Seq(Reveal, FlagAsMine, QuestionMark).find(_.toString equalsIgnoreCase value).getOrElse(Unknown)
}

case class Game(
                 id: Long,
                 player: User,
                 rowCount: Int,
                 columnCount: Int,
                 mineCount: Int,
                 board: Board,
                 totalTime: Int,
                 createdAt: DateTime,
                 latestInteractionAt: DateTime,
                 isFinished: Boolean = false,
                 isWinner: Boolean = false,
                 isSuspended: Boolean = false) {

  def suspend: Either[String, Game] = {
    if (!(isFinished || isSuspended)) {
      Right(copy(isSuspended = true, latestInteractionAt = DateTime.now(DateTimeZone.UTC)))
    } else Left("game.suspend.error.notSuspendable")
  }

  def resume: Either[String, Game] = {
    if (isSuspended) {
      Right(copy(isSuspended = false, latestInteractionAt = DateTime.now(DateTimeZone.UTC)))
    } else Left("game.resume.error.notResumable")
  }

  def makeMove(moveType: MoveType, rowIndex: Int, colIndex: Int): Either[String, Game] = {
    if (gameNotFinishedOrSuspended) {
      moveType match {
        case Reveal => reveal(rowIndex, colIndex)
        case FlagAsMine => flagAsMine(rowIndex, colIndex)
        case QuestionMark => questionMark(rowIndex, colIndex)
        case _ => Left("game.move.error.unknownMoveType")
      }
    } else Left("game.move.error.gameNotInPlayableStatus")
  }

  private def reveal(rowIndex: Int, colIndex: Int): Either[String, Game] = {
    board.reveal(rowIndex, colIndex).fold(
      error => Left(error),
      modifiedBoard => {
        val lost = board.anyMinesRevealed
        val won = !lost && board.allCellsThatAreNotMinesAreRevealed && board.allMinesAreFlagged
        Right(copy(board = modifiedBoard, isWinner = won, isFinished = lost || won))
      }
    )
  }

  private def flagAsMine(rowIndex: Int, colIndex: Int): Either[String, Game] = {
    board.flagAsMine(rowIndex, colIndex).fold(
      error => Left(error),
      modifiedBoard => Right(copy(board = modifiedBoard))
    )
  }

  private def questionMark(rowIndex: Int, colIndex: Int): Either[String, Game] = {
    board.questionMark(rowIndex, colIndex)fold(
      error => Left(error),
      modifiedBoard => Right(copy(board = modifiedBoard))
    )
  }

  private def gameNotFinishedOrSuspended: Boolean = !(isFinished || isSuspended)
}

object Game {

  implicit val jodaDateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val jodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val writes = new Writes[Game] {
    def writes(game: Game): JsValue = {
      Json.obj(
        "id" -> game.id,
        "rowCount" -> game.rowCount,
        "columnCount" -> game.columnCount,
        "mineCount" -> game.mineCount,
        "rowCount" -> game.rowCount,
        "totalTimeInSecs" -> game.totalTime,
        "isSuspended" -> game.isSuspended,
        "isFinished" -> game.isFinished,
        "isWinner" -> game.isWinner,
        "createdAt" -> Json.toJson(game.createdAt),
        "latestInteractionAt" -> Json.toJson(game.latestInteractionAt),
        "board" -> Json.toJson(game.board)
      )
    }
  }

  def apply(player: User, rowCount: Int, columnCount: Int, mineCount: Int): Either[String, Game] = {
    validateNewGameParams(rowCount, columnCount, mineCount).fold(
      error => Left(error),
      _ => Right(
        Game(
          0,
          player,
          rowCount,
          columnCount,
          mineCount,
          BoardGenerator.generateBoard(rowCount, columnCount, mineCount),
          totalTime = 0,
          createdAt = DateTime.now(DateTimeZone.UTC),
          latestInteractionAt = DateTime.now(DateTimeZone.UTC)
        )
      ))
  }

  private def validateNewGameParams(rowCount: Int, columnCount: Int, mineCount: Int): Either[String, String] = {
    if (mineCount < rowCount * columnCount) Right("Ok") else Left("mineCountMustBeLowerThanTotalCells")
  }
}