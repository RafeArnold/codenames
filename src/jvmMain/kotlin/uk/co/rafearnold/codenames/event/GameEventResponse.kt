package uk.co.rafearnold.codenames.event

import uk.co.rafearnold.codenames.game.Action
import uk.co.rafearnold.codenames.game.GameHistory
import uk.co.rafearnold.codenames.game.Player
import uk.co.rafearnold.codenames.game.TeamColour
import uk.co.rafearnold.codenames.game.Tile

sealed interface GameEventResponse {

    val gameId: String

    data class StateUpdate(
        override val gameId: String,
        val started: Boolean,
        val tiles: List<Tile>,
        val players: List<Player>,
        val teamTurn: TeamColour,
        val nextAction: Action,
        val revealedTiles: List<Int>,
        val history: GameHistory
    ) : GameEventResponse
}
