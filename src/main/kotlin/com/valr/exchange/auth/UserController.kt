package com.valr.exchange.auth

import com.valr.exchange.auth.Exceptions.DuplicateUsernameException
import com.valr.exchange.auth.Exceptions.WrongUserNameOrPasswordException
import com.valr.exchange.auth.models.UserRequestModel
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext

class UserController(private val userService: UserService) {
  fun createUser(context: RoutingContext) {
    val user = context.body().asJsonObject().mapTo(UserRequestModel::class.java)
    userService.createUser(user).onComplete {
      if (it.succeeded()) {
        context.response().setStatusCode(201).end(Json.encodePrettily(it.result()))
      } else if (it.cause() is DuplicateUsernameException) {
        context.response().setStatusCode(409).end(it.cause().message)
      } else {
        context.response().setStatusCode(500).end(it.cause().message)
      }
    }
  }

  fun login(context: RoutingContext) {
    val userId = context.pathParam("id")
    val user = context.body().asJsonObject().mapTo(UserRequestModel::class.java)
    userService.login(user).onComplete {
      if (it.succeeded()) {
        context.response().setStatusCode(201).end(Json.encodePrettily(it.result()))
      } else if (it.cause() is WrongUserNameOrPasswordException) {
        context.response().setStatusCode(401).end(it.cause().message)
      } else {
        context.response().setStatusCode(500).end(it.cause().message)
      }
    }
  }


  fun listAllUsers(context: RoutingContext) {
    userService.listAllUsers().onComplete {
      if (it.succeeded()) {
        context.response().setStatusCode(201).end(Json.encodePrettily(it.result()))
      }  else {
        context.response().setStatusCode(500).end(it.cause().message)
      }
    }
  }
}
