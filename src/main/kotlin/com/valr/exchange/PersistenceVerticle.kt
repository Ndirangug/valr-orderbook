package com.valr.exchange

import com.valr.exchange.auth.UserActions
import com.valr.exchange.auth.models.User
import com.valr.exchange.auth.models.UserRequestModel
import com.valr.exchange.common.models.*
import com.valr.exchange.orderbook.OrderBookActions
import com.valr.exchange.orderbook.models.LimitOrderRequestModel
import com.valr.exchange.orderbook.models.Order
import com.valr.exchange.orderbook.models.OrderSide
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap


typealias OrderBooksStore = HashMap<String, HashMap<String, Any>>
typealias OrdersList = MutableList<Order>
typealias CurrencyOrderBook = HashMap<String, Any>

enum class EventBusAddress {
  orderbook_consumer,
  auth_consumer
}

class PersistenceVerticle : AbstractVerticle() {
  val users: MutableList<User> = mutableListOf()
  val orderbook: OrderBooksStore = hashMapOf()
  var sequenceIdAccumulator: Long = 0;

  override fun start(startPromise: Promise<Void?>) {
    val eventBus: EventBus = vertx.eventBus()

    setupAuthConsumer(eventBus)
    setupOrderBookConsumer(eventBus)

    startPromise.complete()
  }

  private fun setupAuthConsumer(eventBus: EventBus) {
    val authConsumer =
      eventBus.consumer<EventConsumerMessage<EventConsumerPayload>>(EventBusAddress.auth_consumer.name)

    authConsumer.handler { message ->
      run {
        when (message.body().action) {
          UserActions.signup -> {
            val userRequest = message.body().payload as UserRequestModel
            val newUser = User(
              id = generateUUID(),
              username = userRequest.username,
              password = userRequest.password,
            );

            users.add(newUser)

            message.reply(EventConsumerMessage(action = UserActions.signup, newUser))
          }

          UserActions.login -> {
            val userRequest = message.body().payload as UserRequestModel
            val userExists =
              users.any { user -> user.username == userRequest.username && user.password == userRequest.password }
            message.reply(EventConsumerMessage(action = UserActions.login, userExists))
          }

          UserActions.fetch_users -> {
            message.reply(EventConsumerMessage(action = UserActions.fetch_users, users))
          }

          else -> {}
        }
      }
    }
  }

  private fun setupOrderBookConsumer(eventBus: EventBus) {
    val orderbookConsumer =
      eventBus.consumer<EventConsumerMessage<EventConsumerPayload>>(EventBusAddress.orderbook_consumer.name)

    orderbookConsumer.handler { message ->
      run {
        when (message.body().action) {
          OrderBookActions.save -> {
            val orderRequest = message.body().payload as LimitOrderRequestModel
            val newOrder = Order(
              id = generateUUID(),
              side = OrderSide.fromName(orderRequest.side),
              quantity = orderRequest.quantity,
              price = orderRequest.price,
              currencyPair = orderRequest.pair,
              orderCount = 0,
              userId = "",
              createdAt = Instant.now().toEpochMilli(),
              updatedAt = Instant.now().toEpochMilli(),
              sequenceNumber = ++sequenceIdAccumulator,
            );

            addOrderToMap(
              currencyPair = orderRequest.pair,
              newOrder = newOrder
            )

            message.reply(EventConsumerMessage(action = OrderBookActions.save, newOrder))

            matchOrder(newOrder)
          }

          OrderBookActions.fetch_orderbook -> {
            val currencyPair = message.body().payload as String
            val currencyOrderBook = orderbook[currencyPair]
            val filteredOrderBook =
              hashMapOf<String, Any>(
                "Asks" to (currencyOrderBook?.get("Asks") as OrdersList).filter { it -> it.isOpen },
                "Bids" to (currencyOrderBook?.get("Bids") as OrdersList).filter { it -> it.isOpen },
                "SequenceNumber" to currencyOrderBook?.get("SequenceNumber") as Long,
                "LastChange" to currencyOrderBook?.get("LastChange") as Long,
              )

            message.reply(EventConsumerMessage(action = OrderBookActions.fetch_orderbook, filteredOrderBook))
          }

          else -> {}
        }
      }
    }
  }


  private fun addOrderToSortedList(sortedList: MutableList<Order>, newOrder: Order) {
    val index = sortedList.binarySearch { o -> -o.price.compareTo(newOrder.price) }
    val insertIndex = if (index < 0) -index - 1 else index
    sortedList.add(insertIndex, newOrder)
  }

  private fun addOrderToMap(currencyPair: String, newOrder: Order) {
    orderbook.putIfAbsent(currencyPair, hashMapOf("Asks" to mutableListOf<Order>(), "Bids" to mutableListOf<Order>()))
    val key = if (newOrder.side == OrderSide.BUY) "Bids" else "Asks";
    val orderList = orderbook[currencyPair]?.get(key) as OrdersList
    addOrderToSortedList(orderList, newOrder)

    orderbook[currencyPair]?.put("LastChange", Instant.now().toEpochMilli())
    orderbook[currencyPair]?.put("SequenceNumber", newOrder.sequenceNumber);
  }

  companion object {
    fun generateUUID(): String {
      return UUID.randomUUID().toString()
    }
  }

  private fun matchOrder(order: Order): Order {
    if (order.side == OrderSide.BUY) {
      //check the lowest price sell order whose price is less than or equal to this order
      //iterate through as many of these as you find
      //for each of those sell orders, check the quantity on offer
      //if buy quantity is less than or equal what is being offered, subtract qty from both orders and mark as closed if qty is 0
      //else, just subtract the qty being offered, mark sell order as closed and check the next, until either the buy order is fully matched or no more sell orders are available for matching
      val matchingSellOrders = ((orderbook[order.currencyPair]?.get("Asks")
        ?: mutableListOf<Order>()) as OrdersList).filter { it -> it.price <= order.price && it.isOpen }
        .sortedBy { it -> it.price }

      for (sellOrder in matchingSellOrders) {
        if (sellOrder.quantity >= order.quantity) {
          val quantityToTrade = order.quantity;
          order.quantity -= quantityToTrade;
          sellOrder.quantity -= quantityToTrade;
        } else {
          val quantityToTrade = sellOrder.quantity;
          order.quantity -= quantityToTrade;
          sellOrder.quantity -= quantityToTrade;
        }
        if (order.quantity.toInt() == 0) break
      }
    } else {
      val matchingBuyOrders = ((orderbook[order.currencyPair]?.get("Bids")
        ?: mutableListOf<Order>()) as OrdersList).filter { it -> it.price >= order.price && it.isOpen }

      for (buyOrder in matchingBuyOrders) {
        if (buyOrder.quantity >= order.quantity) {
          val quantityToTrade = order.quantity;
          order.quantity -= quantityToTrade;
          buyOrder.quantity -= quantityToTrade;
        } else {
          val quantityToTrade = buyOrder.quantity;
          order.quantity -= quantityToTrade;
          buyOrder.quantity -= quantityToTrade;
        }
        if (order.quantity.toInt() == 0) break
      }
    }

    return order;
  }
}

