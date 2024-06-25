package com.valr.exchange.api.orderbook.models

import com.valr.exchange.api.common.EventConsumerPayload

enum class OrderSide {
  BUY,
  SELL;

  companion object {
    fun fromName(side: String): com.valr.exchange.api.orderbook.models.OrderSide {
      return com.valr.exchange.api.orderbook.models.OrderSide.valueOf(side.uppercase())
    }
  }
}

data class Order(
    val id: String,
    val side: com.valr.exchange.api.orderbook.models.OrderSide,
    var quantity: Double,
    val price: Double,
    val currencyPair: String,
    val orderCount: Int,
    val userId: String?,
    val createdAt: Long,
    var updatedAt: Long,
    val sequenceNumber: Long,
) : EventConsumerPayload() {
  val isOpen: Boolean
    get() = quantity > 0.0
}
