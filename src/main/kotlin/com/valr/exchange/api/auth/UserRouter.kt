package com.valr.exchange.api.auth

import com.valr.exchange.api.common.utils.Repositories
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

class UserRouter(vertx: Vertx) {
  val router: Router = Router.router(vertx)

  init {
    val userController =
      UserController(UserService(Repositories.userRepository))

    router.post("/login").handler(userController::login)
    router.post("/signup").handler(userController::createUser)
    router["/users/all"].handler(userController::listAllUsers)
  }

  fun getUserRouter(): Router {
    return this.router
  }

}
