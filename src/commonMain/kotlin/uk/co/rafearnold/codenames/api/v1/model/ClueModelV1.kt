package uk.co.rafearnold.codenames.api.v1.model

import kotlinx.serialization.Serializable

@Serializable
data class ClueModelV1(val word: String, val count: Int)
