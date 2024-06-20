package com.valr.exchange.auth

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class UserRouter(vertx: Vertx?) {
  val router: Router = Router.router(vertx)

  init {
    router.route().handler(BodyHandler.create())

    val userController = UserController(UserService(UserRepository()))
    router["/:id"].handler(userController::getUser)
    router.post("/").handler(userController::createUser)
  }

  fun getUserRouter(): Router {
    return this.router
  }

}
