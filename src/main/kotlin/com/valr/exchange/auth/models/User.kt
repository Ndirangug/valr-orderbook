package com.valr.exchange.auth.models

import com.valr.exchange.common.models.EventConsumerPayload

data class User(var id: String, val username: String, val password: String? = null, var authToken: String? = null): EventConsumerPayload()
