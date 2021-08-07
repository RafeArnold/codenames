package uk.co.rafearnold.codenames.api.v1.serializer

import uk.co.rafearnold.codenames.api.v1.model.GameEventResponseModelV1

interface EventResponseSerializerV1 {
    fun serialize(response: GameEventResponseModelV1): String
    fun deserializeResponse(string: String): GameEventResponseModelV1
}

val defaultEventResponseSerializerV1: EventResponseSerializerV1 = KotlinEventSerializerV1()
