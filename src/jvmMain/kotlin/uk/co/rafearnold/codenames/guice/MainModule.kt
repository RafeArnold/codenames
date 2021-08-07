package uk.co.rafearnold.codenames.guice

import com.google.inject.AbstractModule
import com.google.inject.Binder
import com.google.inject.Provides
import com.google.inject.Scopes
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import uk.co.rafearnold.codenames.BodyRouteConfig
import uk.co.rafearnold.codenames.CorsRouteConfig
import uk.co.rafearnold.codenames.FileWordGenerator
import uk.co.rafearnold.codenames.WebPageHtmlRouteConfig
import uk.co.rafearnold.codenames.WebPageJsRouteConfig
import uk.co.rafearnold.codenames.WordGenerator
import uk.co.rafearnold.codenames.api.v1.EventResponseHandlerV1
import uk.co.rafearnold.codenames.api.v1.EventResponseHandlerV1Impl
import uk.co.rafearnold.codenames.api.v1.GameConnectionServiceV1
import uk.co.rafearnold.codenames.api.v1.GameConnectionServiceV1Impl
import uk.co.rafearnold.codenames.api.v1.handler.EventRouteHandler
import uk.co.rafearnold.codenames.api.v1.handler.NewGameRouteHandler
import uk.co.rafearnold.codenames.api.v1.serializer.EventRequestSerializerV1
import uk.co.rafearnold.codenames.api.v1.serializer.EventResponseSerializerV1
import uk.co.rafearnold.codenames.api.v1.serializer.NewGameResponseSerializerV1
import uk.co.rafearnold.codenames.api.v1.serializer.defaultEventRequestSerializerV1
import uk.co.rafearnold.codenames.api.v1.serializer.defaultEventResponseSerializerV1
import uk.co.rafearnold.codenames.api.v1.serializer.defaultNewGameResponseSerializerV1
import uk.co.rafearnold.codenames.event.EventRequestHandler
import uk.co.rafearnold.codenames.event.EventRequestHandlerImpl
import uk.co.rafearnold.codenames.game.GamesService
import uk.co.rafearnold.codenames.game.GamesServiceImpl
import javax.inject.Qualifier

class MainModule(
    private val vertx: Vertx,
    private val appConfig: JsonObject
) : AbstractModule() {

    @Provides
    @Singleton
    fun vertx(): Vertx = vertx

    @Provides
    @Singleton
    fun jsonAppConfig(): JsonObject = appConfig

    override fun configure() {
        Names.bindProperties(binder(), mapAppConfig(appConfig))
        bind(EventResponseSerializerV1::class.java).toInstance(defaultEventResponseSerializerV1)
        bind(EventRequestSerializerV1::class.java).toInstance(defaultEventRequestSerializerV1)
        bind(NewGameResponseSerializerV1::class.java).toInstance(defaultNewGameResponseSerializerV1)
        bind(EventRequestHandler::class.java).to(EventRequestHandlerImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EventResponseHandlerV1::class.java).to(EventResponseHandlerV1Impl::class.java).`in`(Scopes.SINGLETON)
        bind(GameConnectionServiceV1::class.java).to(GameConnectionServiceV1Impl::class.java).`in`(Scopes.SINGLETON)
        bind(GamesService::class.java).to(GamesServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(WordGenerator::class.java).to(FileWordGenerator::class.java).`in`(Scopes.SINGLETON)
        bindServerRoutes(binder())
    }

    private fun bindServerRoutes(binder: Binder) {
        val multibinder: Multibinder<Route> = Multibinder.newSetBinder(binder, Route::class.java)
        multibinder.addBinding().to(BodyRouteConfig::class.java).`in`(Scopes.SINGLETON)
        multibinder.addBinding().to(CorsRouteConfig::class.java).`in`(Scopes.SINGLETON)

        multibinder.addBinding().to(WebPageHtmlRouteConfig::class.java).`in`(Scopes.SINGLETON)
        multibinder.addBinding().to(WebPageJsRouteConfig::class.java).`in`(Scopes.SINGLETON)

        // Game event API v1 routes. Make sure v1 routes are bound first, before newer versions.
        // This ensures that API requests that do not specify a version will be routed to v1.
        multibinder.addBinding().to(EventRouteHandler::class.java).`in`(Scopes.SINGLETON)
        multibinder.addBinding().to(NewGameRouteHandler::class.java).`in`(Scopes.SINGLETON)
    }

    /**
     * A singleton [Router] created using the provided [Vertx].
     */
    @Provides
    @Singleton
    fun router(vertx: Vertx): Router = Router.router(vertx)

    /**
     * Provides a new [Route] registered to [router]. This is not a singleton to ensure a new route
     * is created each time a [Route] is injected into an instance.
     *
     * The provided [Route] is disabled upon creation. It is the responsibility of the instance
     * receiving the [Route] to enable it.
     */
    @Provides
    fun createNewRoute(router: Router, @RequestFailureHandler failureHandler: Handler<RoutingContext>): Route =
        router.route()
            .failureHandler(failureHandler)
            .disable()

    @Provides
    @Singleton
    @RequestFailureHandler
    fun requestFailureHandler(): Handler<RoutingContext> = uk.co.rafearnold.codenames.api.requestFailureHandler

    companion object {
        private fun mapAppConfig(jsonConfig: JsonObject): Map<String, String> =
            jsonConfig.map.mapValues { it.value.toString().removePrefix("\\") }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
private annotation class RequestFailureHandler
