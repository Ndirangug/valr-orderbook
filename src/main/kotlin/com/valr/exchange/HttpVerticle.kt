package com.valr.exchange

import com.valr.exchange.auth.UserRouter
import com.valr.exchange.common.models.EventConsumerMessage
import com.valr.exchange.orderbook.OrderBookRouter
import com.valr.exchange.common.models.EventConsumerMessageCodec
import com.valr.exchange.common.models.EventConsumerPayload
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Promise
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler


class HttpVerticle : AbstractVerticle() {
  companion object {
    val eventConsumerMessageCodec =
      EventConsumerMessageCodec(EventConsumerPayload::class.java)
  }

  override fun start(startPromise: Promise<Void>) {
    // Add codec to eventbus
    vertx.eventBus().registerDefaultCodec(
        EventConsumerMessage::class.java as Class<EventConsumerMessage<EventConsumerPayload>>,
        eventConsumerMessageCodec
      )


    // Setup Routes
    val baseRouter = Router.router(vertx)
    baseRouter.route().handler(BodyHandler.create())

    val userRouter = UserRouter(vertx).getUserRouter()
    val orderBookRouter = OrderBookRouter(vertx).getOrderBookRouter()

    baseRouter.mountSubRouter("/api/v1", userRouter)
    baseRouter.mountSubRouter("/api/v1", orderBookRouter)

    //Setup JWT Auth
//    val jwtAuth = JWTAuth.create(vertx, JWTAuthOptions()
//      .addPubSecKey(
//        PubSecKeyOptions()
//          .setAlgorithm("HS256")
//          .setPublicKey("your-public-key")
//          .setSymmetric(true)
//          .setSecretKey("your-secret-key")))
//
//    orderBookRouter.route().handler(JWTAuthHandler.create(jwtAuth))

    // Start server
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


