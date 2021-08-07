package uk.co.rafearnold.codenames.api.v1

import uk.co.rafearnold.codenames.game.GamesService
import javax.inject.Inject

class GameConnectionServiceV1Impl @Inject constructor(
    private val gamesService: GamesService
) : GameConnectionServiceV1 {

    private val connectionMap: MutableMap<String, MutableList<PlayerConnectionConfig>> = mutableMapOf()

    override fun getConnections(gameId: String): List<PlayerConnectionConfig> =
        connectionMap.getValue(gameId)

    override fun addConnection(gameId: String, connectionConfig: PlayerConnectionConfig) {
        connectionMap.getOrPut(gameId) { mutableListOf() }
            .add(connectionConfig)
    }

    override fun removeConnection(gameId: String, playerId: String) {
        val gameConnections: MutableList<PlayerConnectionConfig> = connectionMap.getValue(gameId)
        gameConnections.removeAll { it.playerId == playerId }
        if (gameConnections.isEmpty()) {
            gamesService.removeGame(gameId)
        }
    }
}
