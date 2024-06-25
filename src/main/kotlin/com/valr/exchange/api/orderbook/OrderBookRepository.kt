package com.valr.exchange.api.orderbook

import com.valr.exchange.api.common.EventConsumerMessage
import com.valr.exchange.api.common.EventConsumerPayload
import com.valr.exchange.api.common.exceptions.NotFoundException
import com.valr.exchange.api.orderbook.models.LimitOrderRequestModel
import com.valr.exchange.api.orderbook.models.TradeHistoryRequestModel
import com.valr.exchange.data.CurrencyOrderBook
import com.valr.exchange.data.common.EventBusAddress
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Future

import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.ReplyException

class OrderBookRepository(val eventBus: EventBus) {

  fun saveLimitOrder(orderRequest: LimitOrderRequestModel): Future<com.valr.exchange.api.orderbook.models.Order> {
    val result = Promise.promise<com.valr.exchange.api.orderbook.models.Order>()

    val message = EventConsumerMessage(OrderBookActions.save, orderRequest)
    eventBus.request<EventConsumerMessage<EventConsumerPayload>>(
      EventBusAddress.orderbook_consumer.name,
      message
    ).onComplete() {
      if (it.succeeded()) {
        result.complete(it.result().body().payload as com.valr.exchange.api.orderbook.models.Order)
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

  fun getOrderHistory(
    userId: String,
    currencyPair: String
  ): Future<List<com.valr.exchange.api.orderbook.models.Order>> {
    val result = Promise.promise<List<com.valr.exchange.api.orderbook.models.Order>>()

    val message =
      EventConsumerMessage(OrderBookActions.fetch_orderhistory, TradeHistoryRequestModel(userId, currencyPair))
    eventBus.request<EventConsumerMessage<String>>(
      EventBusAddress.orderbook_consumer.name,
      message
    ).onComplete() {
      if (it.succeeded()) {
        result.complete(it.result().body().payload as List<com.valr.exchange.api.orderbook.models.Order>?)
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
