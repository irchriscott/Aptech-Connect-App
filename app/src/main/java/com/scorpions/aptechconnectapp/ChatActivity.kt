package com.scorpions.aptechconnectapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.scorpions.aptechconnectapp.adapters.MessagesAdapter
import com.scorpions.aptechconnectapp.models.Message
import com.scorpions.aptechconnectapp.models.User
import com.scorpions.aptechconnectapp.providers.UserAuthentication
import com.scorpions.aptechconnectapp.utils.Routes
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import okhttp3.*
import java.io.IOException
import android.content.Intent
import android.util.Log
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.scorpions.aptechconnectapp.models.ServerResponse
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import com.github.nkzawa.emitter.Emitter
import android.text.TextUtils


class ChatActivity : AppCompatActivity() {

    private lateinit var userAuthentication: UserAuthentication
    private val routes: Routes = Routes()

    private val client: OkHttpClient = OkHttpClient()
    private lateinit var request: Request

    lateinit var student: User
    lateinit var socket: Socket
    private var user: User? = null

    init {
        try {
            socket = IO.socket("https://aptechconnectsocketio.herokuapp.com/")
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        userAuthentication = UserAuthentication(this@ChatActivity)
        user = userAuthentication.getUserFromSharedPreferences()

        val gson = Gson()

        val studentString = intent.getStringExtra(StudentActivity.STUDENT_OBJECT)
        student = gson.fromJson(studentString, User::class.java)

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.title = "Chat With "  + student.name

        Picasso.get().load("${this.routes.SLASH_END_POINT}${user?.image}").into(userImage)

        val app =

        socket.on("new message", onNewMessage)
        socket.connect()

        this.getMessages(user, student)

        sendMessage.setOnClickListener {
            if(messageText.text.toString() != ""){
                this.sendMessage(user, student)
            } else {
                Toast.makeText(this@ChatActivity, "Message Cannot Be Empty", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        socket.disconnect();
        socket.off("new message", onNewMessage);
        super.onDestroy()
    }

    private fun sendMessage(user: User?, student: User){
        val url = "${this.routes.END_POINT}api/messages/send/${user?.token}/user/${student.id}/"

        val jsonObject = JSONObject()
        val mediaType = MediaType.parse("application/json")
        val client = OkHttpClient()

        try {
            jsonObject.put("message", messageText.text)
        }
        catch (e: JSONException){
            e.printStackTrace()
        }

        val body = RequestBody.create(mediaType, jsonObject.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("ERROR RES", e.message)
                runOnUiThread {
                    Toast.makeText(this@ChatActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body()!!.string()
                val gson = Gson()

                val serverResponse = gson.fromJson(responseBody, ServerResponse::class.java)

                if(response.code() == 200){
                    attemptSend(user, student)
                    runOnUiThread {
                        messageText.text = null
                        Toast.makeText(this@ChatActivity, serverResponse.message, Toast.LENGTH_SHORT).show().also {
                            getMessages(user, student)
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ChatActivity, "Enter Valid Data", Toast.LENGTH_LONG).show()
                    }
                }
            }

        })
    }

    private fun attemptSend(user: User?, student: User?) {
        val message = messageText.text.toString().trim()
        if (TextUtils.isEmpty(message)) {
            return
        }
        val data = JSONObject()
        data.put("sender", user?.id)
        data.put("receiver", student?.id)
        socket.emit("new message", data)
        Log.i("SOCKET IO", "sent")
    }

    private val onNewMessage = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            runOnUiThread(Runnable {
                val data = args[0] as JSONObject
                val sender: String
                val receiver: String
                try {
                    sender = data.getString("sender")
                    receiver = data.getString("receiver")
                } catch (e: JSONException) {
                    return@Runnable
                }
                updateMessages(sender, receiver)
            })
        }
    }

    private fun updateMessages(sender: String, receiver: String){
        if(sender == user?.id.toString() || receiver == user?.id.toString()){
            getMessages(user, student)
        }
    }

    private fun getMessages(user: User?, student: User){
        val url = "${this.routes.END_POINT}api/messages/load/${user?.token}/user/${student.id}/"

        request = Request.Builder()
            .url(url)
            .build()

        val gson = GsonBuilder().create()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    Toast.makeText(this@ChatActivity, e?.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                val responseBody = response?.body()!!.string()
                if(response?.code() == 200){
                    if(responseBody != ""){
                        val messages = gson.fromJson<List<Message>>(responseBody, object: TypeToken<List<Message>>(){}.type)
                        if(!messages.isEmpty()) {
                            runOnUiThread {

                                messages_view.layoutManager = LinearLayoutManager(this@ChatActivity)
                                messages_view.adapter = MessagesAdapter(messages)
                                messages_view.scrollToPosition(messages_view.adapter.itemCount - 1)
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@ChatActivity, "No Messages", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ChatActivity, "No Messages", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ChatActivity, "An Error Occurred", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })

    }
}
