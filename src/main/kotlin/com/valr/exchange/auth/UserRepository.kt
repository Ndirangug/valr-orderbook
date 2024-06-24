package com.valr.exchange.auth

import com.valr.exchange.EventBusAddress
import com.valr.exchange.auth.exceptions.DuplicateUsernameException
import com.valr.exchange.auth.exceptions.WrongUserNameOrPasswordException
import com.valr.exchange.auth.models.User
import com.valr.exchange.auth.models.UserRequestModel
import com.valr.exchange.common.EventConsumerMessage
import com.valr.exchange.common.EventConsumerPayload
import com.valr.exchange.common.Singleton
import com.valr.exchange.common.exceptions.NotFoundException
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpStatusClass
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.ReplyException

class UserRepository(val eventBus: EventBus) {

  fun saveUser(userRequestModel: UserRequestModel): Future<User> {
    val result = Promise.promise<User>()

    val message = EventConsumerMessage(UserActions.signup, userRequestModel)
    eventBus.request<EventConsumerMessage<EventConsumerPayload>>(
      EventBusAddress.auth_consumer.name,
      message
    ).onComplete() {
      if (it.succeeded()) {
        result.complete(it.result().body().payload as User)
      } else {
        val cause = it.cause() as ReplyException
        if (cause.failureCode() == HttpResponseStatus.CONFLICT.code()) {
          result.fail(cause.message?.let { msg -> DuplicateUsernameException(msg) })
        } else {
          result.fail(cause)

        }
      }
    };

    return result.future();
  }

  fun login(userRequestModel: UserRequestModel): Future<User> {
    val result = Promise.promise<User>()

    val message = EventConsumerMessage(UserActions.login, userRequestModel)
    eventBus.request<EventConsumerMessage<EventConsumerPayload>>(
      EventBusAddress.auth_consumer.name,
      message
    ).onComplete() {
      if (it.succeeded()) {
        result.complete(it.result().body().payload as User)
      } else {
        val cause = it.cause() as ReplyException
        if (cause.failureCode() == HttpResponseStatus.UNAUTHORIZED.code()) {
          result.fail(cause.message?.let { msg -> WrongUserNameOrPasswordException(msg) })
        } else {
          result.fail(cause)
        }
      }
    };

    return result.future();
  }

  fun getUser(username: String): Future<User?> {
    val result = Promise.promise<User?>()

    val message = EventConsumerMessage(UserActions.get_user, username)
    eventBus.request<EventConsumerMessage<EventConsumerPayload>>(
      EventBusAddress.auth_consumer.name,
      message
    ).onComplete() {
      if (it.succeeded()) {
        result.complete(it.result().body().payload as User)
      } else {
        val cause = it.cause() as ReplyException
        if (cause.failureCode() == HttpResponseStatus.NOT_FOUND.code()) {
          result.fail(cause.message?.let { msg -> NotFoundException(msg) })
        } else {
          result.fail(cause)
        }
      }
    };

    return result.future();
  }

  fun getAllUsers(): Future<List<User>> {
    val result = Promise.promise<List<User>>()

    val message = EventConsumerMessage(UserActions.fetch_users, null)
    eventBus.request<EventConsumerMessage<EventConsumerPayload>>(
      EventBusAddress.auth_consumer.name,
      message
    ).onComplete() {
      if (it.succeeded()) {
        result.complete((it.result().body().payload as List<User>))
      } else {
        result.fail(it.cause())
      }
    };

    return result.future();
  }
}
