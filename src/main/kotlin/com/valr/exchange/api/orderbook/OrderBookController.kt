package com.valr.exchange.api.orderbook

import com.valr.exchange.api.common.exceptions.NotFoundException
import com.valr.exchange.api.common.utils.getCurrentUser
import com.valr.exchange.api.orderbook.models.LimitOrderRequestModel
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext


class OrderBookController(private val orderBookService: OrderBookService) {
  fun getOrderBook(context: RoutingContext) {
    val currencyPair = context.request().getParam("currencyPair")
    orderBookService.getOrderBook(currencyPair).onComplete {
      if (it.succeeded()) {
        val result = it.result()
        if (result != null) {
          context.response().setStatusCode(HttpResponseStatus.OK.code()).end(Json.encode(result))
        }
      } else if (it.cause() is NotFoundException) {
        context.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
          .end(Json.encode("orderbook for $currencyPair not found"))
      } else {
        context.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(Json.encode(it.cause()))
      }

    }
  }

  fun getTradeHistory(context: RoutingContext) {
    val currencyPair = context.request().getParam("currencyPair")

    getCurrentUser(context).onComplete {
      val user = it.result()
      if (user == null) {
        context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end()
      }

      orderBookService.getTradeHistory(user!!.id, currencyPair).onComplete {
        if (it.succeeded()) {
          val result = it.result()
          if (result != null) {
            context.response().setStatusCode(HttpResponseStatus.OK.code()).end(Json.encode(result))
          }
        } else if (it.cause() is NotFoundException) {
          context.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
            .end(Json.encode("orderbook for $currencyPair not found"))
        } else {
          context.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(Json.encode(it.cause()))
        }
      }

    }
  }

  fun submitLimitOrder(context: RoutingContext) {
    var limitOrderRequestModel = context.body().asJsonObject().mapTo(LimitOrderRequestModel::class.java)

    getCurrentUser(context).onComplete { ar ->
      val user = ar.result()
      if (user == null) {
        context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end()
      }

      limitOrderRequestModel.userId = user!!.id

      orderBookService.submitLimitOrder(limitOrderRequestModel).onComplete {
        if (it.succeeded()) {
          val result = it.result()
          if (result != null) {
            context.response().setStatusCode(HttpResponseStatus.ACCEPTED.code()).end(Json.encode(result))
          } else {
            context.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
              .end(Json.encode(it.cause()))
          }
        } else {
          context.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(Json.encode(it.cause()))
        }
      }
    }
  }
}
