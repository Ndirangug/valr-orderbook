package com.valr.exchange.orderbook

import io.vertx.core.Future

class OrderBookService(private val repository: OrderBookRepository) {
  fun listOrders(): Future<List<String>> {
    return repository.list();
  }
}
