package com.valr.exchange.api.auth.exceptions

class DuplicateUsernameException(private val s: String) : Exception(s) {
}
