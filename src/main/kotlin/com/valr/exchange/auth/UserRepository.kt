package com.valr.exchange.auth

import com.valr.exchange.EventBusAddress
import com.valr.exchange.auth.Exceptions.DuplicateUsernameException
import com.valr.exchange.auth.Exceptions.WrongUserNameOrPasswordException
import com.valr.exchange.auth.models.User
import com.valr.exchange.auth.models.UserRequestModel
import com.valr.exchange.common.models.EventConsumerMessage
import com.valr.exchange.common.models.EventConsumerPayload
import com.valr.exchange.orderbook.OrderBookActions
import com.valr.exchange.orderbook.models.Order
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.ReplyException
import java.util.*

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
        if (cause.failureCode() == 409) {
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
      if (it.succeeded() && it.result().body().payload is User) {
        result.complete(it.result().body().payload as User)
      } else {
        val cause = it.cause() as ReplyException
        if (cause.failureCode() == 401) {
          result.fail(cause.message?.let { msg -> WrongUserNameOrPasswordException(msg) })
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
