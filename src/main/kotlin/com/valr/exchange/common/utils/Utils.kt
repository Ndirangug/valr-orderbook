package com.valr.exchange.common.utils

import com.valr.exchange.auth.models.User
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext


fun getCurrentUser(context: RoutingContext): Future<User?> {
  val userRepository = Repositories.userRepository;
  val userPrincipal = context.user()
  val username = userPrincipal.principal().getString("username")

  return userRepository.getUser(username)
}
