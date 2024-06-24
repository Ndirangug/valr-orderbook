package com.valr.exchange.auth

import com.valr.exchange.common.models.EventActions

class UserActions: EventActions() {
  companion object {
    val signup = "signup";
    val login = "login";
    val fetch_users = "fetch_users";
  }
}
