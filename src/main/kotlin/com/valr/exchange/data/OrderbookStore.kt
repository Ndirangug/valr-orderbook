package com.valr.exchange.data

import com.valr.exchange.api.common.EventConsumerMessage
import com.valr.exchange.api.common.EventConsumerPayload
import com.valr.exchange.api.orderbook.OrderBookActions
import com.valr.exchange.api.orderbook.models.LimitOrderRequestModel
import com.valr.exchange.api.orderbook.models.Order
import com.valr.exchange.api.orderbook.models.OrderHistoryRequestModel
import com.valr.exchange.api.orderbook.models.OrderSide
import com.valr.exchange.data.common.EventBusAddress
import com.valr.exchange.data.common.generateUUID
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.eventbus.EventBus
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

typealias OrderBooksMap = HashMap<String, HashMap<String, Any>>
typealias OrdersList = MutableList<Order>
typealias CurrencyOrderBook = HashMap<String, Any>

class OrderbookStore(val eventBus: EventBus) {
  val orderbook: OrderBooksMap = hashMapOf()
  var sequenceIdAccumulator: Long = 0;

  init {
      setupOrderBookConsumer(eventBus)
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
              userId = orderRequest.userId,
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

            if (currencyOrderBook != null) {
              val filteredOrderBook =
                hashMapOf<String, Any>(
                  "Asks" to (currencyOrderBook?.get("Asks") as OrdersList).filter { it -> it.isOpen },
                  "Bids" to (currencyOrderBook?.get("Bids") as OrdersList).filter { it -> it.isOpen },
                  "SequenceNumber" to currencyOrderBook?.get("SequenceNumber") as Long,
                  "LastChange" to currencyOrderBook?.get("LastChange") as Long,
                )

              message.reply(EventConsumerMessage(action = OrderBookActions.fetch_orderbook, filteredOrderBook))
            } else {
              message.fail(
                HttpResponseStatus.NOT_FOUND.code(),
                "OrderBook for currencypair $currencyPair does not exists"
              )
            }
          }

          OrderBookActions.fetch_orderhistory -> {
            val orderHistoryRequestModel = message.body().payload as OrderHistoryRequestModel
            val currencyPair = orderHistoryRequestModel.currencyPair;

            if (orderbook.containsKey(currencyPair)) {
              val asks =
                orderbook[currencyPair]?.get("Asks") as OrdersList
              val bids = orderbook[currencyPair]?.get("Bids") as OrdersList
              val userOrders = (asks + bids).filter { it -> it.userId == orderHistoryRequestModel.userId }

              message.reply(EventConsumerMessage(action = OrderBookActions.fetch_orderhistory, userOrders))
            } else {
              message.fail(
                HttpResponseStatus.NOT_FOUND.code(),
                "OrderBook for currencypair $currencyPair does not exists"
              )
            }
          }
        }
      }
    }
  }

  private fun addOrderToMap(currencyPair: String, newOrder: Order) {
    orderbook.putIfAbsent(currencyPair, hashMapOf("Asks" to mutableListOf<Order>(), "Bids" to mutableListOf<Order>()))
    val key = if (newOrder.side == OrderSide.BUY) "Bids" else "Asks";
    val orderList = orderbook[currencyPair]?.get(key) as OrdersList
    addOrderToSortedList(orderList, newOrder)

    orderbook[currencyPair]?.put("LastChange", Instant.now().toEpochMilli())
    orderbook[currencyPair]?.put("SequenceNumber", newOrder.sequenceNumber);
  }

  private fun addOrderToSortedList(sortedList: MutableList<Order>, newOrder: Order) {
    val index = sortedList.binarySearch { o -> -o.price.compareTo(newOrder.price) }
    val insertIndex = if (index < 0) -index - 1 else index
    sortedList.add(insertIndex, newOrder)
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
        order.updatedAt = Instant.now().toEpochMilli()
        sellOrder.updatedAt = Instant.now().toEpochMilli()
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
        order.updatedAt = Instant.now().toEpochMilli()
        buyOrder.updatedAt = Instant.now().toEpochMilli()
        if (order.quantity.toInt() == 0) break
      }
    }

    return order;
  }
}
