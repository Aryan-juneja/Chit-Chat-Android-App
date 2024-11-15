package com.example.whatsappclone.Views.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.whatsappclone.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {
    private lateinit var alert: AlertDialog
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn.isEnabled = false

        binding.phoneNumberInput.addTextChangedListener { editable ->
            binding.btn.isEnabled = !editable.isNullOrEmpty() && editable.length == 10
        }

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Sending verification code...")
            .setCancelable(false)
            .setTitle("Please Wait")
        alert = builder.create()


        initializeCallbacks()

        binding.btn.setOnClickListener {
            val number = binding.phoneNumberInput.text.toString()
            val countryCode = binding.countryCodePicker.selectedCountryCode
            val num = "+$countryCode$number"
            showMssf(this@Login, num)
        }
    }

    private fun initializeCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("Login", "Verification completed with credential: $credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("Login", "Verification failed", e)
                alert.dismiss()
                showErrorDialog("Verification failed. Please try again.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                resendToken = token
                Log.d("Login", "Code sent: $verificationId")
                // Move to OTP verification activity and pass the verificationId
                val intent = Intent(this@Login, otpVerification::class.java)
                intent.putExtra("verificationId", verificationId)
                intent.putExtra("phoneNumber", binding.phoneNumberInput.text.toString())
                startActivity(intent)
                alert.dismiss()
                finish()
            }
        }
    }

    private fun showMssf(login: Login, num: String) {
        MaterialAlertDialogBuilder(login)
            .setMessage("We will be verifying the phone number $num.\nIs this OK, or would you like to edit the number?")
            .setNegativeButton("EDIT") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                verifyPhoneNumber(num)
            }
            .setCancelable(false)
            .show()
    }

    private fun verifyPhoneNumber(phoneNumber: String) {
        alert.show()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }



    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

     fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Login", "signInWithCredential:success")
                    // Navigate to the next screen
                } else {
                    Log.w("Login", "signInWithCredential:failure", task.exception)
                    alert.dismiss()
                    showErrorDialog("Sign-in failed. Please try again.")
                }
            }
    }
}

