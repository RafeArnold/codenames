package uk.co.rafearnold.codenames.event

import uk.co.rafearnold.codenames.game.Clue
import uk.co.rafearnold.codenames.game.Guess
import uk.co.rafearnold.codenames.game.Role
import uk.co.rafearnold.codenames.game.Team

sealed interface GameEventRequest {

    val gameId: String

    data class ClueEvent(
        override val gameId: String,
        val playerId: String,
        val clue: Clue
    ) : GameEventRequest

    data class GuessEvent(
        override val gameId: String,
        val playerId: String,
        val guess: Guess
    ) : GameEventRequest

    data class NewPlayerEvent(
        override val gameId: String,
        val playerId: String,
        val playerName: String
    ) : GameEventRequest

    data class SwitchTeamEvent(
        override val gameId: String,
        val playerId: String,
        val newTeam: Team,
        val newRole: Role
    ) : GameEventRequest

    data class StartGame(
        override val gameId: String,
        val playerId: String
    ) : GameEventRequest

    data class RemovePlayer(
        override val gameId: String,
        val playerId: String
    ) : GameEventRequest
}
