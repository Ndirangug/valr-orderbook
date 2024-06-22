package com.valr.exchange

import com.valr.exchange.auth.UserRouter
import com.valr.exchange.orderbook.OrderBookRouter
import com.valr.exchange.orderbook.models.OrderBookConsumerMessageCodec
import com.valr.exchange.orderbook.models.OrderBookConsumerPayload
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Promise
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler


class HttpVerticle : AbstractVerticle() {
  companion object {
    val orderBookConsumerMessageCodec =
      OrderBookConsumerMessageCodec<OrderBookConsumerPayload>(OrderBookConsumerPayload::class.java)
  }

  override fun start(startPromise: Promise<Void>) {
    vertx.eventBus().registerDefaultCodec(
        OrderBookConsumerMessage::class.java as Class<OrderBookConsumerMessage<OrderBookConsumerPayload>>,
        orderBookConsumerMessageCodec
      )

    val baseRouter = Router.router(vertx)
    baseRouter.route().handler(BodyHandler.create())

    val userRouter = UserRouter(vertx).getUserRouter()
    val orderBookRouter = OrderBookRouter(vertx).getOrderBookRouter()

    baseRouter.mountSubRouter("/api/v1/user", userRouter)
    baseRouter.mountSubRouter("/api/v1", orderBookRouter)

    vertx.createHttpServer().requestHandler(baseRouter).listen(8000) { ar: AsyncResult<HttpServer?> ->
      if (ar.succeeded()) {
        println("HTTP server started on port ${ar.result()?.actualPort()}")
        startPromise.complete()
      } else {
        startPromise.fail(ar.cause())
      }
    }
  }
}


