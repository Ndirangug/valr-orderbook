package com.valr.exchange

import com.valr.exchange.orderbook.models.OrderBookConsumerPayload.Order as Order
import com.valr.exchange.orderbook.models.OrderBookConsumerPayload
import com.valr.exchange.orderbook.models.OrderSide
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap


typealias OrderBooksStore = HashMap<String, HashMap<String, Any>>
typealias OrdersList = MutableList<Order>

enum class EventBusAddress {
  orderbook_consumer
}

enum class OrderBookActions {
  save,
  return_saved
}

data class OrderBookConsumerMessage<T>(val action: OrderBookActions, val payload: T)


class PersistenceVerticle : AbstractVerticle() {
  val orderbook: OrderBooksStore = hashMapOf()


  override fun start(startPromise: Promise<Void?>) {
    val eventBus: EventBus = vertx.eventBus()

    val orderbookConsumer =
      eventBus.consumer<OrderBookConsumerMessage<OrderBookConsumerPayload>>(EventBusAddress.orderbook_consumer.name)

    orderbookConsumer.handler { message ->
      run {
        when (message.body().action) {
          OrderBookActions.save -> {
            val orderRequest = message.body().payload as OrderBookConsumerPayload.LimitOrderRequest
            val newOrder = Order(
              id = generateUUID(),
              side = OrderSide.fromName(orderRequest.side),
              quantity = orderRequest.quantity,
              price = orderRequest.price,
              currencyPair = orderRequest.pair,
              orderCount = 0,
              isOpen = true,
              userId = "",
              createdAt = Instant.now().toEpochMilli(),
              updatedAt = Instant.now().toEpochMilli()
            );

            addOrderToMap(
              currencyPair = orderRequest.pair,
              newOrder = newOrder
            )

            message.reply(OrderBookConsumerMessage(action = OrderBookActions.return_saved, newOrder))
          }

          else -> {}
        }
      }
    }

    startPromise.complete()
  }


  fun addOrderToSortedList(sortedList: MutableList<Order>, newOrder: Order) {
    val index = sortedList.binarySearch { o -> -o.price.compareTo(newOrder.price) }
    val insertIndex = if (index < 0) -index - 1 else index
    sortedList.add(insertIndex, newOrder)
  }

  fun addOrderToMap(currencyPair: String, newOrder: Order) {
    orderbook.putIfAbsent(currencyPair, hashMapOf("Asks" to mutableListOf<Order>(), "Bids" to mutableListOf<Order>()))
    val key = if (newOrder.side == OrderSide.BUY) "Bids" else "Asks";
    val orderList = orderbook[currencyPair]?.get(key) as OrdersList
    addOrderToSortedList(orderList, newOrder)
  }

  fun generateUUID(): String {
    return UUID.randomUUID().toString()
  }
}

