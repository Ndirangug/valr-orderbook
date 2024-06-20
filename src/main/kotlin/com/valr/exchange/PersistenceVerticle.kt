package com.valr.exchange

import com.valr.exchange.orderbook.models.AskBid
import com.valr.exchange.orderbook.models.Order
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject

class PersistenceVerticle : AbstractVerticle() {
  val orderbook: HashMap<String, HashMap<AskBid, List<Order>>> = hashMapOf()
  val eventBus: EventBus = vertx.eventBus()
  val orderbookConsumer = eventBus.consumer<JsonObject>("orderbook-consumer")

  override fun start(startPromise: Promise<Void?>) {
    orderbookConsumer.handler { message ->
      {

      }
    }

    startPromise.complete()
  }
}
