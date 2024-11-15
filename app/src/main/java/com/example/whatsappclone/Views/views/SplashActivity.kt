package com.example.whatsappclone.Views.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.whatsappclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val database by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)

        lifecycleScope.launch {
            delay(3000)  // Show the splash screen for 2 seconds

            if (auth.currentUser == null) {
                startActivity(Intent(this@SplashActivity, Login::class.java))
                finish()  // Close SplashActivity when Login activity finishes
            } else {
                checkUserDetailsInFirestore()
            }
        }
    }

    // Function to check if user details are already in Firestore
    private fun checkUserDetailsInFirestore() {
        val userId = auth.currentUser?.uid ?: return
        database.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.getString("name").isNullOrEmpty().not()) {
                    // If user details exist (e.g., name is not empty), navigate to MainActivity
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()  // Close SplashActivity when MainActivity finishes
                } else {
                    // If user details do not exist, navigate to PersonalDetails activity
                    startActivity(Intent(this@SplashActivity, personalDetails::class.java))
                    finish()  // Close SplashActivity when PersonalDetails activity finishes
                }
                finish()  // Close SplashActivity
            }
            .addOnFailureListener {
                // If there's an error in checking, fallback to personal details activity
                startActivity(Intent(this@SplashActivity, personalDetails::class.java))
                finish()
            }
    }
}
