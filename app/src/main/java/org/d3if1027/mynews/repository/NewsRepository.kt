package org.d3if1027.mynews.repository

import org.d3if1027.mynews.db.ArticleDatabase
import org.d3if1027.mynews.models.Article
import retrofit2.Retrofit

class NewsRepository(val db: ArticleDatabase) {
    suspend fun getBreakingNews(country: String, pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber)

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    fun getSaveNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

}