package uk.co.rafearnold.codenames.api.v1.model

import kotlinx.serialization.Serializable

@Serializable
data class TileModelV1(val word: String, val colour: TileColourModelV1)
