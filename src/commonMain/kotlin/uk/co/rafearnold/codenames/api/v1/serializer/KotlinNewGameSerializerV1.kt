package uk.co.rafearnold.codenames.api.v1.serializer

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.co.rafearnold.codenames.api.v1.model.NewGameResponseModelV1

class KotlinNewGameSerializerV1 : NewGameResponseSerializerV1 {

    override fun serialize(response: NewGameResponseModelV1): String = Json.encodeToString(response)

    override fun deserialize(string: String): NewGameResponseModelV1 = Json.decodeFromString(string)
}