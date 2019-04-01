package com.scorpions.aptechconnectapp.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.cloud.translate.TranslateOptions
import com.scorpions.aptechconnectapp.R
import com.scorpions.aptechconnectapp.models.Language
import com.scorpions.aptechconnectapp.models.Message
import com.scorpions.aptechconnectapp.models.User
import com.scorpions.aptechconnectapp.providers.UserAuthentication
import com.scorpions.aptechconnectapp.utils.Routes
import com.scorpions.aptechconnectapp.utils.Utils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_article.*
import kotlinx.android.synthetic.main.message_layout.view.*
import kotlinx.android.synthetic.main.my_message.view.*
import kotlinx.android.synthetic.main.their_message.view.*
import org.apache.commons.lang3.StringEscapeUtils
import java.lang.Exception

class MessagesAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageViewHolder>(){

    private val routes: Routes = Routes()
    lateinit var utils: Utils
    private var user: User? = null
    private lateinit var languages: List<Language>

    private lateinit var auth: UserAuthentication
    private lateinit var activity: Activity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {


        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val eventCell = layoutInflater.inflate(R.layout.message_layout, parent, false)
        utils = Utils(parent.context)
        auth = UserAuthentication(parent.context)
        user = auth.getUserFromSharedPreferences()
        languages = this.utils.getLanguages()
        activity = parent.context as Activity
        return MessageViewHolder(eventCell)
    }

    override fun getItemCount(): Int {
        return this.messages.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = this.messages[position]

        if(message.user.id == user?.id){

            holder.view.message_received.visibility = View.GONE
            holder.view.message_sent.message_body.text = message.message

        } else {

            holder.view.message_sent.visibility = View.GONE
            holder.view.message_received.message_b.text = message.message
            holder.view.message_received.name.text = message.user.name

            Picasso.get().load("${this.routes.SLASH_END_POINT}${message.user.image}").into(holder.view.message_received.avatar)

            holder.view.message_received.setOnLongClickListener {
                val alertDialogBuilder = AlertDialog.Builder(holder.view.context)
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
                                    val detection = translate.detect(message.message)
                                    val translation = translate.translate(message.message,
                                        com.google.cloud.translate.Translate.TranslateOption.sourceLanguage(detection.language),
                                        com.google.cloud.translate.Translate.TranslateOption.targetLanguage(language.language)
                                    )
                                        activity.runOnUiThread {
                                            holder.view.message_received.message_b.text = StringEscapeUtils.unescapeHtml4(translation.translatedText)
                                        }

                                } else {
                                    Log.i("TRANS", "error language")
                                }
                                return null
                            }
                            catch (e: Exception){
                                Log.i("TRANS", "error trying ${e.message}")
                                return null
                            }
                        }
                    }.execute()

                })

                holder.view.message_received.setOnClickListener {
                    holder.view.message_received.message_b.text = message.message
                }

                alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ -> })
                val alertDialog: AlertDialog = alertDialogBuilder.create()
                alertDialog.show()
                true
            }
        }

    }

}

class MessageViewHolder(val view: View) : RecyclerView.ViewHolder(view){

}