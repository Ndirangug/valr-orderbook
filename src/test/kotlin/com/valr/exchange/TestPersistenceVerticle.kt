package com.valr.exchange

import com.valr.exchange.orderbook.OrderBookRepository
import com.valr.exchange.common.models.EventConsumerPayload
import com.valr.exchange.common.models.LimitOrderRequestModel
import com.valr.exchange.common.models.Order
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class TestPersistenceVerticle {
  private lateinit var vertx: Vertx
  private lateinit var orderBookRepository: OrderBookRepository

  @BeforeEach
  fun setUp(vertx: Vertx, testContext: VertxTestContext): Unit {
    this.vertx = vertx
    vertx.deployVerticle(PersistenceVerticle(), testContext.succeeding {
      vertx.eventBus().registerDefaultCodec(
        OrderBookConsumerMessage::class.java as Class<OrderBookConsumerMessage<EventConsumerPayload>>,
        HttpVerticle.eventConsumerMessageCodec
      )
      orderBookRepository = OrderBookRepository(vertx.eventBus())
      testContext.completeNow()
    })
  }

  @Test
  fun `test adding an order returns a sorted list in descending order according to amount`(
    vertx: Vertx, testContext: VertxTestContext
  ) {
    val orderRequest1 = LimitOrderRequestModel("BUY", 1.0, 100.0, "BTCWLD", false, "", "")
    val orderRequest2 = LimitOrderRequestModel("BUY", 1.0, 200.0, "BTCWLD", false, "", "")
    val orderRequest3 = LimitOrderRequestModel("BUY", 1.0, 150.0, "BTCWLD", false, "", "")

    sendOrder(orderRequest1)
    sendOrder(orderRequest2)
    sendOrder(orderRequest3)

    fetchOrderBook("BTCWLD").onComplete() {
      testContext.verify {
        val orderbook = it.result();
        val bids = orderbook["Bids"] as OrdersList
        assertEquals(3, bids.size)
        assertTrue(bids[0].price > bids[1].price && bids[1].price > bids[2].price)
        testContext.completeNow()
      }
    }
  }

  @Test
  fun `test that BUY orders are saved under 'Bids' and SELL orders under 'Asks'`(
    vertx: Vertx, testContext: VertxTestContext
  ) {
    val buyOrderRequest = LimitOrderRequestModel("BUY", 1.0, 50.0, "BTCWLD", false, "", "")
    val sellOrderRequest = LimitOrderRequestModel("SELL", 1.0, 100.0, "BTCWLD", false, "", "")

    sendOrder(buyOrderRequest)
    sendOrder(sellOrderRequest)

    fetchOrderBook("BTCWLD").onComplete() {
      testContext.verify {
        val orderBook = it.result()
        val bids = orderBook["Bids"] as OrdersList
        val asks = orderBook["Asks"] as OrdersList
        assertEquals(1, bids.size)
        assertEquals(1, asks.size)
        assertEquals(buyOrderRequest.price, bids[0].price)
        assertEquals(sellOrderRequest.price, asks[0].price)
        testContext.completeNow()
      }
    }
  }

  @Test
  fun `test matching buy order with existing sell order`(vertx: Vertx, testContext: VertxTestContext) {
    val sellOrderRequest = LimitOrderRequestModel("SELL", 1.0, 100.0, "BTCWLD", false, "", "")
    sendOrder(sellOrderRequest)

    val buyOrderRequest = LimitOrderRequestModel("BUY", 1.0, 100.0, "BTCWLD", false, "", "")
    sendOrder(buyOrderRequest)

    fetchOrderBook("BTCWLD").onComplete() {
      testContext.verify {
        val orderBook = it.result()
        val bids = orderBook["Bids"] as OrdersList
        val asks = orderBook["Asks"] as OrdersList
        assertTrue(bids.isEmpty() && asks.isEmpty())
        testContext.completeNow()
      }
    }
  }

  @Test
  fun `test matching sell order with existing buy order`(vertx: Vertx, testContext: VertxTestContext) {
    val buyOrderRequest = LimitOrderRequestModel("BUY", 1.0, 100.0, "BTCWLD", false, "", "")
    sendOrder(buyOrderRequest)

    val sellOrderRequest = LimitOrderRequestModel("SELL", 1.0, 100.0, "BTCWLD", false, "", "")
    sendOrder(sellOrderRequest)

    fetchOrderBook("BTCWLD").onComplete() {
      testContext.verify {
        val orderBook = it.result()
        val bids = orderBook["Bids"] as OrdersList
        val asks = orderBook["Asks"] as OrdersList
        assertTrue(bids.isEmpty() && asks.isEmpty())
        testContext.completeNow()
      }
    }
  }

  @Test
  fun `test matching sell order with multiple  buy orders`(vertx: Vertx, testContext: VertxTestContext) {
    val buyOrderRequest1 = LimitOrderRequestModel("BUY", 1.0, 100.0, "BTCWLD", false, "", "")
    val buyOrderRequest2 = LimitOrderRequestModel("BUY", 3.0, 100.0, "BTCWLD", false, "", "")
    val buyOrderRequest3 = LimitOrderRequestModel("BUY", 2.0, 100.0, "BTCWLD", false, "", "")
    sendOrder(buyOrderRequest1)
    sendOrder(buyOrderRequest2)
    sendOrder(buyOrderRequest3)

    val sellOrderRequest = LimitOrderRequestModel("SELL", 4.0, 100.0, "BTCWLD", false, "", "")
    sendOrder(sellOrderRequest)

    fetchOrderBook("BTCWLD").onComplete() {
      testContext.verify {
        val orderBook = it.result()
        val bids = orderBook["Bids"] as OrdersList
        val asks = orderBook["Asks"] as OrdersList

        assertTrue(bids.isNotEmpty())
        assertTrue(asks.isEmpty())
        assertEquals(2.0, bids.getOpenOrdersQuantity())
        testContext.completeNow()
      }
    }
  }

  @Test
  fun `test matching buy order with multiple  sell orders`(vertx: Vertx, testContext: VertxTestContext) {
    val sellOrderRequest1 = LimitOrderRequestModel("SELL", 1.0, 100.0, "BTCWLD", false, "", "")
    val sellOrderRequest2 = LimitOrderRequestModel("SELL", 3.0, 100.0, "BTCWLD", false, "", "")
    val sellOrderRequest3 = LimitOrderRequestModel("SELL", 2.0, 100.0, "BTCWLD", false, "", "")
    sendOrder(sellOrderRequest1)
    sendOrder(sellOrderRequest2)
    sendOrder(sellOrderRequest3)

    val buyOrderRequest = LimitOrderRequestModel("BUY", 4.0, 100.0, "BTCWLD", false, "", "")
    sendOrder(buyOrderRequest)

    fetchOrderBook("BTCWLD").onComplete() {
      testContext.verify {
        val orderBook = it.result()
        val bids = orderBook["Bids"] as OrdersList
        val asks = orderBook["Asks"] as OrdersList
        assertTrue(bids.isEmpty())
        assertTrue(asks.isNotEmpty())
        assertEquals(2.0, asks.getOpenOrdersQuantity())
        testContext.completeNow()
      }
    }
  }

  @Test
  fun `test order is closed when quantity is zero`(vertx: Vertx, testContext: VertxTestContext) {
    val sellOrderRequest = LimitOrderRequestModel("SELL", 1.0, 100.0, "BTCWLD", false, "", "")
    sendOrder(sellOrderRequest)

    val buyOrderRequest = LimitOrderRequestModel("BUY", 1.0, 100.0, "BTCWLD", false, "", "")
    sendOrder(buyOrderRequest)

    fetchOrderBook("BTCWLD").onComplete() {
      testContext.verify {
        val orderBook = it.result()
        val bids = orderBook["Bids"] as OrdersList
        val asks = orderBook["Asks"] as OrdersList
        assertTrue(bids.isEmpty() && asks.isEmpty())
        testContext.completeNow()
      }
    }
  }

  @Test
  fun `test fetching orders returns only open orders`(vertx: Vertx, testContext: VertxTestContext) {
    val buyOrderRequest = LimitOrderRequestModel("BUY", 1.0, 100.0, "BTCWLD", false, "", "")
    sendOrder(buyOrderRequest)

    val partialSellOrderRequest = LimitOrderRequestModel("SELL", 0.5, 100.0, "BTCWLD", false, "", "")
    sendOrder(partialSellOrderRequest)

    fetchOrderBook("BTCWLD").onComplete() {
      testContext.verify {
        val orderBook = it.result()
        val bids = orderBook["Bids"] as OrdersList
        val asks = orderBook["Asks"] as OrdersList
        assertTrue(bids.isNotEmpty())
        assertTrue(asks.isEmpty())
        assertEquals(0.5, bids.getOpenOrdersQuantity())
        assertEquals(true, bids[0].isOpen)
        testContext.completeNow()
      }
    }
  }

  private fun sendOrder(orderRequest: LimitOrderRequestModel): Future<Order> {
    return orderBookRepository.saveLimitOrder(orderRequest)
  }

  private fun fetchOrderBook(currencyPair: String): Future<CurrencyOrderBook> {
    return orderBookRepository.getOrderBook(currencyPair)
  }

  private fun OrdersList.getOpenOrdersQuantity(): Double {
    return this.filter { it.isOpen }
      .sumOf { it.quantity }
  }
}
