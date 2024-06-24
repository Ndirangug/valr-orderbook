package com.valr.exchange.auth.Exceptions

class WrongUserNameOrPasswordException(private val s: String): Exception(s) {

}
