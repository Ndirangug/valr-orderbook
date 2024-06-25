package com.valr.exchange.api.common.utils

import com.valr.exchange.api.auth.UserRepository
import com.valr.exchange.api.orderbook.OrderBookRepository
import io.vertx.core.Vertx

class Repositories(val vertx: Vertx) {
  companion object {
    lateinit var userRepository: UserRepository;
    lateinit var orderBookRepository: OrderBookRepository;
  }

  init {
      userRepository = UserRepository(vertx.eventBus())
      orderBookRepository = OrderBookRepository(vertx.eventBus())
  }
}
