package controllers

import javax.inject._
import models.Game._
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

}
