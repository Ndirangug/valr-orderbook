package com.valr.exchange.orderbook

import com.valr.exchange.orderbook.models.LimitOrderRequest
import io.vertx.ext.web.RoutingContext

class OrderBookController(private val orderBookService: OrderBookService) {
  fun getOrderBook(context: RoutingContext) {
    val currencyPair = context.request().getParam("currencyPair")
    println("Currency pair: $currencyPair")
    orderBookService.getOrderBook(currencyPair).onComplete {
      if (it.succeeded()) {
        val result = it.result()
        if (result != null) {
          context.response().end(result.toString())
        } else {
          context.response().setStatusCode(404).end()
        }
      } else {
        context.response().setStatusCode(500).end()
      }
    }
  }

  fun submitLimitOrder(context: RoutingContext) {
    val limitOrderRequest = context.body().asJsonObject().mapTo(LimitOrderRequest::class.java)
    orderBookService.submitLimitOrder(limitOrderRequest).onComplete {
       if (it.succeeded()) {
        val result = it.result()
        if (result != null) {
          context.response().end(result.toString())
        } else {
          context.response().setStatusCode(404).end()
        }
      } else {
        context.response().setStatusCode(500).end()
      }
    }
  }

  fun getTradeHistory(context: RoutingContext) {
    val currencyPair = context.request().getParam("currencyPair")
    orderBookService.getTradeHistory(currencyPair).onComplete {
       if (it.succeeded()) {
        val result = it.result()
        if (result != null) {
          context.response().end(result.toString())
        } else {
          context.response().setStatusCode(404).end()
        }
      } else {
        context.response().setStatusCode(500).end()
      }
    }
  }
}
