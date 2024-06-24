package com.valr.exchange.orderbook

import com.valr.exchange.*
import com.valr.exchange.common.EventConsumerPayload
import com.valr.exchange.common.EventConsumerMessage
import com.valr.exchange.common.exceptions.NotFoundException
import com.valr.exchange.orderbook.models.LimitOrderRequestModel
import com.valr.exchange.orderbook.models.Order
import com.valr.exchange.orderbook.models.OrderHistoryRequestModel
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Future

import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.ReplyException

class OrderBookRepository(val eventBus: EventBus) {

  fun saveLimitOrder(orderRequest: LimitOrderRequestModel): Future<Order> {
    val result = Promise.promise<Order>()

    val message = EventConsumerMessage(OrderBookActions.save, orderRequest)
    eventBus.request<EventConsumerMessage<EventConsumerPayload>>(
      EventBusAddress.orderbook_consumer.name,
      message
    ).onComplete() {
      if (it.succeeded()) {
        result.complete(it.result().body().payload as Order)
      } else {
        result.fail(it.cause())
      }
    };
    return result.future();
  }

  fun getOrderBook(currencyPair: String): Future<CurrencyOrderBook> {
    val result = Promise.promise<CurrencyOrderBook>()

    val message = EventConsumerMessage(OrderBookActions.fetch_orderbook, currencyPair)
    eventBus.request<EventConsumerMessage<String>>(
      EventBusAddress.orderbook_consumer.name,
      message
    ).onComplete() {
      if (it.succeeded()) {
        result.complete(it.result().body().payload as CurrencyOrderBook?)
      } else {
        val cause = it.cause() as ReplyException
        if (cause.failureCode() == HttpResponseStatus.NOT_FOUND.code()) {
          result.fail(cause.message?.let { msg -> NotFoundException(msg) })
        } else {
          result.fail(cause)
        }
      }
    };
    return result.future()
  }

  fun getOrderHistory(userId: String, currencyPair: String): Future<List<Order>> {
    val result = Promise.promise<List<Order>>()

    val message =
      EventConsumerMessage(OrderBookActions.fetch_orderhistory, OrderHistoryRequestModel(userId, currencyPair))
    eventBus.request<EventConsumerMessage<String>>(
      EventBusAddress.orderbook_consumer.name,
      message
    ).onComplete() {
      if (it.succeeded()) {
        result.complete(it.result().body().payload as List<Order>?)
      } else {
        val cause = it.cause() as ReplyException
        if (cause.failureCode() == HttpResponseStatus.NOT_FOUND.code()) {
          result.fail(cause.message?.let { msg -> NotFoundException(msg) })
        } else {
          result.fail(cause)
        }
      }
    };
    return result.future()
  }
}
