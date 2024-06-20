package com.valr.exchange.orderbook

import com.valr.exchange.orderbook.models.LimitOrderRequest
import io.vertx.core.Future

class OrderBookService(private val repository: OrderBookRepository) {
  fun getOrderBook(currencyPair: String): Future<Void> {
    return Future.future(null);
  }

  fun getTradeHistory(currencyPair: String): Future<Void> {
    return Future.future(null);
  }

  fun submitLimitOrder(orderRequest: LimitOrderRequest): Future<Void> {
    return Future.future(null);
  }
}
