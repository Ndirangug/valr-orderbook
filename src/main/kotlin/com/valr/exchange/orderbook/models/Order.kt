package com.valr.exchange.orderbook.models

import java.util.Date

enum class OrderSide {
  BUY,
  SELL
}

enum class AskBid {
  ASK,
  BID
}

data class Order(
  val id: String,
  val side: OrderSide,
  val quantity: Int,
  val price: Double,
  val currencyPair: String,
  val orderCount: Int,
  var isOpen: Boolean,
  val userId: String,
  val createdAt: Date,
  val updatedAt: Date
)
