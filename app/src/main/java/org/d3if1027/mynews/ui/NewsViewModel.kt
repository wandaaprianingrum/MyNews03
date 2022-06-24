package org.d3if1027.mynews.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.d3if1027.mynews.models.Article
import org.d3if1027.mynews.models.NewsResponse
import org.d3if1027.mynews.repository.NewsRepository
import org.d3if1027.mynews.util.Resource
import retrofit2.Response
import java.io.IOException

class NewsViewModel (
    app: Application,
    val newsRepository: NewsRepository
    ): AndroidViewModel(app) {
        val breakingNews = MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
        val breakingNewsPage = 1
        val breakingNewsResponse: NewsResponse? = null

        val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
        var searchNewsPage = 1
        var searchNewsResponse: NewsResponse? null

        init {
            breakingNews("us")
        }

        fun getBreakingNews(countryCode: String) = viewModelScope.launch {
            safeBreakingNewsCall(countryCode)
        }

        fun searchNews(searchQuery: String) = viewModelScope.launch {
            safeSearchNewsCall(searchQuery)
        }

        private fun handleBreakingNewsResponse(response: Response<NewsResponse>: Resource<NewsResponse>{
            if (response.isSuccessful) {
                response.body()?.let { resultResponse ->
                    breakingNewsPage++
                    if (breakingNewsResponse == null) {
                        breakingNewsResponse = resultResponse
                    } else {
                        val oldArticles = breakingNewsResponse?.articles
                        val newArticle = resultResponse.articles
                        oldArticles?.addAll(newArticle)
                    }
                    return Resource.Success(breakingNewsResponse ?: resultResponse)
                }
            }
            return Resource.Error(response.message())
        }

        private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
            if (response.isSuccessful) {
                response.body().let { resultResponse ->
                    searchNewsPage++
                    if (searchNewsResponse == null) {
                        searchNewsResponse = resultResponse
                    } else {
                        val oldArticle = searchNewsResponse?.articles
                        val newArticle = resultResponse.articles
                        oldArticle?.addAll(newArticle)
                    }
                    return Resource.Success(searchNewsResponse ?: resultResponse)
                }
            }
            return Resource.Error(response.message())
        }

        fun saveArticle(article: Article) = viewModelScope.launch {
            newsRepository.upsert(article)
        }

        fun getSaveNews() = newsRepository.getSaveNews()

        fun deleteArticle(article: Article) = viewModelScope.launch {
            newsRepository.deleteArticle(article)
        }

        private suspend fun SafeSearchNewsCall(searchQuery: String) {
            searchNews.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                    searchNews.postValue(handleSearchNewsResponse(response))
                } else {
                    searchNews.postValue(Resource.Error("No Internet Connection"))
                }
            }catch (t: Throwable) {
                when(t) {
                    is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                    else -> searchNews.postValue(Resource.Error("Conversion Error"))
                }
            }
        }

    private suspend fun SafeBreakingNewsCall(searchQuery: String) {
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                BreakingNews.postValue(Resource.Error("No Internet Connection"))
            }
        }catch (t: Throwable) {
            when(t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
        }
    }
