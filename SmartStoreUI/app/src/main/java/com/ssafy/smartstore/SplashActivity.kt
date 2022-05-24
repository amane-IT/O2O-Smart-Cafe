
package com.ssafy.smartstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.UiThread
import com.ssafy.smartstore.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        splashAnimaton()

    }

    @UiThread
    private fun splashAnimaton(){
        val textAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.anim_splash_textview)
        binding.tvTitle.startAnimation(textAnim)
        val imageAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.anim_splash_imageview)
        binding.imgTitle.startAnimation(imageAnim)

        imageAnim.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationStart(animation: Animation?) {

            }

            val timeout: Long = 3000

            override fun onAnimationEnd(animation: Animation?) {
                Handler().postDelayed({
                    kotlin.run {
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }, timeout)
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
        })
    }
}