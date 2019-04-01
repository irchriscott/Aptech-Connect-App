package com.scorpions.aptechconnectapp

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.api.services.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translate.TranslateOption
import com.google.gson.Gson
import com.scorpions.aptechconnectapp.adapters.ArticleViewHolder
import com.scorpions.aptechconnectapp.models.Article
import com.scorpions.aptechconnectapp.models.User
import com.scorpions.aptechconnectapp.providers.UserAuthentication
import com.scorpions.aptechconnectapp.utils.Routes
import com.scorpions.aptechconnectapp.utils.Utils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_article.*
import com.google.cloud.translate.Translation
import com.scorpions.aptechconnectapp.adapters.CommentsAdapter
import com.scorpions.aptechconnectapp.models.Comment
import com.scorpions.aptechconnectapp.models.ServerResponse
import okhttp3.*
import org.apache.commons.lang3.StringEscapeUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

import java.lang.Exception


class ArticleActivity : AppCompatActivity() {

    private var user: User? = null
    private lateinit var userAuthentication: UserAuthentication
    private val routes: Routes = Routes()
    private lateinit var utils: Utils
    lateinit var commentsList: MutableList<Comment>

    @SuppressLint("StaticFieldLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        val gson = Gson()

        val articleString = intent.getStringExtra(ArticleViewHolder.ARTICLE_OBJECT)
        val article: Article = gson.fromJson(articleString, Article::class.java)

        userAuthentication = UserAuthentication(this@ArticleActivity)
        user = userAuthentication.getUserFromSharedPreferences()
        utils = Utils(this@ArticleActivity)

        val languages = this.utils.getLanguages()

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.title = article.title

        userName.text = article.user.name
        articleTitle.text = article.title
        content.text = article.text
        comments.text = article.comments.count().toString()
        postedAt.text = utils.formatDateTime(article.created_at)

        this.commentsList = article.comments

        Picasso.get().load("${this.routes.SLASH_END_POINT}${article.user.image}").into(userImage)
        Picasso.get().load("${this.routes.SLASH_END_POINT}${article.image}").into(imageView)
        Picasso.get().load("${this.routes.SLASH_END_POINT}${user?.image}").into(commentProfileImage)

        val utils: Utils = Utils(this@ArticleActivity)

        val textViewHandler: Handler = Handler()

        if(article.comments.isNotEmpty()){
            commentsView.layoutManager = LinearLayoutManager(this@ArticleActivity)
            commentsView.adapter = CommentsAdapter(this.commentsList)

            noComments.visibility = View.GONE
            commentsView.visibility = View.VISIBLE
        } else {
            noComments.visibility = View.VISIBLE
            commentsView.visibility = View.GONE
        }

        content.setOnClickListener {
            content.text = article.text
            articleTitle.text = article.title
        }

        submitComment.setOnClickListener {
            this.postComment(article)
        }

        content.setOnLongClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this@ArticleActivity)
            alertDialogBuilder.setTitle("Translate To : ")
            alertDialogBuilder.setItems(user?.branch!!.languages.toTypedArray(), DialogInterface.OnClickListener { _, which ->


                object : AsyncTask<Void, Void, Void>() {

                    override fun doInBackground(vararg params: Void): Void? {
                        try {
                            val language = languages.findLast {
                                it.name.trim().toLowerCase() == user?.branch!!.languages[which].trim().toLowerCase()
                            }
                            if(language != null ){
                                val options: TranslateOptions = TranslateOptions.newBuilder().setApiKey("AIzaSyD2mLGusTJZqu7zesBgobnoVIzN6hIayvk").build()
                                val translate = options.service
                                val detection = translate.detect(article.text)
                                val translation = translate.translate(article.text,
                                    com.google.cloud.translate.Translate.TranslateOption.sourceLanguage(detection.language),
                                    com.google.cloud.translate.Translate.TranslateOption.targetLanguage(language.language)
                                )
                                val titleTranslation = translate.translate(article.title,
                                    com.google.cloud.translate.Translate.TranslateOption.sourceLanguage(detection.language),
                                    com.google.cloud.translate.Translate.TranslateOption.targetLanguage(language.language)
                                )
                                textViewHandler.post {
                                    if (content != null && articleTitle != null) {
                                        content.text = StringEscapeUtils.unescapeHtml4(translation.translatedText)
                                        articleTitle.text = StringEscapeUtils.unescapeHtml4(titleTranslation.translatedText)
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@ArticleActivity, "Language Not Supported", Toast.LENGTH_LONG).show()
                                }
                            }
                            return null
                        }
                        catch (e: Exception){
                            runOnUiThread {
                                Toast.makeText(this@ArticleActivity, "Language Not Supported", Toast.LENGTH_LONG).show()
                            }
                            return null
                        }
                    }
                }.execute()

            })

            alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ -> })
            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
            true
        }

    }

    private fun postComment(article: Article){
        val url = "${this.routes.END_POINT}${this.routes.postComment}${this.user?.token}/"

        val jsonObject = JSONObject()
        val mediaType = MediaType.parse("application/json")
        val client = OkHttpClient()

        try {
            jsonObject.put("article", article.id)
            jsonObject.put("comment", commentEditText.text)
        }
        catch (e: JSONException){
            e.printStackTrace()
        }

        if(commentEditText.text.toString() != "") {

            val body = RequestBody.create(mediaType, jsonObject.toString())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.i("ERROR RES", e.message)
                    runOnUiThread {
                        Toast.makeText(this@ArticleActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body()!!.string()
                    val gson = Gson()
                    if (response.code() == 200) {
                        val commentResponse = gson.fromJson(responseBody, Comment::class.java)
                        runOnUiThread {
                            commentEditText.text = null
                            Toast.makeText(this@ArticleActivity, "Comment Posted Successfully", Toast.LENGTH_SHORT)
                                .show().also {
                                commentsList.add(commentResponse)
                                noComments.visibility = View.GONE
                                commentsView.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        val serverResponse = gson.fromJson(responseBody, ServerResponse::class.java)
                        runOnUiThread {
                            Toast.makeText(this@ArticleActivity, serverResponse.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            })
        } else {
            Toast.makeText(this@ArticleActivity, "Comment Cannot Be Empty", Toast.LENGTH_SHORT).show()
        }
    }
}
