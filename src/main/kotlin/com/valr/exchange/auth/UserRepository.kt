package com.valr.exchange.auth

import com.valr.exchange.auth.models.User
import com.valr.exchange.auth.models.UserRequestModel
import io.vertx.core.Future
import java.util.*

class UserRepository {
  private val users = HashMap<String, User>()

  fun save(userRequestModel: UserRequestModel): Future<User> {
    val userId = UUID.randomUUID().toString();
    val user = User(userId, userRequestModel.username, userRequestModel.password)
    users[userId] = user
    return Future.succeededFuture(user)
  }

  fun findById(userId: String): Future<User?> {
    return Future.succeededFuture(users[userId])
  }
}
