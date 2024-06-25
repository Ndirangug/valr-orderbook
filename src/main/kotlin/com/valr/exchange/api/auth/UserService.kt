package com.valr.exchange.api.auth

import com.valr.exchange.api.auth.models.UserRequestModel
import com.valr.exchange.api.auth.models.UserResponse
import io.vertx.core.Future
import io.vertx.core.Promise

class UserService(private val userRepository: UserRepository) {
  fun createUser(user: UserRequestModel): Future<UserResponse> {
    val userPromise = Promise.promise<UserResponse>()

    userRepository.saveUser(user).onComplete(){
      if(it.succeeded()){
        userPromise.complete(UserResponse.fromUser(it.result()))
      }else{
        userPromise.fail(it.cause())
      }
    }
    return userPromise.future()
  }

  fun login(user: UserRequestModel): Future<UserResponse> {
    val userPromise = Promise.promise<UserResponse>()

    userRepository.login(user).onComplete(){
      if(it.succeeded()){
        userPromise.complete(UserResponse.fromUser(it.result()))
      }else{
        userPromise.fail(it.cause())
      }
    }
    return userPromise.future()

  }

  fun listAllUsers(): Future<List<UserResponse>> {
    val usersPromise = Promise.promise<List<UserResponse>>()

    userRepository.getAllUsers().onComplete{ ar ->
      if(ar.succeeded()){
        val userResponseList = ar.result().map { user -> UserResponse.fromUser(user) }
        usersPromise.complete(userResponseList)
      }else{
        usersPromise.fail(ar.cause())
      }
    }
    return usersPromise.future()

  }
}
