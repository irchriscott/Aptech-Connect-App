package com.scorpions.aptechconnectapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.scorpions.aptechconnectapp.adapters.ArticlesAdapter
import com.scorpions.aptechconnectapp.models.Article
import com.scorpions.aptechconnectapp.models.User
import com.scorpions.aptechconnectapp.providers.UserAuthentication
import com.scorpions.aptechconnectapp.utils.Routes
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.error_network.view.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var userRollNumber: TextView
    private lateinit var userName: TextView
    private lateinit var userImage: CircleImageView
    private lateinit var toolbar: Toolbar
    private lateinit var articlesView: RecyclerView
    private lateinit var floatingActionButton: FloatingActionButton

    private lateinit var userAuthentication: UserAuthentication
    private val routes: Routes = Routes()

    private val client: OkHttpClient = OkHttpClient()
    private lateinit var request: Request

    private val mOnNavigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.menu_articles -> {
                startActivity(Intent(this@MainActivity, MainActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_events -> {
                startActivity(Intent(this@MainActivity, EventsActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_students -> {
                startActivity(Intent(this@MainActivity, StudentsActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_ebooks -> {
                startActivity(Intent(this@MainActivity, BooksActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_feedback -> {
                startActivity(Intent(this@MainActivity, FeedBackActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_logout -> {
                startActivity(Intent(this@MainActivity, Login::class.java)).also {
                    Toast.makeText(this@MainActivity, "User Logged Out", Toast.LENGTH_LONG).show()
                    userAuthentication.logOutUser()
                }
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userAuthentication = UserAuthentication(this@MainActivity)
        val user = userAuthentication.getUserFromSharedPreferences()

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout) as DrawerLayout
        toolbar = findViewById<Toolbar>(R.id.action_bar) as Toolbar
        articlesView = findViewById<RecyclerView>(R.id.articlesView) as RecyclerView
        floatingActionButton = findViewById<FloatingActionButton>(R.id.addArticle) as FloatingActionButton

        floatingActionButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddArticle::class.java))
        }

        val navigationView: NavigationView = findViewById<NavigationView>(R.id.nav_view) as NavigationView

        navigationView.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawerOpen, R.string.drawerClose)

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.title = "Aptech Connect"

        toolbar.setTitleTextColor(android.graphics.Color.WHITE)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        val header: View = navigationView.getHeaderView(0)

        userName = header.findViewById(R.id.userName) as TextView
        userRollNumber = header.findViewById(R.id.userRollNumber) as TextView
        userImage = header.findViewById(R.id.userImage) as CircleImageView

        userName.text = user?.name
        userRollNumber.text = user?.roll_no!!.toUpperCase()
        Picasso.get().load("${this.routes.SLASH_END_POINT}${user?.image}").into(userImage)

        this.getArticles(user)

    }

    private fun getArticles(user: User?){
        val url = "${this.routes.END_POINT}${this.routes.getArticles}${user?.token}/"
        request = Request.Builder()
            .url(url)
            .build()

        val gson = GsonBuilder().create()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, e?.message, Toast.LENGTH_LONG).show()
                    progress_circular.visibility = View.GONE
                    networkError.visibility = View.VISIBLE
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                val responseBody = response?.body()!!.string()
                if(response?.code() == 200){
                    if(responseBody != ""){
                        val articles = gson.fromJson<List<Article>>(responseBody, object: TypeToken<List<Article>>(){}.type)
                        if(!articles.isEmpty()) {
                            runOnUiThread {

                                articlesView.layoutManager = LinearLayoutManager(this@MainActivity)
                                articlesView.adapter = ArticlesAdapter(articles)

                                progress_circular.visibility = View.GONE
                                networkError.visibility = View.GONE
                                articlesView.visibility = View.VISIBLE
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "No Posted Article", Toast.LENGTH_LONG).show()
                                progress_circular.visibility = View.GONE
                                networkError.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "No Posted Article", Toast.LENGTH_LONG).show()
                            progress_circular.visibility = View.GONE
                            networkError.visibility = View.VISIBLE
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "An Error Occurred", Toast.LENGTH_LONG).show()
                        progress_circular.visibility = View.GONE
                        networkError.visibility = View.VISIBLE
                        networkError.errorMessage.text = "An Error Occurred"
                    }
                }
            }
        })

    }
}
