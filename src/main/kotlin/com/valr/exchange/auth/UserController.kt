package com.valr.exchange.auth

import com.valr.exchange.common.models.UserRequestModel
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext

class UserController(private val userService: UserService) {
    fun createUser(context: RoutingContext) {
      val user = context.body().asJsonObject().mapTo(UserRequestModel::class.java)
        userService.createUser(user).onComplete {
            if (it.succeeded()) {
                context.response().setStatusCode(201).end(Json.encodePrettily(it.result()))
            } else {
                context.response().setStatusCode(500).end()
            }
        }
    }

    fun getUser(context: RoutingContext) {
        val userId = context.pathParam("id")
        userService.getUser(userId).onComplete {
            if (it.succeeded()) {
                val user = it.result()
                if (user != null) {
                    context.response().end(Json.encodePrettily(user))
                } else {
                    context.response().setStatusCode(404).end()
                }
            } else {
                context.response().setStatusCode(500).end()
            }
        }
    }
}
