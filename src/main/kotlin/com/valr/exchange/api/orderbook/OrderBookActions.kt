package com.valr.exchange.api.orderbook

import com.valr.exchange.api.common.EventActions

class OrderBookActions: EventActions() {
  companion object{
    val save = "save";
    val fetch_orderbook = "fetch_orderbook";
    val fetch_orderhistory = "fetch_orderhistory";
  }
}
