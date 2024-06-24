package com.valr.exchange.auth

import com.valr.exchange.auth.models.User
import com.valr.exchange.auth.models.UserRequestModel
import io.vertx.core.Future

class UserService(private val userRepository: UserRepository) {
    fun createUser(user: UserRequestModel): Future<User> {
        return userRepository.save(user)
    }

    fun getUser(userId: String): Future<User?> {
        return userRepository.findById(userId)
    }
}
