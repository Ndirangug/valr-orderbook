package com.valr.exchange.api.orderbook
import com.valr.exchange.api.orderbook.models.LimitOrderRequestModel
import com.valr.exchange.api.orderbook.models.OrderBookEntryResponseMddel
import com.valr.exchange.api.orderbook.models.OrderBookResponseModel
import com.valr.exchange.api.orderbook.models.TradeHistoryItemResponseModel
import com.valr.exchange.data.CurrencyOrderBook
import io.vertx.core.Future
import io.vertx.core.Promise

class OrderBookService(private val repository: OrderBookRepository) {
  fun getOrderBook(currencyPair: String): Future<OrderBookResponseModel> {
    val promise = Promise.promise<OrderBookResponseModel>();

    repository.getOrderBook(currencyPair).onComplete(){ ar ->
      if (ar.succeeded()) {
        promise.complete(OrderBookResponseModel.fromCurrencyOrderBook(ar.result()));
      }else{
        promise.fail(ar.cause())
      }
    }

    return promise.future();
  }

  fun getTradeHistory(userId: String, currencyPair: String): Future<List<TradeHistoryItemResponseModel>> {
    val promise = Promise.promise<List<TradeHistoryItemResponseModel>>();

    repository.getOrderHistory(userId, currencyPair).onComplete(){ ar ->
      if (ar.succeeded()) {
        val result = ar.result().map { TradeHistoryItemResponseModel.fromOrder(it) };
        promise.complete(result)
      }else{
        promise.fail(ar.cause())
      }
    };

    return promise.future();
  }

  fun submitLimitOrder(orderRequest: LimitOrderRequestModel): Future<OrderBookEntryResponseMddel> {
    val promise = Promise.promise<OrderBookEntryResponseMddel>();

     repository.saveLimitOrder(orderRequest).onComplete(){
        if (it.succeeded()){
          promise.complete(OrderBookEntryResponseMddel.fromOrder(it.result()))
        }else{
         promise.fail(it.cause())
        }
    }

    return promise.future();
  }
}
