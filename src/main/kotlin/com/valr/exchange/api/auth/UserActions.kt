package com.valr.exchange.api.auth

import com.valr.exchange.api.common.EventActions

class UserActions: EventActions() {
  companion object {
    val signup = "signup";
    val login = "login";
    val fetch_users = "fetch_users";
    val get_user = "get_user";
  }
}
