package com.valr.exchange.orderbook
import com.valr.exchange.CurrencyOrderBook
import com.valr.exchange.common.models.LimitOrderRequestModel
import com.valr.exchange.common.models.Order
import io.vertx.core.Future

class OrderBookService(private val repository: OrderBookRepository) {
  fun getOrderBook(currencyPair: String): Future<CurrencyOrderBook> {
    return repository.getOrderBook(currencyPair);
  }

  fun getTradeHistory(currencyPair: String): Future<Void> {
    return Future.future(null);
  }

  fun submitLimitOrder(orderRequest: LimitOrderRequestModel): Future<Order> {
    return repository.saveLimitOrder(orderRequest)
  }
}
