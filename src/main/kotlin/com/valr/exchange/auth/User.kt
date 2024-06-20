package com.valr.exchange.auth

data class User(var id: String, val username: String, val password: String)
data class UserRequestModel(val username: String = "", val password: String = "")
