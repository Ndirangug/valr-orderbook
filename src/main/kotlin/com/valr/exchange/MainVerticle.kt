package com.valr.exchange

import io.vertx.core.*

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void?>) {
    vertx.deployVerticle(HttpVerticle::class.java.name) { event: AsyncResult<String?> ->
      if (event.succeeded()) {
        startPromise.complete()
      } else {
        startPromise.fail(event.cause())
      }
    }
  }

  fun deployVerticle(verticleName: String?): Future<Void> {
    val asyncResult = Promise.promise<Void>()
    vertx.deployVerticle(verticleName) { event: AsyncResult<String?> ->
      if (event.succeeded()) {
        asyncResult.complete()
      } else {
        asyncResult.fail(event.cause())
      }
    }
    return asyncResult.future();
  }
}
