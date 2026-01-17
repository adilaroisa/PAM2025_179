package com.example.grandchroniclerapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val article_id: Int,
    val user_id: Int,
    val category_id: Int,
    val title: String,
    val content: String,
    val images: List<String> = emptyList(),
    val captions: List<String> = emptyList(),
    val created_at: String? = null,
    val published_at: String? = null,
    val views_count: Int = 0,
    val category_name: String,
    val author_name: String? = null,
    val tags: String? = null,
    val status: String
) {
    // Jika list kosong, akan return null.
    val image: String?
        get() = images.firstOrNull()
}

@Serializable
data class ArticleResponse(
    val status: Boolean,
    val message: String? = null,
    val data: List<Article> = emptyList()
)

@Serializable
data class DetailArticleResponse(
    val status: Boolean,
    val message: String? = null,
    val data: Article? = null
)