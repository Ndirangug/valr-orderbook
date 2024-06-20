package com.valr.exchange.orderbook

import io.vertx.core.Future

class OrderBookRepository {
  fun list(): Future<List<String>> {
    return Future.succeededFuture(listOf("1", "2"))
  }
}
