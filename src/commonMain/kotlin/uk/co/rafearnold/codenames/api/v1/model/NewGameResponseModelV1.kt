package uk.co.rafearnold.codenames.api.v1.model

import kotlinx.serialization.Serializable

@Serializable
data class NewGameResponseModelV1(
    val gameId: String
)
