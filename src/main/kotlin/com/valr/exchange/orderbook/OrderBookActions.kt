package com.valr.exchange.orderbook

import com.valr.exchange.common.models.EventActions

class OrderBookActions: EventActions() {
  companion object{
    val save = "save";
    val fetch_orderbook = "fetch_orderbook";
    val fetch_orderhistory = "fetch_orderhistory";
  }
}
