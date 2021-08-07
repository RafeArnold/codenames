package uk.co.rafearnold.codenames.api.v1

import io.vertx.core.http.ServerWebSocket

interface GameConnectionServiceV1 {

    fun getConnections(gameId: String): List<PlayerConnectionConfig>

    fun addConnection(gameId: String, connectionConfig: PlayerConnectionConfig)

    fun removeConnection(gameId: String, playerId: String)
}

data class PlayerConnectionConfig(val playerId: String, val connection: ServerWebSocket)
