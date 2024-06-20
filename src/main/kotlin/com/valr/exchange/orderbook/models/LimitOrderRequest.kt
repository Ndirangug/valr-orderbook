package com.valr.exchange.orderbook.models

data class LimitOrderRequest(
  val side: String,
  val quantiy: Double,
  val price: Double,
  val pair: String,
  val postOnly: Boolean,
  val customerOrderId: String,
  val timeInForce: String
)
