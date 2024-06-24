package com.valr.exchange.auth

import com.valr.exchange.common.EventActions

class UserActions: EventActions() {
  companion object {
    val signup = "signup";
    val login = "login";
    val fetch_users = "fetch_users";
    val get_user = "get_user";
  }
}
