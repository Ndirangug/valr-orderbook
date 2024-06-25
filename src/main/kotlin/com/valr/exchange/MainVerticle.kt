package com.valr.exchange


import com.valr.exchange.api.HttpVerticle
import io.vertx.core.*

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void?>) {
    Future.all(
      deployVerticle(HttpVerticle::class.java.name),
      deployVerticle(PersistenceVerticle::class.java.name),
    ).onComplete {
      if (it.succeeded()) {
        startPromise.complete()
      } else {
        startPromise.fail(it.cause())
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
