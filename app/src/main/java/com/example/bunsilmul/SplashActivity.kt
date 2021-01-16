package com.example.bunsilmul

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity: AppCompatActivity() {

    val SPLASH_VIEW_TIME: Long = 1500 //1.5초간 스플래시 화면을 보여줌

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({ //delay를 위한 handler
            startActivity(Intent(this, MainActivity_Login::class.java))
            finish()
        }, SPLASH_VIEW_TIME)

    }
}