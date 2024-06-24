package com.valr.exchange.common

data class EventConsumerMessage<T>(val action: String, val payload: T)
