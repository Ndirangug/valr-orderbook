package com.valr.exchange.api.orderbook.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.valr.exchange.api.common.EventConsumerPayload

data class LimitOrderRequestModel @JsonCreator constructor(
  @JsonProperty("side") val side: String,
  @JsonProperty("quantity") val quantity: Double,
  @JsonProperty("price") val price: Double,
  @JsonProperty("pair") val pair: String,
  @JsonProperty("postOnly") val postOnly: Boolean? = null,
  @JsonProperty("customerOrderId") val customerOrderId: String? = null,
  @JsonProperty("timeInForce") val timeInForce: String? = null,
  @JsonProperty("userId") var userId: String? = null,
  @JsonProperty("allowMargin") var allowMargin: Boolean? = null,
) : EventConsumerPayload()
