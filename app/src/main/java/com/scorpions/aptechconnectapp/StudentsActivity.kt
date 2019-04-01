package com.scorpions.aptechconnectapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.scorpions.aptechconnectapp.adapters.StudentsAdapter
import com.scorpions.aptechconnectapp.models.Event
import com.scorpions.aptechconnectapp.models.User
import com.scorpions.aptechconnectapp.providers.UserAuthentication
import com.scorpions.aptechconnectapp.utils.Routes
import kotlinx.android.synthetic.main.activity_students.*
import kotlinx.android.synthetic.main.error_network.view.*
import okhttp3.*
import java.io.IOException

class StudentsActivity : AppCompatActivity() {

    private lateinit var userAuthentication: UserAuthentication
    private val routes: Routes = Routes()

    private val client: OkHttpClient = OkHttpClient()
    private lateinit var request: Request

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students)

        supportActionBar!!.title = "Students"

        userAuthentication = UserAuthentication(this@StudentsActivity)
        val user = userAuthentication.getUserFromSharedPreferences()

        this.getUsers(user)
    }

    private fun getUsers(user: User?){
        val url = "${this.routes.END_POINT}${this.routes.getUsers}${user?.token}/"
        request = Request.Builder()
            .url(url)
            .build()

        val gson = GsonBuilder().create()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    Toast.makeText(this@StudentsActivity, e?.message, Toast.LENGTH_LONG).show()
                    progress_circular.visibility = View.GONE
                    networkError.visibility = View.VISIBLE
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                val responseBody = response?.body()!!.string()
                if(response?.code() == 200){
                    if(responseBody != ""){
                        val students = gson.fromJson<List<User>>(responseBody, object: TypeToken<List<User>>(){}.type)
                        if(!students.isEmpty()) {
                            runOnUiThread {

                                eventsView.layoutManager = LinearLayoutManager(this@StudentsActivity)
                                eventsView.adapter = StudentsAdapter(students)

                                progress_circular.visibility = View.GONE
                                networkError.visibility = View.GONE
                                eventsView.visibility = View.VISIBLE
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@StudentsActivity, "No Student Enrolled", Toast.LENGTH_LONG).show()
                                progress_circular.visibility = View.GONE
                                networkError.visibility = View.VISIBLE
                                networkError.errorMessage.text  = "No Student Enrolled"
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@StudentsActivity, "No Student Enrolled", Toast.LENGTH_LONG).show()
                            progress_circular.visibility = View.GONE
                            networkError.visibility = View.VISIBLE
                            networkError.errorMessage.text  = "No Student Enrolled"
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@StudentsActivity, "An Error Occurred", Toast.LENGTH_LONG).show()
                        progress_circular.visibility = View.GONE
                        networkError.visibility = View.VISIBLE
                        networkError.errorMessage.text  = "An Error Occurred"
                    }
                }
            }
        })
    }
}
