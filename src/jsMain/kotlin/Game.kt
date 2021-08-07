import kotlinx.browser.window
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.WebSocket
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import react.RBuilder
import react.RComponent
import react.dom.attrs
import react.dom.b
import react.dom.button
import react.dom.div
import react.dom.h2
import react.dom.input
import react.dom.label
import react.dom.p
import react.dom.table
import react.dom.tbody
import react.dom.td
import react.dom.th
import react.dom.thead
import react.dom.tr
import react.setState
import uk.co.rafearnold.codenames.api.v1.model.ActionModelV1
import uk.co.rafearnold.codenames.api.v1.model.ClueModelV1
import uk.co.rafearnold.codenames.api.v1.model.GameEventModelV1
import uk.co.rafearnold.codenames.api.v1.model.GameEventRequestModelV1
import uk.co.rafearnold.codenames.api.v1.model.GameEventResponseModelV1
import uk.co.rafearnold.codenames.api.v1.model.GuessModelV1
import uk.co.rafearnold.codenames.api.v1.model.NewGameResponseModelV1
import uk.co.rafearnold.codenames.api.v1.model.PlayerModelV1
import uk.co.rafearnold.codenames.api.v1.model.RoleModelV1
import uk.co.rafearnold.codenames.api.v1.model.TeamColourModelV1
import uk.co.rafearnold.codenames.api.v1.model.TeamModelV1
import uk.co.rafearnold.codenames.api.v1.model.TileColourModelV1
import uk.co.rafearnold.codenames.api.v1.model.TileModelV1
import uk.co.rafearnold.codenames.api.v1.serializer.EventRequestSerializerV1
import uk.co.rafearnold.codenames.api.v1.serializer.EventResponseSerializerV1
import uk.co.rafearnold.codenames.api.v1.serializer.defaultEventRequestSerializerV1
import uk.co.rafearnold.codenames.api.v1.serializer.defaultEventResponseSerializerV1
import uk.co.rafearnold.codenames.api.v1.serializer.defaultNewGameResponseSerializerV1

@JsExport
class Game : RComponent<GameProps, GameState>() {

    override fun GameState.init(props: GameProps) {
        setState {
            joined = false
        }
    }

    private fun joinGame(gameId: String, playerName: String) {
        val protocol: String =
            when (window.location.protocol) {
                "http:" -> "ws:"
                else -> "wss:"
            }
        val socket =
            WebSocket("$protocol//${window.location.host}${window.location.pathname.removeSuffix("/")}/play/v1/events?game-id=$gameId")
        setState { eventSocket = socket }
        socket.onmessage = {
            val message: Any? = it.data
            console.log("Message received: '$message'")
            when (val response: GameEventResponseModelV1 = eventResponseSerializerV1.deserializeResponse("$message")) {
                is GameEventResponseModelV1.StateUpdateGuesser -> handleStateUpdateGuesserMessage(response)
                is GameEventResponseModelV1.StateUpdateSpyMaster -> handleStateUpdateSpyMasterMessage(response)
            }
        }
        socket.onopen = {
            console.log("Connection opened")
            val request: GameEventRequestModelV1 =
                GameEventRequestModelV1.NewPlayerEvent(playerName = playerName)
            sendEventRequest(request)
        }
        socket.onclose = {
            console.log("Connection closed")
        }
    }

    private fun handleStateUpdateGuesserMessage(update: GameEventResponseModelV1.StateUpdateGuesser) {
        setState {
            handleStateUpdateMessage(update)
            guesserTiles = GuesserTiles(tiles = update.tiles, revealedTiles = update.revealedTiles)
        }
    }

    private fun handleStateUpdateSpyMasterMessage(update: GameEventResponseModelV1.StateUpdateSpyMaster) {
        setState {
            handleStateUpdateMessage(update)
            spyMasterTiles = SpyMasterTiles(tiles = update.tiles, revealedTiles = update.revealedTiles)
        }
    }

    private fun GameState.handleStateUpdateMessage(update: GameEventResponseModelV1.StateUpdate) {
        joined = true
        playerId = update.playerId
        started = update.started
        players = update.players
        teamTurn = update.teamTurn
        nextAction = update.nextAction
        history = update.history
    }

    private fun switchTeam(newTeam: TeamModelV1, newRole: RoleModelV1) {
        val request: GameEventRequestModelV1 =
            GameEventRequestModelV1.SwitchTeamEvent(newTeam = newTeam, newRole = newRole)
        sendEventRequest(request)
    }

    private fun enoughPlayersToStart(): Boolean {
        val allPlayers: List<PlayerModelV1> = state.players
        val bluePlayers: List<PlayerModelV1> = allPlayers.filter { it.team == TeamModelV1.BLUE }
        if (bluePlayers.none { it.role == RoleModelV1.GUESSER }) return false
        if (bluePlayers.none { it.role == RoleModelV1.SPY_MASTER }) return false
        val redPlayers: List<PlayerModelV1> = allPlayers.filter { it.team == TeamModelV1.RED }
        if (redPlayers.none { it.role == RoleModelV1.GUESSER }) return false
        if (redPlayers.none { it.role == RoleModelV1.SPY_MASTER }) return false
        return true
    }

    private fun startGame() {
        sendEventRequest(GameEventRequestModelV1.StartGame)
    }

    private fun guess(tileIndex: Int) {
        val request: GameEventRequestModelV1 =
            GameEventRequestModelV1.GuessEvent(guess = GuessModelV1(tileIndex = tileIndex))
        sendEventRequest(request)
    }

    private fun clue(word: String, count: Int) {
        val request: GameEventRequestModelV1 =
            GameEventRequestModelV1.ClueEvent(clue = ClueModelV1(word = word, count = count))
        sendEventRequest(request)
    }

    private fun sendEventRequest(request: GameEventRequestModelV1) {
        val body: String = eventRequestSerializerV1.serialize(request)
        console.log("Sending message: '$body'")
        state.eventSocket.send(body)
    }

    private fun newGame(playerName: String) {
        val requestUrl =
            "${window.location.protocol}//${window.location.host}${window.location.pathname.removeSuffix("/")}/play/v1/new-game"
        window.fetch(requestUrl, RequestInit(method = "POST"))
            .then { response: Response ->
                response.text()
                    .then { body: String ->
                        console.log("New game response body: $body")
                        val newGameResponse: NewGameResponseModelV1 =
                            defaultNewGameResponseSerializerV1.deserialize(body)
                        val gameId: String = newGameResponse.gameId
                        joinGame(gameId = gameId, playerName = playerName)
                        window.history.pushState(
                            "object or string",
                            "Title",
                            "${window.location.protocol}//${window.location.host}${window.location.pathname}?game-id=$gameId"
                        )
                    }
            }
    }

    private fun setClueWord(word: String) {
        setState { this.clueWord = word }
    }

    private fun setClueCount(count: Int?) {
        setState { this.clueCount = count }
    }

    private fun setPlayerNameInputValue(value: String) {
        setState { this.playerNameInputValue = value }
    }

    override fun RBuilder.render() {
        div {
            if (state.joined) {
                val allPlayers: List<PlayerModelV1> = state.players
                val thisPlayer: PlayerModelV1 = allPlayers.first { it.id == state.playerId }
                h2 { +"players" }
                table {
                    thead {
                        tr {
                            th { +"spectators" }
                            th { +"blue" }
                            th { +"red" }
                        }
                    }
                    val spectators: List<PlayerModelV1> =
                        allPlayers.filter { it.team == TeamModelV1.SPECTATOR }
                    val blueTeam: List<PlayerModelV1> =
                        allPlayers.filter { it.team == TeamModelV1.BLUE }
                    val blueGuessers: List<PlayerModelV1> =
                        blueTeam.filter { it.role == RoleModelV1.GUESSER }
                    val blueSpyMasters: List<PlayerModelV1> =
                        blueTeam.filter { it.role == RoleModelV1.SPY_MASTER }
                    val redTeam: List<PlayerModelV1> =
                        allPlayers.filter { it.team == TeamModelV1.RED }
                    val redGuessers: List<PlayerModelV1> =
                        redTeam.filter { it.role == RoleModelV1.GUESSER }
                    val redSpyMasters: List<PlayerModelV1> =
                        redTeam.filter { it.role == RoleModelV1.SPY_MASTER }
                    tbody {
                        tr {
                            td {
                                +spectators.joinToString(separator = ", ") { it.name }
                                if (!state.started && thisPlayer.team != TeamModelV1.SPECTATOR) {
                                    p {
                                        button {
                                            attrs {
                                                onClickFunction =
                                                    { switchTeam(TeamModelV1.SPECTATOR, RoleModelV1.GUESSER) }
                                            }
                                            +"join spectators"
                                        }
                                    }
                                }
                            }
                            td {
                                p {
                                    b { +"guessers" }
                                }
                                p {
                                    +blueGuessers.joinToString(separator = ", ") { it.name }
                                }
                                if (!state.started && (thisPlayer.team != TeamModelV1.BLUE || thisPlayer.role != RoleModelV1.GUESSER)) {
                                    p {
                                        button {
                                            attrs {
                                                onClickFunction =
                                                    { switchTeam(TeamModelV1.BLUE, RoleModelV1.GUESSER) }
                                            }
                                            +"join blue guessers"
                                        }
                                    }
                                }
                                p {
                                    b { +"spy masters" }
                                }
                                p {
                                    +blueSpyMasters.joinToString(separator = ", ") { it.name }
                                }
                                if (!state.started && (thisPlayer.team != TeamModelV1.BLUE || thisPlayer.role != RoleModelV1.SPY_MASTER)) {
                                    p {
                                        button {
                                            attrs {
                                                onClickFunction =
                                                    { switchTeam(TeamModelV1.BLUE, RoleModelV1.SPY_MASTER) }
                                            }
                                            +"join blue spy masters"
                                        }
                                    }
                                }
                            }
                            td {
                                p {
                                    b { +"guessers" }
                                }
                                p {
                                    +redGuessers.joinToString(separator = ", ") { it.name }
                                }
                                if (!state.started && (thisPlayer.team != TeamModelV1.RED || thisPlayer.role != RoleModelV1.GUESSER)) {
                                    p {
                                        button {
                                            attrs {
                                                onClickFunction =
                                                    { switchTeam(TeamModelV1.RED, RoleModelV1.GUESSER) }
                                            }
                                            +"join red guessers"
                                        }
                                    }
                                }
                                p {
                                    b { +"spy masters" }
                                }
                                p {
                                    +redSpyMasters.joinToString(separator = ", ") { it.name }
                                }
                                if (!state.started && (thisPlayer.team != TeamModelV1.RED || thisPlayer.role != RoleModelV1.SPY_MASTER)) {
                                    p {
                                        button {
                                            attrs {
                                                onClickFunction =
                                                    { switchTeam(TeamModelV1.RED, RoleModelV1.SPY_MASTER) }
                                            }
                                            +"join red spy masters"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!state.started && thisPlayer.isHost && enoughPlayersToStart()) {
                    p {
                        button {
                            attrs {
                                onClickFunction = { startGame() }
                            }
                            +"start game"
                        }
                    }
                }
                if (state.started) {
                    table {
                        tbody {
                            for (rowIndex: Int in 0 until 5) {
                                val tileIndexOffset: Int = 5 * rowIndex
                                tr {
                                    for (tileIndex: Int in tileIndexOffset until (tileIndexOffset + 5)) {
                                        val tileWord: String
                                        val revealedTileColour: TileColourModelV1?
                                        when (thisPlayer.role) {
                                            RoleModelV1.SPY_MASTER -> {
                                                val tiles: SpyMasterTiles = state.spyMasterTiles
                                                val tile: TileModelV1 = tiles.tiles[tileIndex]
                                                tileWord = tile.word
                                                revealedTileColour =
                                                    if (tileIndex in tiles.revealedTiles) tile.colour
                                                    else null
                                            }
                                            RoleModelV1.GUESSER -> {
                                                val tiles: GuesserTiles = state.guesserTiles
                                                tileWord = tiles.tiles[tileIndex]
                                                revealedTileColour = tiles.revealedTiles[tileIndex]
                                            }
                                        }
                                        td {
                                            if (revealedTileColour != null) {
                                                +"$tileWord - $revealedTileColour"
                                            } else {
                                                +tileWord
                                                if (state.nextAction == ActionModelV1.GUESS && thisPlayer.role == RoleModelV1.GUESSER) {
                                                    val playerTeam: TeamModelV1 = thisPlayer.team
                                                    val teamTurn: TeamModelV1 =
                                                        when (state.teamTurn) {
                                                            TeamColourModelV1.RED -> TeamModelV1.RED
                                                            TeamColourModelV1.BLUE -> TeamModelV1.BLUE
                                                        }
                                                    if (playerTeam == teamTurn) {
                                                        button {
                                                            attrs {
                                                                onClickFunction = { guess(tileIndex = tileIndex) }
                                                            }
                                                            +"guess"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (thisPlayer.role == RoleModelV1.SPY_MASTER) {
                        val tiles: List<TileModelV1> = state.spyMasterTiles.tiles
                        table {
                            tbody {
                                for (rowIndex: Int in 0 until 5) {
                                    val tileIndexOffset: Int = 5 * rowIndex
                                    tr {
                                        for (tileIndex: Int in tileIndexOffset until (tileIndexOffset + 5)) {
                                            td {
                                                +"${tiles[tileIndex].colour}"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (state.nextAction == ActionModelV1.CLUE && thisPlayer.role == RoleModelV1.SPY_MASTER) {
                        val playerTeam: TeamModelV1 = thisPlayer.team
                        val teamTurn: TeamModelV1 =
                            when (state.teamTurn) {
                                TeamColourModelV1.RED -> TeamModelV1.RED
                                TeamColourModelV1.BLUE -> TeamModelV1.BLUE
                            }
                        if (playerTeam == teamTurn) {
                            label { +"word" }
                            input(type = InputType.text) {
                                attrs {
                                    onChangeFunction = { setClueWord((it.target as HTMLInputElement).value) }
                                }
                            }
                            label { +"count" }
                            input(type = InputType.number) {
                                attrs {
                                    min = "1"
                                    onChangeFunction = {
                                        val count: Int? = (it.target as HTMLInputElement).value.toIntOrNull()
                                        setClueCount(count)
                                    }
                                }
                            }
                            val clueWord: String? = state.clueWord
                            val clueCount: Int? = state.clueCount
                            if (!clueWord.isNullOrBlank() && clueCount != null && clueCount > 0) {
                                button {
                                    attrs {
                                        onClickFunction = { clue(clueWord, clueCount) }
                                    }
                                    +"submit clue"
                                }
                            }
                        }
                    }
                    if (state.nextAction == ActionModelV1.GUESS) {
                        val lastClue: GameEventModelV1.Clue =
                            state.history.events
                                .filterIsInstance<GameEventModelV1.Clue>()
                                .last()
                        +"CLUE: ${lastClue.word} - ${lastClue.count}"
                    }
                }
            } else {
                label { +"name" }
                input(type = InputType.text) {
                    attrs {
                        onChangeFunction = {
                            val value: String = (it.target as HTMLInputElement).value
                            setPlayerNameInputValue(value)
                        }
                    }
                }
                val playerNameInputValue: String? = state.playerNameInputValue
                if (!playerNameInputValue.isNullOrBlank()) {
                    button {
                        attrs {
                            onClickFunction = { newGame(playerName = playerNameInputValue) }
                        }
                        +"new game"
                    }
                    val gameId: String? = props.gameId
                    if (gameId != null) {
                        button {
                            attrs {
                                onClickFunction = { joinGame(gameId = gameId, playerName = playerNameInputValue) }
                            }
                            +"join game"
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val eventRequestSerializerV1: EventRequestSerializerV1 = defaultEventRequestSerializerV1
        private val eventResponseSerializerV1: EventResponseSerializerV1 = defaultEventResponseSerializerV1
    }
}
