package com.valr.exchange.orderbook.models

import com.valr.exchange.common.models.EventConsumerPayload

enum class OrderSide {
  BUY,
  SELL;

  companion object {
    fun fromName(side: String): OrderSide {
      return OrderSide.valueOf(side.uppercase())
    }
  }
}

data class Order(
  val id: String,
  val side: OrderSide,
  var quantity: Double,
  val price: Double,
  val currencyPair: String,
  val orderCount: Int,
  val userId: String,
  val createdAt: Long,
  val updatedAt: Long,
  val sequenceNumber: Long,
) : EventConsumerPayload() {
  val isOpen: Boolean
    get() = quantity > 0.0
}
