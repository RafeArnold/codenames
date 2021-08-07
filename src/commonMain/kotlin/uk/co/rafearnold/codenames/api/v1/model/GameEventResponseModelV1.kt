package uk.co.rafearnold.codenames.api.v1.model

import kotlinx.serialization.Serializable

@Serializable
sealed class GameEventResponseModelV1 {

    interface StateUpdate {
        val playerId: String
        val started: Boolean
        val players: List<PlayerModelV1>
        val teamTurn: TeamColourModelV1
        val nextAction: ActionModelV1
        val history: GameHistoryModelV1
    }

    @Serializable
    data class StateUpdateGuesser(
        override val playerId: String,
        override val started: Boolean,
        override val players: List<PlayerModelV1>,
        override val teamTurn: TeamColourModelV1,
        override val nextAction: ActionModelV1,
        override val history: GameHistoryModelV1,
        val tiles: List<String>,
        val revealedTiles: Map<Int, TileColourModelV1>
    ) : GameEventResponseModelV1(), StateUpdate

    @Serializable
    data class StateUpdateSpyMaster(
        override val playerId: String,
        override val started: Boolean,
        override val players: List<PlayerModelV1>,
        override val teamTurn: TeamColourModelV1,
        override val nextAction: ActionModelV1,
        override val history: GameHistoryModelV1,
        val tiles: List<TileModelV1>,
        val revealedTiles: List<Int>
    ) : GameEventResponseModelV1(), StateUpdate
}
