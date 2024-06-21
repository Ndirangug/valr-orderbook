package com.valr.exchange.orderbook
import com.valr.exchange.CurrencyOrderBook
import com.valr.exchange.orderbook.models.OrderBookConsumerPayload
import io.vertx.core.Future
import io.vertx.core.Promise

class OrderBookService(private val repository: OrderBookRepository) {
  fun getOrderBook(currencyPair: String): Future<CurrencyOrderBook> {
    return repository.getOrderBook(currencyPair);
  }

  fun getTradeHistory(currencyPair: String): Future<Void> {
    return Future.future(null);
  }

  fun submitLimitOrder(orderRequest: OrderBookConsumerPayload.LimitOrderRequest): Future<OrderBookConsumerPayload.Order> {
    return repository.saveLimitOrder(orderRequest)
  }
}
