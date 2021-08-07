package uk.co.rafearnold.codenames.api.v1.model

import kotlinx.serialization.Serializable

@Serializable
sealed class GameEventRequestModelV1 {

    @Serializable
    data class ClueEvent(
        val clue: ClueModelV1
    ) : GameEventRequestModelV1()

    @Serializable
    data class GuessEvent(
        val guess: GuessModelV1
    ) : GameEventRequestModelV1()

    @Serializable
    data class NewPlayerEvent(
        val playerName: String
    ) : GameEventRequestModelV1()

    @Serializable
    data class SwitchTeamEvent(
        val newTeam: TeamModelV1,
        val newRole: RoleModelV1
    ) : GameEventRequestModelV1()

    @Serializable
    object StartGame : GameEventRequestModelV1()

    @Serializable
    object RemovePlayer : GameEventRequestModelV1()
}
