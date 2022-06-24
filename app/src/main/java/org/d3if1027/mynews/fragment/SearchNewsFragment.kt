package org.d3if1027.mynews.fragment

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_breaking_news.*
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.android.synthetic.main.fragment_search_news.paginationProgressBar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.d3if1027.mynews.R
import org.d3if1027.mynews.adapters.NewsAdapter
import org.d3if1027.mynews.ui.NewsActivity
import org.d3if1027.mynews.util.Constants.Companion.QUERY_PAGE_SIZE
import org.d3if1027.mynews.util.Resource

class SearchNewsFragment:Fragment(R.layout.fragment_search_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    val TAG = "SearchNewsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        setupRecyclerView()

        newsAdapter.setOnItemClickListener {
            val budle = Bundle().apply {
                putSerializable("article", it)
            }

            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }

        var job: job? = null
        search.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        viewModel.searchNews(editable.toString())
                    }
                }
            }
        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer { respons ->
            when (respons) {
                is Resource<Any?>.Success -> {
                    hideProgressBar()
                    respons.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.article.toList())
                        val totalPages = newsResponse.totalResult / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.searchNewsPage == totalPages
                        if (isLastPage) {
                            rvSearchNews.setPadding(0,0,0,0)
                        }
                    }
                }

                is Resource<Any?>.Error -> {
                    hideProgressBar()
                    respons.message?.let{ message ->
                        Toast.makeText(activity, "An Error occured: $message", Toast.LENGTH_LONG).show()
                    }
                }

                is Resource<Any?>.Loading -> {
                    showingProgressBar()
                }
            }
        })
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showingProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.searchNews(search.text.toString())
                isScrolling = false
            }
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        rvSearchNews.apply {
            adapter = newsAdapter

            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }
}