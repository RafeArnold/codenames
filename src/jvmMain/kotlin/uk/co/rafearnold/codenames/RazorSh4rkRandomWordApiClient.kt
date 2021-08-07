package uk.co.rafearnold.codenames

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class RazorSh4rkRandomWordApiClient @Inject constructor(
    @Named("RazorSh4rk-api-base-url") private val baseUrl: String
) : WordGenerator {

    private val httpClient: HttpClient = HttpClient.newHttpClient()

    override fun getWords(count: Int): List<String> {
        val request: HttpRequest =
            HttpRequest.newBuilder()
                .uri(URI("$baseUrl/word?number=$count&swear=0"))
                .GET()
                .build()
        val response: HttpResponse<String> =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return Json.decodeFromString(response.body())
    }
}
