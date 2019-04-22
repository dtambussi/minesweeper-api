package services

import javax.inject.{Inject, Singleton}
import models._
import repositories.{GameRepository, UserRepository}

@Singleton
class GameOrchestrator @Inject()(gameRepository: GameRepository, userRepository: UserRepository) {

  def create(request: NewGameRequest): Either[String, Game] = {
    for {
      player <- userRepository.findById(request.userId).toRight("player.not.found").right
      newGame <- Game(player, request.rowCount, request.columnCount, request.mineCount).right
      newPersistedGame <- gameRepository.insert(newGame).right
    } yield newPersistedGame
  }

  def suspend(gameId: Long, request: SuspensionRequest):  Either[String, Game] = {
    for {
      requestingUser <- userRepository.findById(request.userId).toRight("player.not.found").right
      game <- gameRepository.findById(gameId).toRight("game.not.found").right
      _ <- if (requestingUser == game.player) Right(requestingUser).right else Left("game.suspend.error.userNotAllowed").right
      suspendedGame <- game.suspend.right
      updatedGame <- Right(gameRepository.update(suspendedGame)).right
    } yield updatedGame
  }

  def resume(gameId: Long, request: ResumeRequest):  Either[String, Game] = {
    for {
      requestingUser <- userRepository.findById(request.userId).toRight("player.not.found").right
      game <- gameRepository.findById(gameId).toRight("game.not.found").right
      _ <- if (requestingUser == game.player) Right(requestingUser).right else Left("game.resume.error.userNotAllowed").right
      // no time tracking while game is suspended
      resumedGame <- game.resume.right
      updatedGame <- Right(gameRepository.update(resumedGame)).right
    } yield updatedGame
  }

  def move(gameId: Long, request: MoveRequest):  Either[String, Game] = {
    for {
      requestingUser <- userRepository.findById(request.userId).toRight("player.not.found").right
      game <- gameRepository.findById(gameId).toRight("game.not.found").right
      _ <- if (requestingUser == game.player) Right(requestingUser).right else Left("game.move.error.userNotAllowed").right
      gameAfterMove <-  game.makeMove(MoveType.fromString(request.moveType), request.rowIndex, request.colIndex).right
      updatedGame <- Right(gameRepository.update(gameAfterMove)).right
    } yield updatedGame
  }
}
