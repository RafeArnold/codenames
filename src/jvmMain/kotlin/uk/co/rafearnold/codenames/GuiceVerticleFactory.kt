package uk.co.rafearnold.codenames

import com.google.inject.Injector
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.spi.VerticleFactory
import uk.co.rafearnold.codenames.GuiceVerticleFactory.Companion.getVerticleIdentifier
import kotlin.reflect.KClass

/**
 * An optional Guice specific implementation of [VerticleFactory]. This factory takes an [Injector]
 * instance. This [Injector] provides the [Verticle] instances that vertx is asked to deploy. In
 * order for vertx to delegate verticle creation to this factory, prefix the name of the verticle
 * that you wish to deploy with [prefix] followed by a ":" when calling [Vertx.deployVerticle], or
 * call [getVerticleIdentifier].
 */
class GuiceVerticleFactory(private val injector: Injector) : VerticleFactory {

    override fun prefix(): String = prefix

    /**
     * Set to true so that vertx deploys all this factory's verticles from a worker thread, instead
     * of the main event loop thread. This prevents verticles that take significant time to deploy
     * from blocking other vertx operations.
     */
    override fun blockingCreate(): Boolean = true

    /**
     * Simply delegates the creation of the desired [Verticle] instance to [injector].
     */
    override fun createVerticle(verticleName: String, classLoader: ClassLoader): Verticle =
        injector.getInstance(classLoader.loadClass(VerticleFactory.removePrefix(verticleName))) as Verticle

    companion object {
        const val prefix = "uk.co.rafearnold.guice"

        /**
         * Provides the identifier that should be used to deploy a [Verticle] of type [clazz] from
         * this [VerticleFactory].
         */
        fun getVerticleIdentifier(clazz: KClass<out Verticle>): String =
            "$prefix:${clazz.qualifiedName}"
    }
}
