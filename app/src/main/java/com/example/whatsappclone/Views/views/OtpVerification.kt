package com.example.whatsappclone.Views.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.whatsappclone.databinding.ActivityOtpVerificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class otpVerification : AppCompatActivity() {
    private lateinit var binding: ActivityOtpVerificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        binding.btn.isEnabled = false
        binding.button.isEnabled = false

        // Retrieve the verification ID and phone number passed from the previous activity
        verificationId = intent.getStringExtra("verificationId").toString()
        val phoneNumber = intent.getStringExtra("phoneNumber")

        // Display the phone number for verification
        binding.textView4.text = "Verify $phoneNumber"

        binding.btn.setOnClickListener {
            startCountdown() // Resend OTP button functionality
        }

        // Create a spannable string for the text that includes a clickable "Wrong Number?"
        val spannable = SpannableString("Waiting to automatically detect SMS sent to $phoneNumber Wrong Number?")
        val startIndex = spannable.indexOf("Wrong Number?")
        val endIndex = startIndex + "Wrong Number?".length

        // Define the clickable span to handle the click event on "Wrong Number?"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Go back to the Login activity when "Wrong Number?" is clicked
                startActivity(Intent(this@otpVerification, Login::class.java))
                finish()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = Color.RED
            }
        }

        // Apply the clickable span to "Wrong Number?"
        spannable.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )

        // Set the spannable text on the TextView and enable link handling
        binding.textView5.apply {
            text = spannable
            movementMethod = LinkMovementMethod.getInstance() // Make the text clickable
            highlightColor = Color.TRANSPARENT // Remove the highlight effect on click
        }

        // Start the countdown timer initially
        startCountdown()

        // Add a text change listener for the OTP input
        binding.phoneNumberInput.addTextChangedListener { editable ->
            // Enable the verify button only if OTP input is valid (6 characters) and countdown is not running
            if (editable?.length == 6 && !binding.btn.isEnabled) {
                binding.button.isEnabled = true
            } else {
                binding.button.isEnabled = false
            }
        }

        // OTP verification button logic
        binding.button.setOnClickListener {
            binding.loader.visibility = View.VISIBLE
            binding.button.isEnabled = false

            val otp = binding.phoneNumberInput.text.toString()
            if (otp.isNotEmpty() && otp.length == 6) {
                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this@otpVerification, "Enter a valid OTP", Toast.LENGTH_SHORT).show()
                binding.loader.visibility = View.GONE
            }
        }
    }

    // Function to start a countdown timer for OTP verification
    private fun startCountdown() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the countdown timer display
                binding.time.text = "Time remaining: ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                // When the countdown finishes, enable the resend OTP button and disable the verify button
                binding.time.text = "Time Up"
                binding.btn.isEnabled = true // Resend button enabled
                binding.button.isEnabled = false // Disable OTP verification button
            }
        }.start()
    }

    // Function to sign in using the phone auth credential (OTP)
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.loader.visibility = View.GONE
                if (task.isSuccessful) {
                    // OTP verified and signed in successfully
                    checkUserProfileAndRedirect()
                } else {
                    Toast.makeText(this@otpVerification, "OTP Verification Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to check if the user's profile is complete
    private fun checkUserProfileAndRedirect() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists() && !document.getString("name").isNullOrEmpty()) {
                        // User has completed profile, redirect to MainActivity
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        // User profile incomplete, redirect to personalDetails activity
                        startActivity(Intent(this, personalDetails::class.java))
                    }
                    finish()
                }
                .addOnFailureListener {
                    // In case of failure, redirect to personalDetails as fallback
                    startActivity(Intent(this, personalDetails::class.java))
                    finish()
                }
        }
    }
}
