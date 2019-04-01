package com.scorpions.aptechconnectapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.scorpions.aptechconnectapp.adapters.BooksAdapter
import com.scorpions.aptechconnectapp.adapters.EventsAdapter
import com.scorpions.aptechconnectapp.models.Book
import com.scorpions.aptechconnectapp.models.Event
import com.scorpions.aptechconnectapp.models.User
import com.scorpions.aptechconnectapp.providers.UserAuthentication
import com.scorpions.aptechconnectapp.utils.Routes
import kotlinx.android.synthetic.main.activity_books.*
import kotlinx.android.synthetic.main.error_network.view.*
import okhttp3.*
import java.io.IOException

class BooksActivity : AppCompatActivity() {

    private lateinit var userAuthentication: UserAuthentication
    private val routes: Routes = Routes()

    private val client: OkHttpClient = OkHttpClient()
    private lateinit var request: Request

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books)

        supportActionBar!!.title = "Books"

        userAuthentication = UserAuthentication(this@BooksActivity)
        val user = userAuthentication.getUserFromSharedPreferences()

        this.getBooks(user)
    }

    private fun getBooks(user: User?){
        val url = "${this.routes.END_POINT}${this.routes.getBooks}${user?.token}/"
        request = Request.Builder()
            .url(url)
            .build()

        val gson = GsonBuilder().create()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    Toast.makeText(this@BooksActivity, e?.message, Toast.LENGTH_LONG).show()
                    progress_circular.visibility = View.GONE
                    networkError.visibility = View.VISIBLE
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                val responseBody = response?.body()!!.string()
                if(response?.code() == 200){
                    if(responseBody != ""){
                        val books = gson.fromJson<List<Book>>(responseBody, object: TypeToken<List<Book>>(){}.type)
                        if(!books.isEmpty()) {
                            runOnUiThread {

                                booksView.layoutManager = LinearLayoutManager(this@BooksActivity)
                                booksView.adapter = BooksAdapter(books)

                                progress_circular.visibility = View.GONE
                                networkError.visibility = View.GONE
                                booksView.visibility = View.VISIBLE
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@BooksActivity, "No Posted Book", Toast.LENGTH_LONG).show()
                                progress_circular.visibility = View.GONE
                                networkError.visibility = View.VISIBLE
                                networkError.errorMessage.text  = "No Posted Book"
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@BooksActivity, "No Posted Book", Toast.LENGTH_LONG).show()
                            progress_circular.visibility = View.GONE
                            networkError.visibility = View.VISIBLE
                            networkError.errorMessage.text  = "No Posted Book"
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@BooksActivity, "An Error Occurred", Toast.LENGTH_LONG).show()
                        progress_circular.visibility = View.GONE
                        networkError.visibility = View.VISIBLE
                        networkError.errorMessage.text  = "An Error Occurred"
                    }
                }
            }
        })

    }
}
