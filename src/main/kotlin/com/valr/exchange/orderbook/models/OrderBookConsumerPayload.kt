package com.valr.exchange.orderbook.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

enum class OrderSide {
  BUY,
  SELL;

  companion object {
    fun fromName(side: String): OrderSide {
      return OrderSide.valueOf(side.uppercase())
    }
  }
}

sealed class OrderBookConsumerPayload {
  data class LimitOrderRequest @JsonCreator constructor(
    @JsonProperty("side") val side: String,
    @JsonProperty("quantity") val quantity: Double,
    @JsonProperty("price") val price: Double,
    @JsonProperty("pair") val pair: String,
    @JsonProperty("postOnly") val postOnly: Boolean,
    @JsonProperty("customerOrderId") val customerOrderId: String,
    @JsonProperty("timeInForce") val timeInForce: String
) : OrderBookConsumerPayload()



  data class Order(
    val id: String,
    val side: OrderSide,
    val quantity: Double,
    val price: Double,
    val currencyPair: String,
    val orderCount: Int,
    var isOpen: Boolean,
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long
):OrderBookConsumerPayload()
}

