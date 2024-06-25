package com.valr.exchange.api

import com.valr.exchange.api.common.EventConsumerMessage
import com.valr.exchange.api.common.EventConsumerMessageCodec
import com.valr.exchange.api.common.EventConsumerPayload
import com.valr.exchange.api.common.utils.Repositories
import com.valr.exchange.api.orderbook.OrderBookRouter
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Promise
import io.vertx.core.http.HttpServer
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.JWTAuthHandler

class HttpVerticle : AbstractVerticle() {
  companion object {
    val eventConsumerMessageCodec =
      EventConsumerMessageCodec(EventConsumerPayload::class.java)

    lateinit var jwtAuth: JWTAuth
  }

  override fun start(startPromise: Promise<Void>) {
    // Add codec to eventbus
    vertx.eventBus().registerDefaultCodec(
      EventConsumerMessage::class.java as Class<EventConsumerMessage<EventConsumerPayload>>,
      eventConsumerMessageCodec
    )

    // Initialzie repositories
    Repositories(vertx);

    //Setup JWT Auth
    val secretKey = System.getenv("JWT_SECRET") ?: throw IllegalStateException("JWT_SECRET environment variable not set")
    jwtAuth = JWTAuth.create(
      vertx, JWTAuthOptions()
        .addPubSecKey(
          PubSecKeyOptions()
            .setAlgorithm("HS256")
            .setBuffer(secretKey)
        )
    );
    val jwtAuthHandler = JWTAuthHandler.create(jwtAuth)

    // Setup Routes
    val baseRouter = Router.router(vertx)
    baseRouter.route().handler(BodyHandler.create())

    val userRouter = com.valr.exchange.api.auth.UserRouter(vertx).getUserRouter()
    val orderBookRouter = OrderBookRouter(vertx, jwtAuthHandler).getOrderBookRouter()

    baseRouter.mountSubRouter("/api/v1", userRouter)
    baseRouter.mountSubRouter("/api/v1", orderBookRouter)

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


