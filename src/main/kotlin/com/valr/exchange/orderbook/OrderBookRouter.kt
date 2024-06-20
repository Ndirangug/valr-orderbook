package com.valr.exchange.orderbook

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.impl.RouterImpl

class OrderBookRouter (vertx: Vertx?) : RouterImpl(vertx) {
  val router: Router = Router.router(vertx)

  init {
    val orderBookController = OrderBookController(OrderBookService(OrderBookRepository()))

    router["/"].handler(orderBookController::listOrders)
  }

  fun getOrderBookRouter(): Router {
    return this.router
  }
}
