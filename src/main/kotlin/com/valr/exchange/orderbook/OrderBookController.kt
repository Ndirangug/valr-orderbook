package com.valr.exchange.orderbook

import com.valr.exchange.CurrencyOrderBook
import com.valr.exchange.orderbook.models.OrderBookConsumerPayload
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import com.valr.exchange.orderbook.models.OrderBookConsumerPayload.LimitOrderRequest as LimitOrderRequest
import com.valr.exchange.orderbook.models.OrderBookConsumerPayload.Order as Order



class OrderBookController(private val orderBookService: OrderBookService) {
  fun getOrderBook(context: RoutingContext) {
    val currencyPair = context.request().getParam("currencyPair")
    orderBookService.getOrderBook(currencyPair).onComplete {
      if (it.succeeded()) {
        val result = it.result() as CurrencyOrderBook
        if (result != null) {
          context.response().end(Json.encode(result))
        } else {
          context.response().setStatusCode(404).end()
        }
      } else {
        context.response().setStatusCode(500).end(Json.encode(it.cause()))
      }
    }
  }

  fun getTradeHistory(context: RoutingContext) {
    val currencyPair = context.request().getParam("currencyPair")
    orderBookService.getTradeHistory(currencyPair).onComplete {
       if (it.succeeded()) {
        val result = it.result()
        if (result != null) {
          context.response().end(Json.encode(result))
        } else {
          context.response().setStatusCode(404).end()
        }
      } else {
        context.response().setStatusCode(500).end(Json.encode(it.cause()))
      }
    }
  }

  fun submitLimitOrder(context: RoutingContext) {
    val limitOrderRequest = context.body().asJsonObject().mapTo(LimitOrderRequest::class.java)
    orderBookService.submitLimitOrder(limitOrderRequest).onComplete {
       if (it.succeeded()) {
        val result = it.result() as Order
        if (result != null) {
          context.response().setStatusCode(201).end(Json.encode(result))
        } else {
          context.response().setStatusCode(500).end(Json.encode(it.cause()))
        }
      } else {
        context.response().setStatusCode(500).end(Json.encode(it.cause()))
      }
    }
  }
}
