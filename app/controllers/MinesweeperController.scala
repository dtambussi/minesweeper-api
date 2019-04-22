package controllers

import javax.inject._
import models._
import play.api.Logging
import play.api.data.Forms._
import play.api.data.{Form, FormError, Forms}
import play.api.libs.json._
import play.api.mvc._
import repositories.{GameRepository, UserRepository}
import services.GameOrchestrator

@Singleton
class MinesweeperController @Inject()(cc: ControllerComponents, gameOrchestrator: GameOrchestrator, gameRepository: GameRepository, userRepository: UserRepository)
  extends AbstractController(cc) with Logging {

  implicit object FormErrorWrites extends Writes[FormError] {
    override def writes(o: FormError): JsValue = Json.obj(
      "key" -> Json.toJson(o.key),
      "message" -> Json.toJson(o.message)
    )
  }

  val newGameRequestForm = Form(Forms.mapping(
    "userId" -> longNumber,
    "rowCount" -> number(min=1),
    "columnCount" -> number(min=1),
    "mineCount" -> number(min=1))
  (NewGameRequest.apply)(NewGameRequest.unapply))

  val moveRequestForm = Form(Forms.mapping(
    "userId" -> longNumber,
    "moveType" -> nonEmptyText,
    "rowIndex" -> number(min=0),
    "colIndex" -> number(min=0))
  (MoveRequest.apply)(MoveRequest.unapply))

  val resumeRequestForm = Form(Forms.mapping("userId" -> longNumber)(ResumeRequest.apply)(ResumeRequest.unapply))

  val suspensionRequestForm = Form(Forms.mapping("userId" -> longNumber)(SuspensionRequest.apply)(SuspensionRequest.unapply))

  val newUserForm = Form(Forms.mapping("nickname" -> nonEmptyText(minLength = 0, maxLength = 50))(NewUserRequest.apply)(NewUserRequest.unapply))

  def createUser: Action[JsValue] = Action(parse.json) { implicit request =>
    newUserForm.bindFromRequest.fold(
      errorForm => BadRequest(Json.toJson(errorForm.errors)),
      newUserRequest => {
        userRepository.insert(User(0, newUserRequest.nickname)).fold(
          error => BadRequest(s"user.create.error {$error}"),
          newUser =>  Ok(Json.toJson(newUser))
        )
      }
    )
  }

  def createGame: Action[JsValue] = Action(parse.json) { implicit request =>
    newGameRequestForm.bindFromRequest.fold(
      errorForm => BadRequest(Json.toJson(errorForm.errors)),
      newGameRequest => {
        gameOrchestrator.create(newGameRequest).fold(
          error => BadRequest(s"game.create.error {$error}"),
          newGame =>  Ok(Json.toJson(newGame))
        )
      }
    )
  }

  def move(gameId: Long): Action[JsValue] = Action(parse.json) { implicit request =>
    moveRequestForm.bindFromRequest.fold(
      errorForm => BadRequest(Json.toJson(errorForm.errors)),
      moveRequest => {
        gameOrchestrator.move(gameId, moveRequest).fold(
          error => BadRequest(s"game.move.error {$error}"),
          currentGame =>  Ok(Json.toJson(currentGame))
        )
      }
    )
  }

  def suspend(gameId: Long): Action[JsValue] = Action(parse.json) { implicit request =>
    suspensionRequestForm.bindFromRequest.fold(
      errorForm => BadRequest(Json.toJson(errorForm.errors)),
      suspensionRequest => {
        gameOrchestrator.suspend(gameId, suspensionRequest).fold(
          error => BadRequest(s"game.suspend.error {$error}"),
          currentGame =>  Ok(Json.toJson(currentGame))
        )
      }
    )
  }

  def resume(gameId: Long): Action[JsValue] = Action(parse.json) { implicit request =>
    resumeRequestForm.bindFromRequest.fold(
      errorForm => BadRequest(Json.toJson(errorForm.errors)),
      resumeRequest => {
        gameOrchestrator.resume(gameId, resumeRequest).fold(
          error => BadRequest(s"game.resume.error {$error}"),
          currentGame =>  Ok(Json.toJson(currentGame))
        )
      }
    )
  }

  def listGamesByPlayer(userId: Long) = Action {
    Ok(Json.obj("games" -> gameRepository.listByPlayerUserId(userId)))
  }
}