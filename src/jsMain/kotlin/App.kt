import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.h1

@JsExport
class App : RComponent<RProps, RState>() {

    override fun RBuilder.render() {
        val queryParams = URLSearchParams(window.location.search)
        val gameId: String? = queryParams.get("game-id")
        div {
            h1 { +"codenames" }
            child(Game::class) { attrs.gameId = gameId }
        }
    }
}
