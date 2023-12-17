package com.axiii.parkingtally.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.axiii.parkingtally.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Launch splash screen
        lifecycleScope.launch {
            delay(2000L)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}