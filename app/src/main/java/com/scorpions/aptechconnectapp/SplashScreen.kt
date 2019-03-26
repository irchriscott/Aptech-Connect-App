package com.scorpions.aptechconnectapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.scorpions.aptechconnectapp.providers.UserAuthentication

class SplashScreen : Activity() {

    private lateinit var aptechText: TextView
    private lateinit var connectText: TextView
    private lateinit var animation: Animation

    private var delayHandler: Handler? = null
    private var SPLASH_SCREEN_TIME: Long = 5000
    private val userAuthentication: UserAuthentication = UserAuthentication(this@SplashScreen)

    private val runnable: Runnable = Runnable {
        if(!isFinishing){
            if(this.userAuthentication.isLoggedIn()){
                startActivity(Intent(this@SplashScreen, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashScreen, Login::class.java))
            }
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        aptechText = findViewById<TextView>(R.id.aptechtext) as TextView
        connectText = findViewById<TextView>(R.id.connectText) as TextView

        animation = AnimationUtils.loadAnimation(this, R.anim.anim_splash_screen)
        aptechText.startAnimation(animation)
        connectText.startAnimation(animation)

        delayHandler = Handler()
        delayHandler!!.postDelayed(runnable, SPLASH_SCREEN_TIME)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(delayHandler != null){
            delayHandler!!.removeCallbacks(runnable)
        }
    }
}
