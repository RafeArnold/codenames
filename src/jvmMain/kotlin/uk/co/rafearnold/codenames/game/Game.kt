package uk.co.rafearnold.codenames.game

interface Game {

    val id: String

    val isStarted: Boolean

    val tiles: List<Tile>

    val players: List<Player>

    val state: GameState

    val history: GameHistory

    fun clue(playerId: String, clue: Clue)

    fun guess(playerId: String, guess: Guess)

    fun newPlayer(playerId: String, playerName: String): Player

    fun start(playerId: String)

    fun switchTeam(playerId: String, newTeam: Team, newRole: Role)

    fun removePlayer(playerId: String)
}

interface Clue {
    val word: String
    val count: Int
}

interface Guess {
    val tileIndex: Int
}

interface GameState {

    var teamTurn: TeamColour

    var nextAction: Action

    val revealedTileIndices: MutableList<Int>
}

interface GameHistory {

    val events: MutableList<GameEvent>
}

sealed interface GameEvent {

    interface Clue : GameEvent {
        val playerId: String
        val word: String
        val count: Int
    }

    interface Guess : GameEvent {
        val playerId: String
        val tileIndex: Int
    }
}

interface Tile {
    val word: String
    val colour: TileColour
}

enum class TileColour {
    RED, BLUE, GREY, BLACK
}

enum class TeamColour {
    RED, BLUE;

    operator fun not(): TeamColour =
        when (this) {
            RED -> BLUE
            BLUE -> RED
        }
}

enum class Action {
    CLUE, GUESS
}

interface Player {
    val id: String
    val name: String
    var team: Team
    var role: Role
    val isHost: Boolean
}

enum class Team {
    RED, BLUE, SPECTATOR
}

enum class Role {
    SPY_MASTER, GUESSER
}
