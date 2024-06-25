package com.valr.exchange.api.common

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.json.JsonObject

class EventConsumerMessageCodec<T>(private val payloadClass: Class<T>) : MessageCodec<EventConsumerMessage<T>, EventConsumerMessage<T>> {

    override fun encodeToWire(buffer: Buffer?, msg: EventConsumerMessage<T>?) {
        val json = JsonObject()
        json.put("action", msg?.action)
        json.put("payload", JsonObject.mapFrom(msg?.payload))
        buffer?.appendBuffer(json.toBuffer())
    }

    override fun decodeFromWire(pos: Int, buffer: Buffer): EventConsumerMessage<T>? {
        val json = buffer.toJsonObject()
        val action = json.getString("action")// Adjust based on your enum type
        val payloadJson = json.getJsonObject("payload")
        val payload = payloadJson.mapTo(payloadClass)
        return EventConsumerMessage(action, payload)
    }

    override fun transform(msg: EventConsumerMessage<T>?): EventConsumerMessage<T>? {
        return msg?.copy() // Transform if needed, typically return a copy
    }

    override fun name(): String {
        return "OrderBookConsumerMessageCodec_${payloadClass.simpleName}"
    }

    override fun systemCodecID(): Byte {
        // -1 means user defined codec, see https://vertx.io/docs/apidocs/io/vertx/core/eventbus/MessageCodec.html
        return -1
    }
}

