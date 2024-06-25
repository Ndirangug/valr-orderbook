package com.valr.exchange.api.orderbook.models

data class OrderBookEntryResponseMddel(
  val side: String,
  val quantity: Double,
  val price: Double,
  val currencyPair: String,
  val orderCount: Int
) {
  companion object {
    fun fromOrder(order: Order): OrderBookEntryResponseMddel {
      return OrderBookEntryResponseMddel(
        side = order.side.name,
        quantity = order.quantity,
        price = order.price,
        currencyPair = order.currencyPair,
        orderCount = order.orderCount
      )
    }
  }
}
