package models

case class User(id: Long, nickname: String) {
  override def equals(obj: Any): Boolean = id == obj.asInstanceOf[User].id
}

case class NewUserRequest(nickname: String)

