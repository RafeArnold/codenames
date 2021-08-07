package uk.co.rafearnold.codenames.api.v1.model

import kotlinx.serialization.Serializable

@Serializable
data class GameHistoryModelV1(val events: List<GameEventModelV1>)
