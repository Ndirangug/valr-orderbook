package com.valr.exchange.orderbook

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.impl.RouterImpl

class OrderBookRouter (vertx: Vertx) : RouterImpl(vertx) {
  private val router: Router = Router.router(vertx)

  init {
    val orderBookController = OrderBookController(OrderBookService(OrderBookRepository(vertx.eventBus())))

    router["/:currencyPair/orderbook"].handler(orderBookController::getOrderBook)
    router["/:currencyPair/tradehistory"].handler(orderBookController::getTradeHistory)
    router.post("/orders/limit").handler(orderBookController::submitLimitOrder)
  }

  fun getOrderBookRouter(): Router {
    return this.router
  }
}
