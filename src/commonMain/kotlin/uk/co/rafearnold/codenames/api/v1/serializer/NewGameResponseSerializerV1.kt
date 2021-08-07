package uk.co.rafearnold.codenames.api.v1.serializer

import uk.co.rafearnold.codenames.api.v1.model.NewGameResponseModelV1

interface NewGameResponseSerializerV1 {
    fun serialize(response: NewGameResponseModelV1): String
    fun deserialize(string: String): NewGameResponseModelV1
}

val defaultNewGameResponseSerializerV1: NewGameResponseSerializerV1 = KotlinNewGameSerializerV1()
