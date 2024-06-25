package com.valr.exchange.api.auth

import com.valr.exchange.api.HttpVerticle
import com.valr.exchange.api.auth.exceptions.DuplicateUsernameException
import com.valr.exchange.api.auth.exceptions.WrongUserNameOrPasswordException
import com.valr.exchange.api.auth.models.User
import com.valr.exchange.api.auth.models.UserRequestModel
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.web.RoutingContext

class UserController(private val userService: UserService) {
  fun createUser(context: RoutingContext) {
    val user = context.body().asJsonObject().mapTo(UserRequestModel::class.java)
    userService.createUser(user).onComplete {
      if (it.succeeded()) {
        context.response().setStatusCode(HttpResponseStatus.CREATED.code()).end(Json.encodePrettily(it.result()))
      } else if (it.cause() is DuplicateUsernameException) {
        context.response().setStatusCode(HttpResponseStatus.CONFLICT.code()).end(it.cause().message)
      } else {
        context.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(it.cause().message)
      }
    }
  }

  fun login(context: RoutingContext) {
    val user = context.body().asJsonObject().mapTo(UserRequestModel::class.java)
    userService.login(user).onComplete {
      if (it.succeeded()) {
        val user = (it.result() as User)
        val token = HttpVerticle.jwtAuth.generateToken(
         JsonObject( mapOf("sub" to user.id, "username" to user.username)),
          JWTOptions().setAlgorithm("HS256").setExpiresInMinutes(60)
        )
        user.authToken = token
        context.response().setStatusCode(HttpResponseStatus.OK.code()).end(Json.encodePrettily(user))
      } else if (it.cause() is WrongUserNameOrPasswordException) {
        context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end(it.cause().message)
      } else {
        context.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(it.cause().message)
      }
    }
  }


  fun listAllUsers(context: RoutingContext) {
    userService.listAllUsers().onComplete {
      if (it.succeeded()) {
        context.response().setStatusCode(HttpResponseStatus.OK.code()).end(Json.encodePrettily(it.result()))
      }  else {
        context.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(it.cause().message)
      }
    }
  }
}
