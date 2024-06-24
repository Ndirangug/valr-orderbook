package com.valr.exchange.orderbook

import com.valr.exchange.CurrencyOrderBook
import com.valr.exchange.orderbook.models.LimitOrderRequestModel
import com.valr.exchange.orderbook.models.Order
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

class OrderBookController(private val orderBookService: OrderBookService) {
  fun getOrderBook(context: RoutingContext) {
    val currencyPair = context.request().getParam("currencyPair")
    orderBookService.getOrderBook(currencyPair).onComplete {
      if (it.succeeded()) {
        val result = it.result() as CurrencyOrderBook?
        if (result != null) {
          context.response().end(Json.encode(result))
        } else {
          val response = JsonObject().put("error", "orderbook for $currencyPair not found")
          context.response().setStatusCode(404).end(Json.encode(response))
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
    val limitOrderRequestModel = context.body().asJsonObject().mapTo(LimitOrderRequestModel::class.java)
    orderBookService.submitLimitOrder(limitOrderRequestModel).onComplete {
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
