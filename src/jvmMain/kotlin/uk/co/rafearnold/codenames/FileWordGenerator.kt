package uk.co.rafearnold.codenames

import io.vertx.core.Vertx
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FileWordGenerator @Inject constructor(
    @Named("words-file") wordsFile: String,
    vertx: Vertx
) : WordGenerator {

    private val words: List<String> =
        Json.decodeFromString(vertx.fileSystem().readFileBlocking(wordsFile).toString())

    override fun getWords(count: Int): List<String> =
        words.shuffled().take(count)
}
