package com.valr.exchange.auth.exceptions

class DuplicateUsernameException(private val s: String) : Exception(s) {
}
