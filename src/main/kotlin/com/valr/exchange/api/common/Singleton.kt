package com.valr.exchange.api.common

open class Singleton private constructor() {
  companion object {
    private var instance: Singleton? = null

    fun getInstance(): Singleton {
      if (instance == null) {
        instance = Singleton()
      }
      return instance as Singleton
    }
  }
}
