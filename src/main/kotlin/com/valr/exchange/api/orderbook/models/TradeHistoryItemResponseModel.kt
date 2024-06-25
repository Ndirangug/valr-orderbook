package com.valr.exchange.api.orderbook.models

import java.time.Instant
import java.time.format.DateTimeFormatter

data class TradeHistoryItemResponseModel(
  val quantity: Double,
  val price: Double,
  val currencyPair: String,
  val tradedAt: String,
  val takerSide: String,
  val sequenceId: Long,
  val id: String,
  val quoteVolume: Double
) {
  companion object {
    fun fromOrder(order: Order): TradeHistoryItemResponseModel {
      return TradeHistoryItemResponseModel(
        takerSide = order.takerSide?.name ?: "",
        quantity = order.quantity,
        price = order.price,
        currencyPair = order.currencyPair,
        tradedAt = DateTimeFormatter.ISO_INSTANT.format(
          Instant.ofEpochMilli(order.updatedAt)
        ),
        sequenceId = order.sequenceNumber,
        id = order.id,
        quoteVolume = order.price * order.quantity
      )
    }
  }
}
