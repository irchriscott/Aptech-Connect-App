package com.scorpions.aptechconnectapp.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import com.scorpions.aptechconnectapp.ArticleActivity
import com.scorpions.aptechconnectapp.R
import com.scorpions.aptechconnectapp.models.Article
import com.scorpions.aptechconnectapp.utils.Routes
import com.scorpions.aptechconnectapp.utils.Utils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.article_layout.view.*


class ArticlesAdapter(private val articles: List<Article>) : RecyclerView.Adapter<ArticleViewHolder>(){

    private val routes: Routes = Routes()
    lateinit var utils: Utils

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val articleCell = layoutInflater.inflate(R.layout.article_layout, parent, false)
        utils = Utils(parent.context)
        return ArticleViewHolder(articleCell)
    }

    override fun getItemCount(): Int {
        return this.articles.count()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = this.articles[position]
        holder.view.userName.text = article.user.name
        holder.view.title.text = article.title
        holder.view.content.text = "${article.text.substring(0, Math.min(article.text.length, 200))}... Read More"
        holder.view.comments.text = article.comments.count().toString()
        holder.view.postedAt.text = utils.formatDateTime(article.created_at)

        holder.article = article

        Picasso.get().load("${this.routes.SLASH_END_POINT}${article.user.image}").into(holder.view.userImage)
        Picasso.get().load("${this.routes.SLASH_END_POINT}${article.image}").into(holder.view.imageView)
    }

}


class ArticleViewHolder(var view: View, var article: Article? = null) : RecyclerView.ViewHolder(view) {

    companion object {
        const val ARTICLE_OBJECT = "ARTICLE_OBJECT"
    }

    private fun navigateToSingleArticle(){

        val intent = Intent(view.context, ArticleActivity::class.java)

        val jsonGSON = Gson()
        val articleOBJECT = jsonGSON.toJson(article)
        intent.putExtra(ARTICLE_OBJECT, articleOBJECT)

        view.context.startActivity(intent)
    }

    init {
        view.setOnClickListener {
            this.navigateToSingleArticle()
        }
    }

}