package com.valr.exchange.orderbook

import io.vertx.ext.web.RoutingContext

class OrderBookController(private val orderBookService: OrderBookService) {
  fun listOrders(context: RoutingContext) {
    orderBookService.listOrders().onComplete {
      if (it.succeeded()) {
        val orderBook = it.result()
        if (orderBook != null) {
          context.response().end(orderBook.toString())
        } else {
          context.response().setStatusCode(404).end()
        }
      } else {
        context.response().setStatusCode(500).end()
      }
    }
  }
}
