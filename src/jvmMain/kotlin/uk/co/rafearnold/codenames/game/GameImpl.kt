package uk.co.rafearnold.codenames.game

import uk.co.rafearnold.codenames.CodeNamesException

class GameImpl(
    override val id: String,
    override val tiles: List<Tile>,
    override val state: GameState
) : Game {

    private val playersMap: MutableMap<String, Player> = mutableMapOf()

    private val inactivePlayers: MutableMap<String, Player> = mutableMapOf()

    override var isStarted: Boolean = false

    override val players: List<Player>
        get() = playersMap.values.toList()

    override val history: GameHistory = GameHistoryImpl()

    override fun clue(playerId: String, clue: Clue) {
        validateGameHasStarted()
        validateAction(Action.CLUE)
        val player: Player = getPlayer(playerId)
        validatePlayer(player)
        history.events.add(GameEventImpl.Clue(playerId = playerId, word = clue.word, count = clue.count))
        state.nextAction = Action.GUESS
    }

    override fun guess(playerId: String, guess: Guess) {
        validateGameHasStarted()
        validateAction(Action.GUESS)
        val player: Player = getPlayer(playerId)
        validatePlayer(player)
        val tileIndex: Int = guess.tileIndex
        validateTileIsInBounds(tileIndex)
        validateTileIsNotRevealed(tileIndex)
        history.events.add(GameEventImpl.Guess(playerId = playerId, tileIndex = tileIndex))
        state.revealedTileIndices.add(tileIndex)
        val revealedTile: Tile = tiles[tileIndex]
        if (!guessIsCorrect(revealedTile) || !teamHasGuessesLeft()) {
            state.teamTurn = !state.teamTurn
            state.nextAction = Action.CLUE
        }
    }

    override fun newPlayer(playerId: String, playerName: String): Player {
        val inactivePlayer: Player? = inactivePlayers.remove(playerId)
        val player: Player =
            if (inactivePlayer != null) inactivePlayer
            else {
                if (playerName.isBlank()) throw PlayerNameEmptyException()
                PlayerImpl(
                    id = playerId,
                    name = playerName,
                    team = Team.SPECTATOR,
                    role = Role.GUESSER,
                    isHost = playersMap.isEmpty()
                )
            }
        playersMap[playerId] = player
        return player
    }

    override fun start(playerId: String) {
        validateGameHasNotStarted()
        val thisPlayer: Player = getPlayer(playerId)
        if (!thisPlayer.isHost) throw NotHostException()
        val allPlayers: Collection<Player> = playersMap.values
        if (allPlayers.size < 4) throw NotEnoughPlayersException()
        val bluePlayers: List<Player> = allPlayers.filter { it.team == Team.BLUE }
        if (bluePlayers.none { it.role == Role.GUESSER }) throw NotEnoughPlayersException()
        if (bluePlayers.none { it.role == Role.SPY_MASTER }) throw NotEnoughPlayersException()
        val redPlayers: List<Player> = allPlayers.filter { it.team == Team.RED }
        if (redPlayers.none { it.role == Role.GUESSER }) throw NotEnoughPlayersException()
        if (redPlayers.none { it.role == Role.SPY_MASTER }) throw NotEnoughPlayersException()
        isStarted = true
    }

    override fun switchTeam(playerId: String, newTeam: Team, newRole: Role) {
        validateGameHasNotStarted()
        val player: Player = getPlayer(playerId)
        player.team = newTeam
        player.role = newRole
    }

    override fun removePlayer(playerId: String) {
        val inactivePlayer: Player? = playersMap.remove(playerId)
        if (inactivePlayer != null) inactivePlayers[playerId] = inactivePlayer
    }

    private fun validateGameHasNotStarted() {
        if (isStarted) throw GameAlreadyStartedException()
    }

    private fun validateGameHasStarted() {
        if (!isStarted) throw GameNotStartedException()
    }

    private fun validateAction(attemptedAction: Action) {
        val expectedAction: Action = state.nextAction
        if (expectedAction != attemptedAction) {
            throw IllegalActionStateException(attemptedAction = attemptedAction, expectedAction = expectedAction)
        }
    }

    private fun getPlayer(id: String): Player =
        playersMap[id] ?: throw UnrecognisedPlayerException()

    private fun validatePlayer(player: Player) {
        val teamTurn: TeamColour = state.teamTurn
        val playerTeam: Team = player.team
        val expectedTeam: Team =
            when (teamTurn) {
                TeamColour.RED -> Team.RED
                TeamColour.BLUE -> Team.BLUE
            }
        if (playerTeam != expectedTeam) {
            throw IllegalPlayerTeamException(expectedTeam = teamTurn, actualTeam = playerTeam)
        }
        val playerRole: Role = player.role
        val expectedRole: Role =
            when (state.nextAction) {
                Action.CLUE -> Role.SPY_MASTER
                Action.GUESS -> Role.GUESSER
            }
        if (playerRole != expectedRole) {
            throw IllegalPlayerRoleException(expectedRole = expectedRole, actualRole = playerRole)
        }
    }

    private fun validateTileIsInBounds(tileIndex: Int) {
        if (tileIndex < 0 || tileIndex > 25) throw TileIndexOutOfBoundsException(tileIndex)
    }

    private fun validateTileIsNotRevealed(tileIndex: Int) {
        if (tileIndex in state.revealedTileIndices) throw TileAlreadyRevealedException(tileIndex)
    }

    private fun guessIsCorrect(guessedTile: Tile): Boolean {
        val correctTileColour: TileColour =
            when (state.teamTurn) {
                TeamColour.RED -> TileColour.RED
                TeamColour.BLUE -> TileColour.BLUE
            }
        return correctTileColour == guessedTile.colour
    }

    private fun teamHasGuessesLeft(): Boolean {
        val guessesTaken: Int =
            history.events
                .takeLastWhile { it is GameEvent.Guess }
                .count()
        val lastClue: GameEvent.Clue =
            history.events
                .filterIsInstance<GameEvent.Clue>()
                .last()
        val guessesLeft: Int = lastClue.count - guessesTaken + 1
        return guessesLeft > 0
    }
}

data class ClueImpl(override val word: String, override val count: Int) : Clue

data class GuessImpl(override val tileIndex: Int) : Guess

class GameStateImpl(override var teamTurn: TeamColour) : GameState {

    override var nextAction: Action = Action.CLUE

    override val revealedTileIndices: MutableList<Int> = mutableListOf()
}

class GameHistoryImpl : GameHistory {

    override val events: MutableList<GameEvent> = mutableListOf()
}

interface GameEventImpl {

    data class Clue(
        override val playerId: String,
        override val word: String,
        override val count: Int
    ) : GameEvent.Clue

    data class Guess(
        override val playerId: String,
        override val tileIndex: Int
    ) : GameEvent.Guess
}

data class TileImpl(override val word: String, override val colour: TileColour) : Tile

data class PlayerImpl(
    override val id: String,
    override val name: String,
    override var team: Team,
    override var role: Role,
    override val isHost: Boolean
) : Player

class UnrecognisedPlayerException : CodeNamesException("Unrecognised player")

class IllegalActionStateException(
    attemptedAction: Action,
    expectedAction: Action
) : CodeNamesException("'$expectedAction' action was expected, but '$attemptedAction' was attempted instead")

class IllegalPlayerTeamException(
    expectedTeam: TeamColour,
    actualTeam: Team
) : CodeNamesException("An action was attempted by a player from team $actualTeam, but it's team $expectedTeam's turn")

class IllegalPlayerRoleException(
    expectedRole: Role,
    actualRole: Role
) : CodeNamesException("An action was attempted by a player with the $actualRole role, but it's the turn of the player with the $expectedRole role")

class NotHostException : CodeNamesException("Only the host can perform this action")

class GameAlreadyStartedException : CodeNamesException("This action can not be performed once the game has started")

class GameNotStartedException : CodeNamesException("This action can only be performed once the game has started")

class NotEnoughPlayersException :
    CodeNamesException("There must be at least one spy master and one guesser on each team before the game can start")

class TileIndexOutOfBoundsException(tileIndex: Int) : CodeNamesException("Tile index '$tileIndex' is out of bounds")

class TileAlreadyRevealedException(tileIndex: Int) : CodeNamesException("Tile '$tileIndex' has already been revealed")

class PlayerNameEmptyException : CodeNamesException("Player name must be non-empty")
