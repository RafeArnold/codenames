package uk.co.rafearnold.codenames.api.v1.model

import uk.co.rafearnold.codenames.event.GameEventRequest
import uk.co.rafearnold.codenames.game.Action
import uk.co.rafearnold.codenames.game.Clue
import uk.co.rafearnold.codenames.game.ClueImpl
import uk.co.rafearnold.codenames.game.GameEvent
import uk.co.rafearnold.codenames.game.GameHistory
import uk.co.rafearnold.codenames.game.Guess
import uk.co.rafearnold.codenames.game.GuessImpl
import uk.co.rafearnold.codenames.game.Player
import uk.co.rafearnold.codenames.game.Role
import uk.co.rafearnold.codenames.game.Team
import uk.co.rafearnold.codenames.game.TeamColour
import uk.co.rafearnold.codenames.game.Tile
import uk.co.rafearnold.codenames.game.TileColour

fun Action.mapToActionModelV1(): ActionModelV1 =
    when (this) {
        Action.CLUE -> ActionModelV1.CLUE
        Action.GUESS -> ActionModelV1.GUESS
    }

fun ClueModelV1.mapToClue(): Clue =
    ClueImpl(word = word, count = count)

fun GameEvent.mapToGameEventModelV1(): GameEventModelV1 =
    when (this) {
        is GameEvent.Clue -> GameEventModelV1.Clue(playerId = this.playerId, word = this.word, count = this.count)
        is GameEvent.Guess -> GameEventModelV1.Guess(playerId = playerId, tileIndex = this.tileIndex)
    }

fun GameEventRequestModelV1.mapToGameEventRequest(gameId: String, playerId: String): GameEventRequest =
    when (this) {
        is GameEventRequestModelV1.ClueEvent -> {
            GameEventRequest.ClueEvent(
                gameId = gameId,
                playerId = playerId,
                clue = this.clue.mapToClue()
            )
        }
        is GameEventRequestModelV1.GuessEvent -> {
            GameEventRequest.GuessEvent(
                gameId = gameId,
                playerId = playerId,
                guess = this.guess.mapToGuess()
            )
        }
        is GameEventRequestModelV1.NewPlayerEvent -> {
            GameEventRequest.NewPlayerEvent(
                gameId = gameId,
                playerId = playerId,
                playerName = this.playerName
            )
        }
        is GameEventRequestModelV1.StartGame -> {
            GameEventRequest.StartGame(
                gameId = gameId,
                playerId = playerId
            )
        }
        is GameEventRequestModelV1.SwitchTeamEvent -> {
            GameEventRequest.SwitchTeamEvent(
                gameId = gameId,
                playerId = playerId,
                newTeam = this.newTeam.mapToTeam(),
                newRole = this.newRole.mapToRole()
            )
        }
        GameEventRequestModelV1.RemovePlayer -> {
            GameEventRequest.RemovePlayer(
                gameId = gameId,
                playerId = playerId
            )
        }
    }

fun GameHistory.mapToGameHistoryModelV1(): GameHistoryModelV1 =
    GameHistoryModelV1(
        events = this.events.map { it.mapToGameEventModelV1() }
    )

fun GuessModelV1.mapToGuess(): Guess =
    GuessImpl(tileIndex = tileIndex)

fun Player.mapToPlayer(): PlayerModelV1 =
    PlayerModelV1(
        id = this.id,
        name = this.name,
        team = this.team.mapToTeamModelV1(),
        role = this.role.mapToRoleModelV1(),
        isHost = this.isHost
    )

fun Role.mapToRoleModelV1(): RoleModelV1 =
    when (this) {
        Role.SPY_MASTER -> RoleModelV1.SPY_MASTER
        Role.GUESSER -> RoleModelV1.GUESSER
    }

fun RoleModelV1.mapToRole(): Role =
    when (this) {
        RoleModelV1.SPY_MASTER -> Role.SPY_MASTER
        RoleModelV1.GUESSER -> Role.GUESSER
    }

fun TeamColour.mapToTeamColourModelV1(): TeamColourModelV1 =
    when (this) {
        TeamColour.RED -> TeamColourModelV1.RED
        TeamColour.BLUE -> TeamColourModelV1.BLUE
    }

fun Team.mapToTeamModelV1(): TeamModelV1 =
    when (this) {
        Team.RED -> TeamModelV1.RED
        Team.BLUE -> TeamModelV1.BLUE
        Team.SPECTATOR -> TeamModelV1.SPECTATOR
    }

fun TeamModelV1.mapToTeam(): Team =
    when (this) {
        TeamModelV1.RED -> Team.RED
        TeamModelV1.BLUE -> Team.BLUE
        TeamModelV1.SPECTATOR -> Team.SPECTATOR
    }

fun TileColour.mapToTileColourModelV1(): TileColourModelV1 =
    when (this) {
        TileColour.RED -> TileColourModelV1.RED
        TileColour.BLUE -> TileColourModelV1.BLUE
        TileColour.GREY -> TileColourModelV1.GREY
        TileColour.BLACK -> TileColourModelV1.BLACK
    }

fun Tile.mapToTileModelV1(): TileModelV1 =
    TileModelV1(
        word = this.word,
        colour = this.colour.mapToTileColourModelV1()
    )
