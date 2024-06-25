package com.valr.exchange.api.orderbook
import com.valr.exchange.api.orderbook.models.LimitOrderRequestModel
import com.valr.exchange.data.CurrencyOrderBook
import io.vertx.core.Future

class OrderBookService(private val repository: OrderBookRepository) {
  fun getOrderBook(currencyPair: String): Future<CurrencyOrderBook> {
    return repository.getOrderBook(currencyPair);
  }

  fun getTradeHistory(userId: String, currencyPair: String): Future<List<com.valr.exchange.api.orderbook.models.Order>> {
    return repository.getOrderHistory(userId, currencyPair);
  }

  fun submitLimitOrder(orderRequest: LimitOrderRequestModel): Future<com.valr.exchange.api.orderbook.models.Order> {
    return repository.saveLimitOrder(orderRequest)
  }
}
