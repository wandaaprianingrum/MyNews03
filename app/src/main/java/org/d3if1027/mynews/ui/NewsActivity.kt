package org.d3if1027.mynews.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_news.*
import org.d3if1027.mynews.R
import org.d3if1027.mynews.db.ArticleDatabase
import org.d3if1027.mynews.repository.NewsRepository

class NewsActivity : AppCompatActivity() {

    lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        val newsrepository = NewsRepository(ArticleDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsrepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)
        
        bottomNavigationView.setupWithNavController(newsNavHostFragment.findNavController())
    }
}