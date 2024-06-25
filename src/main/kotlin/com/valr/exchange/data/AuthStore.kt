package com.valr.exchange.data

import com.valr.exchange.api.auth.UserActions
import com.valr.exchange.api.auth.models.User
import com.valr.exchange.api.auth.models.UserRequestModel
import com.valr.exchange.api.common.EventConsumerMessage
import com.valr.exchange.api.common.EventConsumerPayload
import com.valr.exchange.data.common.EventBusAddress
import com.valr.exchange.data.common.generateUUID
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.eventbus.EventBus

typealias UsersMap = HashMap<String, User>

class AuthStore(val eventBus: EventBus) {
  val users: UsersMap = hashMapOf()
  init {
      setupAuthConsumer(eventBus);
  }

  private fun setupAuthConsumer(eventBus: EventBus) {
    val authConsumer =
      eventBus.consumer<EventConsumerMessage<EventConsumerPayload>>(EventBusAddress.auth_consumer.name)

    authConsumer.handler { message ->
      run {
        when (message.body().action) {
          UserActions.signup -> {
            val userRequest = message.body().payload as UserRequestModel

            if (!users.containsKey(userRequest.username)) {
              val newUser = User(
                id = generateUUID(),
                username = userRequest.username,
                password = userRequest.password,
              );
              users[newUser.username] = newUser
              message.reply(EventConsumerMessage(action = UserActions.signup, newUser))
            } else {
              message.fail(
                HttpResponseStatus.CONFLICT.code(),
                "User with username ${userRequest.username} already exists"
              )
            }
          }

          UserActions.login -> {
            val userRequest = message.body().payload as UserRequestModel
            val userFound =
              users.containsKey(userRequest.username) && users[userRequest.username]?.password == userRequest.password
            val user = users[userRequest.username]

            if (userFound) {
              message.reply(EventConsumerMessage(action = UserActions.login, user))
            } else {
              message.fail(HttpResponseStatus.UNAUTHORIZED.code(), "Wrong username or password")
            }
          }

          UserActions.fetch_users -> {
            val usersList = users.values.toList().map { user -> User(id = user.id, username = user.username) }
            message.reply(EventConsumerMessage(action = UserActions.fetch_users, usersList))
          }

          UserActions.get_user -> {
            val username = message.body().payload as String
            val user = users[username]

            if (user != null) {
              message.reply(EventConsumerMessage(action = UserActions.get_user, user))
            } else {
              message.fail(HttpResponseStatus.NOT_FOUND.code(), "User with ${username} not found")
            }
          }
        }
      }
    }
  }
}
