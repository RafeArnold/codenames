package uk.co.rafearnold.codenames.api.v1.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerModelV1(
    val id: String,
    val name: String,
    val team: TeamModelV1,
    val role: RoleModelV1,
    val isHost: Boolean
)
