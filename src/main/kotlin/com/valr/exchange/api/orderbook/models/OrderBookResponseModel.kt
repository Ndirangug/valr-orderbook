package com.valr.exchange.api.orderbook.models

import com.valr.exchange.data.CurrencyOrderBook
import com.valr.exchange.data.OrdersList
import java.time.Instant
import java.time.format.DateTimeFormatter

data class OrderBookResponseModel(
  val asks: List<OrderBookEntryResponseMddel>,
  val bids: List<OrderBookEntryResponseMddel>,
  val lastChange: String,
  val sequenceNumber: Long
) {
  companion object {
    fun fromCurrencyOrderBook(orderbook: CurrencyOrderBook): OrderBookResponseModel {
      val asks = (orderbook["Asks"] as OrdersList).map { OrderBookEntryResponseMddel.fromOrder(it) }
      val bids = (orderbook["Bids"] as OrdersList).map { OrderBookEntryResponseMddel.fromOrder(it) }

      return OrderBookResponseModel(
        asks = asks,
        bids = bids,
        lastChange = DateTimeFormatter.ISO_INSTANT.format(
          Instant.ofEpochMilli(orderbook["LastChange"] as Long)
        ),
        sequenceNumber = orderbook["SequenceNumber"] as Long
      )
    }
  }
}
