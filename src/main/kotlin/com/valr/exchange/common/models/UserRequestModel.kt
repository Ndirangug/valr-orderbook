package com.valr.exchange.common.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class UserRequestModel @JsonCreator constructor(
  @JsonProperty("username") val username: String = "",
  @JsonProperty("password") val password: String = ""
) : EventConsumerPayload()
