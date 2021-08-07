package uk.co.rafearnold.codenames.game

import uk.co.rafearnold.codenames.CodeNamesException
import uk.co.rafearnold.codenames.WordGenerator
import java.util.*
import javax.inject.Inject

class GamesServiceImpl @Inject constructor(
    private val wordGenerator: WordGenerator
) : GamesService {

    private val gameMap: MutableMap<String, Game> = mutableMapOf()

    override fun gameExists(gameId: String): Boolean = gameId in gameMap

    override fun getGame(gameId: String): Game = gameMap[gameId] ?: throw NoSuchGameException(gameId = gameId)

    override fun newGame(): Game {
        val firstTeam: TeamColour = TeamColour.values().random()
        val game: Game =
            GameImpl(
                id = "${UUID.randomUUID()}",
                tiles = createRandomTiles(firstTeam),
                state = GameStateImpl(firstTeam)
            )
        gameMap[game.id] = game
        return game
    }

    override fun removeGame(gameId: String) {
        gameMap.remove(gameId)
    }

    private fun createRandomTiles(firstTeam: TeamColour): List<Tile> {
        val colours: List<TileColour> = initTileColours(firstTeam).shuffled()
        val words: List<String> = wordGenerator.getWords(colours.size)
        return colours.mapIndexed { index: Int, colour: TileColour ->
            TileImpl(word = words[index], colour = colour)
        }
    }

    private fun initTileColours(firstTeam: TeamColour): List<TileColour> =
        initTileColours +
                when (firstTeam) {
                    TeamColour.RED -> TileColour.RED
                    TeamColour.BLUE -> TileColour.BLUE
                }

    companion object {
        private val initTileColours: List<TileColour> =
            listOf(
                TileColour.BLACK,
                TileColour.BLUE,
                TileColour.BLUE,
                TileColour.BLUE,
                TileColour.BLUE,
                TileColour.BLUE,
                TileColour.BLUE,
                TileColour.BLUE,
                TileColour.BLUE,
                TileColour.RED,
                TileColour.RED,
                TileColour.RED,
                TileColour.RED,
                TileColour.RED,
                TileColour.RED,
                TileColour.RED,
                TileColour.RED,
                TileColour.GREY,
                TileColour.GREY,
                TileColour.GREY,
                TileColour.GREY,
                TileColour.GREY,
                TileColour.GREY,
                TileColour.GREY
            )
    }
}

class NoSuchGameException(gameId: String) : CodeNamesException("No such game with the ID '$gameId' exists")
