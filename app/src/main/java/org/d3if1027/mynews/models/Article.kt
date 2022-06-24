package org.d3if1027.mynews.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(
    tableName = "articles"
)

data class Article (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val author: String?,
    val content: String?,
    val description: String?,
    val published: String?,
    val source: Source?,
    val title: String?,
    val url: String?,
    val urlToImage: String?
    ):Serializable