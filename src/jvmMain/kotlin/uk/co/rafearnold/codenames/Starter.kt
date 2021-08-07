package uk.co.rafearnold.codenames

import com.google.inject.Guice
import com.google.inject.Module
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.co.rafearnold.codenames.guice.MainModule
import java.time.ZoneOffset
import java.util.*
import kotlin.reflect.KClass

class Starter : AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))

        val vertx: Vertx = vertx

        vertx.exceptionHandler { log.error("Uncaught exception", it) }

        getAppConfigRetriever(vertx).getConfig { configResult: AsyncResult<JsonObject> ->
            runCatching {
                if (configResult.failed()) {
                    log.error("Failed to retrieved app config", configResult.cause())
                    startPromise.fail(configResult.cause())
                }

                val config: JsonObject = configResult.result()

                val modules: List<Module> =
                    listOf(
                        MainModule(vertx, config)
                    )
                vertx.registerVerticleFactory(GuiceVerticleFactory(Guice.createInjector(modules)))

                val verticlesToDeploy: List<KClass<out Verticle>> =
                    listOf(
                        ServerVerticle::class,
                        ServerRouteVerticle::class
                    )

                var count = verticlesToDeploy.size
                val checkpoint: Handler<AsyncResult<String>> =
                    Handler { result ->
                        if (result.succeeded()) {
                            if (--count == 0) {
                                startPromise.complete()
                                log.info("Successfully launched starter verticle")
                            }
                        } else startPromise.fail(result.cause())
                    }

                // Deploy verticles.
                for (verticleToDeploy: KClass<out Verticle> in verticlesToDeploy) {
                    vertx.deployVerticle(
                        GuiceVerticleFactory.getVerticleIdentifier(verticleToDeploy),
                        DeploymentOptions().setWorker(true)
                    ) { outcome ->
                        if (outcome.failed()) {
                            log.error("Failed to start ${verticleToDeploy.simpleName}", outcome.cause())
                        } else log.info("Successfully deployed ${verticleToDeploy.simpleName}")
                        checkpoint.handle(outcome)
                    }
                }
            }.onFailure {
                log.error("Failed to deploy verticles", it)
                startPromise.fail(it)
            }
        }
    }

    private fun getAppConfigRetriever(vertx: Vertx): ConfigRetriever {
        val applicationPropertiesPath: String =
            System.getProperty("application.properties.path", System.getenv()["APPLICATION_PROPERTIES_PATH"])
                ?: "application.properties"
        val applicationProperties =
            ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setConfig(JsonObject().put("path", applicationPropertiesPath))

        val systemProperties = ConfigStoreOptions().setType("sys")
        val envVariables = ConfigStoreOptions().setType("env")

        val options = ConfigRetrieverOptions()
            .addStore(applicationProperties)
            .addStore(systemProperties)
            .addStore(envVariables)

        return ConfigRetriever.create(vertx, options)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(Starter::class.java)
    }
}
