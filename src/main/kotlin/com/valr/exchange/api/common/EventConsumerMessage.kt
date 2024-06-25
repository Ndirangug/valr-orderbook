package com.valr.exchange.api.common

data class EventConsumerMessage<T>(val action: String, val payload: T)
