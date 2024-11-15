package com.example.whatsappclone.Views.views

import Models.User
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.whatsappclone.R // Import your R file for drawable access
import com.example.whatsappclone.databinding.ActivityPersonalDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class personalDetails : AppCompatActivity() {
    // Firebase Firestore instance
    private val database by lazy { FirebaseFirestore.getInstance() }

    // Firebase Auth instance
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Firebase Storage instance
    private val storage by lazy { FirebaseStorage.getInstance() }

    // View Binding
    private lateinit var binding: ActivityPersonalDetailsBinding

    // Variables to store image URI and phone number
    private var downloadUri: Uri? = null
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check and request storage permissions
        checkPermissionForImage()

        // Get phone number from current authenticated user
        phoneNumber = auth.currentUser?.phoneNumber.toString()

        // Set profile image click listener to open image picker
        binding.profileImage.setOnClickListener {
            Log.d("ProfileImage", "Image clicked!")
            pickImageFromGallery()
        }

        // Set button click listener to upload user details
        binding.btn.setOnClickListener {
            uploadUserDetails()
        }
    }

    // Function to upload user details (name, profile image, and status)
    private fun uploadUserDetails() {
        binding.btn.isEnabled = false
        binding.loader.visibility = View.VISIBLE

        // Get the user-entered name
        val name = binding.appCompatEditText.text.toString()
        if (name.isEmpty()) {
            Toast.makeText(this, "Name is empty", Toast.LENGTH_SHORT).show()
            binding.btn.isEnabled = true
            binding.loader.visibility = View.GONE
            return
        }

        // Check for status, if empty use default
        val status = if (binding.About.text.isNullOrEmpty()) {
            "Hey there! I am using Chit-Chat"
        } else {
            binding.About.text.toString()
        }

        // Ensure an image is selected, if not use default drawable image
        if (downloadUri == null) {
            Log.d("TAG", "No image selected, using default image")
            val defaultImageUri = Uri.parse(
                "android.resource://$packageName/${R.drawable.man303792640}"
            ) // Replace with your default image drawable
            uploadImage(defaultImageUri) { downloadUrl ->
                saveUserDetails(downloadUrl, name, status)
            }
        } else {
            uploadImage(downloadUri!!) { downloadUrl ->
                saveUserDetails(downloadUrl, name, status)
            }
        }
    }

    // Function to save user details to Firestore
    private fun saveUserDetails(downloadUrl: Uri, name: String, status: String) {
        val uid = auth.uid
        val user = User(downloadUrl.toString(), name, phoneNumber, uid.toString(), status)

        // Save user details in Firestore
        database.collection("Users").document(uid.toString()).set(user).addOnSuccessListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Error creating user details", Toast.LENGTH_SHORT).show()
            binding.btn.isEnabled = true
            binding.loader.visibility = View.GONE
        }
    }

    // Function to check and request storage permissions
    private fun checkPermissionForImage() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED ||
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permissions, 1001)
        }
    }

    // Function to open the image picker
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, 1000)
    }

    // Handle the result of the image picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                binding.profileImage.setImageURI(uri)
                downloadUri = uri
            }
        }
    }

    // Function to upload the image to Firebase Storage and get the download URL
    private fun uploadImage(uri: Uri, onComplete: (Uri) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val storageRef = storage.reference.child("images/$userId")
            val uploadTask = storageRef.putFile(uri)

            // Get the download URL after uploading
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    Log.d("TAG", "Image uploaded: $downloadUrl")
                    onComplete(downloadUrl)
                } else {
                    Log.d("TAG", "Upload failed")
                    binding.btn.isEnabled = true
                    binding.loader.visibility = View.GONE
                }
            }.addOnFailureListener { exception ->
                Log.d("TAG", "Upload error: ${exception.message}")
                binding.btn.isEnabled = true
                binding.loader.visibility = View.GONE
            }
        } else {
            Log.d("TAG", "User not authenticated")
            binding.btn.isEnabled = true
            binding.loader.visibility = View.GONE
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
