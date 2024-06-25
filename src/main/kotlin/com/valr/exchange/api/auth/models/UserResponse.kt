package com.valr.exchange.api.auth.models

class UserResponse(val id: String, val username: String, var authToken: String) {
  companion object {
    fun fromUser(user: User): UserResponse {
      return UserResponse(username = user.username, id = user.id, authToken = user.authToken ?: "")
    }
  }
}
