package org.d3if1027.mynews.models


data class NewsResponse (
    val articles: MutableList<Article>,
    val status: String,
    val totalResult: Int
    )
