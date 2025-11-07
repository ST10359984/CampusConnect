package com.example.campusconnect

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 2000L
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logoSplash)
        auth = FirebaseAuth.getInstance()

        logo.alpha = 0f
        logo.scaleX = 0.5f
        logo.scaleY = 0.5f

        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserRole()
        }, SPLASH_DELAY)
    }

    private fun checkUserRole() {
        val user = auth.currentUser
        if (user != null) {
            redirectToRole(user)
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    private fun redirectToRole(user: FirebaseUser) {
        user.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isAdmin = task.result?.claims?.get("admin") as? Boolean ?: false

                    if (isAdmin) {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                finish()
            }
    }
}