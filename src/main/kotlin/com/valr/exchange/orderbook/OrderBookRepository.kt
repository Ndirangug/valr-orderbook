package com.valr.exchange.orderbook

import com.valr.exchange.*
import com.valr.exchange.common.models.EventConsumerPayload
import com.valr.exchange.common.models.EventConsumerMessage
import com.valr.exchange.orderbook.models.LimitOrderRequestModel
import com.valr.exchange.orderbook.models.Order
import io.vertx.core.Future

import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus

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
        result.fail(it.cause())
      }
    };
    return result.future()

  }
}
