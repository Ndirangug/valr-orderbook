package com.valr.exchange.common.models

data class EventConsumerMessage<T>(val action: String, val payload: T)
