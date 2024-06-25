package com.valr.exchange.api.auth.exceptions

class WrongUserNameOrPasswordException(private val s: String): Exception(s) {

}
