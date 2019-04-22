package models

import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.libs.json._

case class User(id: Long, nickname: String) {
  override def equals(obj: Any): Boolean = id == obj.asInstanceOf[User].id
}

object User {
  implicit val writes = Json.writes[User]
}

case class NewUserRequest(nickname: String)

object NewUserRequest {
  val form = Form(mapping("nickname" -> nonEmptyText)(NewUserRequest.apply)(NewUserRequest.unapply))
  implicit val formatter = Json.format[NewUserRequest]
}