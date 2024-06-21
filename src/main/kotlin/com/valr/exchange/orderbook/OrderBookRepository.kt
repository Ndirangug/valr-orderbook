package com.valr.exchange.orderbook

import com.valr.exchange.EventBusAddress
import com.valr.exchange.HttpVerticle
import com.valr.exchange.OrderBookActions
import com.valr.exchange.OrderBookConsumerMessage
import com.valr.exchange.orderbook.models.OrderBookConsumerMessageCodec
import com.valr.exchange.orderbook.models.OrderBookConsumerPayload
import io.vertx.core.Future
import com.valr.exchange.orderbook.models.OrderBookConsumerPayload.Order as Order
import com.valr.exchange.orderbook.models.OrderBookConsumerPayload.LimitOrderRequest as LimitOrderRequest

import io.vertx.core.Promise
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus

class OrderBookRepository(val eventBus: EventBus) {

  fun saveLimitOrder(orderRequest: LimitOrderRequest): Future<Order> {
    val result = Promise.promise<Order>()

    val message = OrderBookConsumerMessage(OrderBookActions.save, orderRequest)
    eventBus.request<OrderBookConsumerMessage<OrderBookConsumerPayload>>(
      EventBusAddress.orderbook_consumer.name,
      message
    ).onComplete() {
      if (it.succeeded()) {
        result.complete(it.result().body().payload as Order)
      }else{
        result.fail(it.cause())
      }
    };

    return result.future();
  }
}
