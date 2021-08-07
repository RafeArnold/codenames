package uk.co.rafearnold.codenames.event

import uk.co.rafearnold.codenames.game.Game
import uk.co.rafearnold.codenames.game.GamesService
import javax.inject.Inject

class EventRequestHandlerImpl @Inject constructor(
    private val gamesService: GamesService
) : EventRequestHandler {

    override fun handle(event: GameEventRequest): GameEventResponse {
        val game: Game = gamesService.getGame(event.gameId)
        return when (event) {
            is GameEventRequest.ClueEvent -> handleClueEvent(game, event)
            is GameEventRequest.GuessEvent -> handleGuessEvent(game, event)
            is GameEventRequest.NewPlayerEvent -> handleNewPlayerEvent(game, event)
            is GameEventRequest.StartGame -> handleStartGame(game, event)
            is GameEventRequest.SwitchTeamEvent -> handleSwitchTeamEvent(game, event)
            is GameEventRequest.RemovePlayer -> handleRemovePlayerEvent(game, event)
        }
    }

    private fun handleClueEvent(game: Game, event: GameEventRequest.ClueEvent): GameEventResponse {
        game.clue(playerId = event.playerId, clue = event.clue)
        return createStateUpdateResponse(game)
    }

    private fun handleGuessEvent(game: Game, event: GameEventRequest.GuessEvent): GameEventResponse {
        game.guess(playerId = event.playerId, guess = event.guess)
        return createStateUpdateResponse(game)
    }

    private fun handleNewPlayerEvent(game: Game, event: GameEventRequest.NewPlayerEvent): GameEventResponse {
        game.newPlayer(playerId = event.playerId, playerName = event.playerName)
        return createStateUpdateResponse(game)
    }

    private fun handleStartGame(game: Game, event: GameEventRequest.StartGame): GameEventResponse {
        game.start(playerId = event.playerId)
        return createStateUpdateResponse(game)
    }

    private fun handleSwitchTeamEvent(game: Game, event: GameEventRequest.SwitchTeamEvent): GameEventResponse {
        game.switchTeam(playerId = event.playerId, newTeam = event.newTeam, newRole = event.newRole)
        return createStateUpdateResponse(game)
    }

    private fun handleRemovePlayerEvent(game: Game, event: GameEventRequest.RemovePlayer): GameEventResponse {
        game.removePlayer(event.playerId)
        return createStateUpdateResponse(game)
    }

    private fun createStateUpdateResponse(game: Game): GameEventResponse.StateUpdate {
        return GameEventResponse.StateUpdate(
            gameId = game.id,
            started = game.isStarted,
            tiles = game.tiles,
            players = game.players,
            teamTurn = game.state.teamTurn,
            nextAction = game.state.nextAction,
            revealedTiles = game.state.revealedTileIndices,
            history = game.history
        )
    }
}
