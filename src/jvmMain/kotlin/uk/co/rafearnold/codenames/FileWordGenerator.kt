package uk.co.rafearnold.codenames

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FileWordGenerator @Inject constructor(
    @Named("words-file") private val wordsFile: String
) : WordGenerator {

    private val words: List<String> =
        Json.decodeFromString(File(wordsFile).readText())

    override fun getWords(count: Int): List<String> =
        words.shuffled().take(count)
}
