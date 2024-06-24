package com.valr.exchange.auth.exceptions

class WrongUserNameOrPasswordException(private val s: String): Exception(s) {

}
