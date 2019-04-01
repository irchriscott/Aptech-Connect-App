package com.scorpions.aptechconnectapp

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import com.scorpions.aptechconnectapp.models.ServerResponse
import com.scorpions.aptechconnectapp.models.User
import com.scorpions.aptechconnectapp.providers.UserAuthentication
import com.scorpions.aptechconnectapp.utils.Routes
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class Login : Activity() {

    lateinit var rollNumber: EditText
    lateinit var password: EditText
    lateinit var loginButton: Button

    private lateinit var userAuthentication: UserAuthentication
    private val routes: Routes = Routes()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userAuthentication = UserAuthentication(this@Login)

        rollNumber = findViewById<EditText>(R.id.rollNumber) as EditText
        password = findViewById<EditText>(R.id.password) as EditText
        loginButton = findViewById<Button>(R.id.loginButton) as Button

        loginButton.setOnClickListener {
            loginButton.isClickable = false
            this.loginUser()
        }
    }

    private fun loginUser() {
        val url = "${routes.END_POINT}${routes.userLogin}"
        Log.i("URL", url)

        val jsonObject = JSONObject()
        val mediaType = MediaType.parse("application/json")
        val client = OkHttpClient()

        try {
            jsonObject.put("roll_no", rollNumber.text)
            jsonObject.put("password", password.text)
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
                loginButton.isClickable = true
                Log.i("ERROR RES", e.message)
                runOnUiThread {
                    Toast.makeText(this@Login, e.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body()!!.string()
                val gson = Gson()

                if(response.code() == 200){
                    Log.i("RESPONSE", responseBody)
                    val user = gson.fromJson(responseBody, User::class.java)
                    userAuthentication.saveUserToSharedPreferences(user)
                    runOnUiThread {
                        Toast.makeText(this@Login, "User Logged In Successfully", Toast.LENGTH_SHORT).show().also {
                            startActivity(Intent(this@Login, MainActivity::class.java))
                        }
                    }
                } else {
                    loginButton.isClickable = true
                    val serverResponse = gson.fromJson(responseBody, ServerResponse::class.java)
                    runOnUiThread {
                        val alertDialogBuilder = AlertDialog.Builder(this@Login)
                        alertDialogBuilder.setTitle(serverResponse.type.capitalize())
                        alertDialogBuilder.setMessage(serverResponse.message)
                        alertDialogBuilder.setNegativeButton("OK", DialogInterface.OnClickListener{ _, _ ->
                            Toast.makeText(this@Login, "Enter Valid Data", Toast.LENGTH_LONG).show()
                        })
                        alertDialogBuilder.setOnCancelListener {
                            Toast.makeText(this@Login, "Enter Valid Data", Toast.LENGTH_LONG).show()
                        }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                    }
                }
            }

        })
    }
}
