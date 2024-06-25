package com.valr.exchange.api.auth

import com.valr.exchange.api.auth.models.User
import com.valr.exchange.api.auth.models.UserRequestModel
import io.vertx.core.Future

class UserService(private val userRepository: UserRepository) {
  fun createUser(user: UserRequestModel): Future<User> {
    return userRepository.saveUser(user)
  }

  fun login(user: UserRequestModel): Future<User> {
    return userRepository.login(user)
  }

  fun listAllUsers(): Future<List<User>> {
    return userRepository.getAllUsers()
  }
}
