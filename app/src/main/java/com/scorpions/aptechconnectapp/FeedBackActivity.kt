package com.scorpions.aptechconnectapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.scorpions.aptechconnectapp.models.ServerResponse
import com.scorpions.aptechconnectapp.models.User
import com.scorpions.aptechconnectapp.providers.UserAuthentication
import com.scorpions.aptechconnectapp.utils.Routes
import kotlinx.android.synthetic.main.activity_feed_back.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class FeedBackActivity : AppCompatActivity() {

    private lateinit var userAuthentication: UserAuthentication
    private val routes: Routes = Routes()
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_back)

        supportActionBar!!.title = "Send FeedBack To Aptech"
        userAuthentication = UserAuthentication(this@FeedBackActivity)
        user = userAuthentication.getUserFromSharedPreferences()

        submit.setOnClickListener {
            this.postFeedBack(user)
        }
    }

    private fun postFeedBack(user: User?) {
        val url = "${routes.END_POINT}${routes.sendFeedback}${user?.token}/"
        val client = OkHttpClient()

        if(titleText.text.toString() != "" && textText.text.toString() != "") {

            val req = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("about", titleText.text.toString())
                .addFormDataPart("feedback", textText.text.toString()).build()

            val request = Request.Builder()
                .url(url)
                .post(req)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@FeedBackActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body()!!.string()
                    val gson = Gson()
                    val serverResponse = gson.fromJson(responseBody, ServerResponse::class.java)
                    if (response.code() == 200) {
                        runOnUiThread {
                            Toast.makeText(this@FeedBackActivity, serverResponse.message, Toast.LENGTH_SHORT).show().also {
                                startActivity(Intent(this@FeedBackActivity, MainActivity::class.java))
                            }
                        }
                    } else {
                        runOnUiThread {
                            val alertDialogBuilder = AlertDialog.Builder(this@FeedBackActivity)
                            alertDialogBuilder.setTitle(serverResponse.type.capitalize())
                            alertDialogBuilder.setMessage(serverResponse.message)
                            alertDialogBuilder.setNegativeButton("OK", DialogInterface.OnClickListener { _, _ ->
                                Toast.makeText(this@FeedBackActivity, "Enter Valid Data", Toast.LENGTH_LONG).show()
                            })
                            alertDialogBuilder.setOnCancelListener {
                                Toast.makeText(this@FeedBackActivity, "Enter Valid Data", Toast.LENGTH_LONG).show()
                            }
                            val alertDialog = alertDialogBuilder.create()
                            alertDialog.show()
                        }
                    }
                }

            })
        } else {
            Toast.makeText(this@FeedBackActivity, "Fill All Fields", Toast.LENGTH_LONG).show()
        }
    }
}
