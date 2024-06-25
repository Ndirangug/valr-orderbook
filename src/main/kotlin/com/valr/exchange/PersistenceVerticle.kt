package com.valr.exchange

import com.valr.exchange.data.AuthStore
import com.valr.exchange.data.OrderbookStore
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus


class PersistenceVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void?>) {
    val eventBus: EventBus = vertx.eventBus()

    AuthStore(eventBus)
    OrderbookStore(eventBus)

    startPromise.complete()
  }
}

