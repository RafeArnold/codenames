package uk.co.rafearnold.codenames.api.v1.serializer

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.co.rafearnold.codenames.api.v1.model.GameEventRequestModelV1
import uk.co.rafearnold.codenames.api.v1.model.GameEventResponseModelV1

class KotlinEventSerializerV1 : EventRequestSerializerV1, EventResponseSerializerV1 {

    override fun serialize(request: GameEventRequestModelV1): String = Json.encodeToString(request)

    override fun deserializeRequest(string: String): GameEventRequestModelV1 = Json.decodeFromString(string)

    override fun serialize(response: GameEventResponseModelV1): String = Json.encodeToString(response)

    override fun deserializeResponse(string: String): GameEventResponseModelV1 = Json.decodeFromString(string)
}
