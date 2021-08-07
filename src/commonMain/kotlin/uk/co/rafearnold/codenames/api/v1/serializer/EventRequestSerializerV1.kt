package uk.co.rafearnold.codenames.api.v1.serializer

import uk.co.rafearnold.codenames.api.v1.model.GameEventRequestModelV1

interface EventRequestSerializerV1 {
    fun serialize(request: GameEventRequestModelV1): String
    fun deserializeRequest(string: String): GameEventRequestModelV1
}

val defaultEventRequestSerializerV1: EventRequestSerializerV1 = KotlinEventSerializerV1()
