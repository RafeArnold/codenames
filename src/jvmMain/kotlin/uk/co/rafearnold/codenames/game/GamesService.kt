package uk.co.rafearnold.codenames.game

interface GamesService {

    fun gameExists(gameId: String): Boolean

    fun getGame(gameId: String): Game

    fun newGame(): Game

    fun removeGame(gameId: String)
}
