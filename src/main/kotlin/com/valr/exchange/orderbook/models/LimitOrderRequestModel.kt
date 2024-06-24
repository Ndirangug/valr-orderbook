package com.valr.exchange.orderbook.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.valr.exchange.common.models.EventConsumerPayload

data class LimitOrderRequestModel @JsonCreator constructor(
  @JsonProperty("side") val side: String,
  @JsonProperty("quantity") val quantity: Double,
  @JsonProperty("price") val price: Double,
  @JsonProperty("pair") val pair: String,
  @JsonProperty("postOnly") val postOnly: Boolean,
  @JsonProperty("customerOrderId") val customerOrderId: String,
  @JsonProperty("timeInForce") val timeInForce: String
) : EventConsumerPayload()