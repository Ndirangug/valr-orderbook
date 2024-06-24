package com.valr.exchange.auth.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.valr.exchange.common.EventConsumerPayload

data class UserRequestModel @JsonCreator constructor(
  @JsonProperty("username") val username: String = "",
  @JsonProperty("password") val password: String = ""
) : EventConsumerPayload()
