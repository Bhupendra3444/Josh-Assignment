package com.example.sampletasks.network

import com.example.sampletasks.model.ProductSnippet
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class DummyProductsApi(
    engineFactory: HttpClientEngineFactory<*>
) {
    private val client = HttpClient(engineFactory) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun fetchSnippets(limit: Int = 10): List<ProductSnippet> {
        val response: ProductResponse = client.get("https://dummyjson.com/products?limit=$limit").body()
        return response.products.map {
            ProductSnippet(
                id = it.id,
                title = it.title,
                description = it.description,
                thumbnail = it.thumbnail
            )
        }
    }
}

@Serializable
private data class ProductResponse(
    val products: List<ProductDto>
)

@Serializable
private data class ProductDto(
    val id: Long,
    val title: String,
    val description: String,
    val thumbnail: String,
    @SerialName("brand") val brand: String? = null
)

expect fun platformHttpClientEngine(): HttpClientEngineFactory<*>
