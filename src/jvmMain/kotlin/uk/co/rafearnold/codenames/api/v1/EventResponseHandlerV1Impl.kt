package uk.co.rafearnold.codenames.api.v1

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.co.rafearnold.codenames.api.v1.model.GameEventResponseModelV1
import uk.co.rafearnold.codenames.api.v1.model.TileColourModelV1
import uk.co.rafearnold.codenames.api.v1.model.TileModelV1
import uk.co.rafearnold.codenames.api.v1.model.mapToActionModelV1
import uk.co.rafearnold.codenames.api.v1.model.mapToGameHistoryModelV1
import uk.co.rafearnold.codenames.api.v1.model.mapToPlayer
import uk.co.rafearnold.codenames.api.v1.model.mapToTeamColourModelV1
import uk.co.rafearnold.codenames.api.v1.model.mapToTileColourModelV1
import uk.co.rafearnold.codenames.api.v1.model.mapToTileModelV1
import uk.co.rafearnold.codenames.api.v1.serializer.EventResponseSerializerV1
import uk.co.rafearnold.codenames.event.GameEventResponse
import uk.co.rafearnold.codenames.game.Game
import uk.co.rafearnold.codenames.game.GamesService
import uk.co.rafearnold.codenames.game.Player
import uk.co.rafearnold.codenames.game.Role
import javax.inject.Inject

class EventResponseHandlerV1Impl @Inject constructor(
    private val connectionService: GameConnectionServiceV1,
    private val gamesService: GamesService,
    private val eventResponseSerializer: EventResponseSerializerV1
) : EventResponseHandlerV1 {

    override fun handle(response: GameEventResponse) {
        return when (response) {
            is GameEventResponse.StateUpdate -> handleStateUpdate(response)
        }
    }

    private fun handleStateUpdate(response: GameEventResponse.StateUpdate) {
        val connections: List<PlayerConnectionConfig> = connectionService.getConnections(response.gameId)
        val game: Game = gamesService.getGame(response.gameId)
        for (config: PlayerConnectionConfig in connections) {
            val playerId: String = config.playerId
            val player: Player = game.players.first { it.id == playerId }
            val connectionResponse =
                when (player.role) {
                    Role.GUESSER -> {
                        val revealedTiles: Map<Int, TileColourModelV1> =
                            response.revealedTiles
                                .associateWith { index: Int ->
                                    response.tiles[index].colour.mapToTileColourModelV1()
                                }
                        GameEventResponseModelV1.StateUpdateGuesser(
                            playerId = playerId,
                            started = response.started,
                            tiles = response.tiles.map { it.word },
                            players = response.players.map { it.mapToPlayer() },
                            teamTurn = response.teamTurn.mapToTeamColourModelV1(),
                            nextAction = response.nextAction.mapToActionModelV1(),
                            revealedTiles = revealedTiles,
                            history = response.history.mapToGameHistoryModelV1()
                        )
                    }
                    Role.SPY_MASTER -> {
                        val tiles: List<TileModelV1> = response.tiles.map { it.mapToTileModelV1() }
                        GameEventResponseModelV1.StateUpdateSpyMaster(
                            playerId = playerId,
                            started = response.started,
                            tiles = tiles,
                            players = response.players.map { it.mapToPlayer() },
                            teamTurn = response.teamTurn.mapToTeamColourModelV1(),
                            nextAction = response.nextAction.mapToActionModelV1(),
                            revealedTiles = response.revealedTiles,
                            history = response.history.mapToGameHistoryModelV1()
                        )
                    }
                }
            val responseString: String = eventResponseSerializer.serialize(connectionResponse)
            log.info("Sending text message to player '$playerId': $responseString")
            config.connection.writeTextMessage(responseString)
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(EventResponseHandlerV1Impl::class.java)
    }
}
