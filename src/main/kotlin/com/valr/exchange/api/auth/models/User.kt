package com.valr.exchange.api.auth.models

import com.valr.exchange.api.common.EventConsumerPayload

data class User(var id: String, val username: String, val password: String? = null, var authToken: String? = null): EventConsumerPayload()
