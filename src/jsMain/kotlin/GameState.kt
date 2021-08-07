import org.w3c.dom.WebSocket
import react.RState
import uk.co.rafearnold.codenames.api.v1.model.ActionModelV1
import uk.co.rafearnold.codenames.api.v1.model.GameHistoryModelV1
import uk.co.rafearnold.codenames.api.v1.model.PlayerModelV1
import uk.co.rafearnold.codenames.api.v1.model.TeamColourModelV1
import uk.co.rafearnold.codenames.api.v1.model.TileColourModelV1
import uk.co.rafearnold.codenames.api.v1.model.TileModelV1

external interface GameState : RState {
    var joined: Boolean

    var playerId: String
    var eventSocket: WebSocket

    var started: Boolean
    var guesserTiles: GuesserTiles
    var spyMasterTiles: SpyMasterTiles
    var players: List<PlayerModelV1>
    var teamTurn: TeamColourModelV1
    var nextAction: ActionModelV1
    var history: GameHistoryModelV1

    var playerNameInputValue: String?
    var clueWord: String?
    var clueCount: Int?
}

data class GuesserTiles(val tiles: List<String>, val revealedTiles: Map<Int, TileColourModelV1>)

data class SpyMasterTiles(val tiles: List<TileModelV1>, val revealedTiles: List<Int>)
