package uk.co.rafearnold.codenames.api.v1.model

import kotlinx.serialization.Serializable

@Serializable
sealed class GameEventModelV1 {

    @Serializable
    data class Clue(val playerId: String, val word: String, val count: Int): GameEventModelV1()

    @Serializable
    data class Guess(val playerId: String, val tileIndex: Int): GameEventModelV1()
}
