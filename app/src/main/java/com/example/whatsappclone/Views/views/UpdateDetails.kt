package com.example.whatsappclone.Views.views

import Models.User
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.databinding.ActivityUpdateDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage

class UpdateDetails : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityUpdateDetailsBinding
    private lateinit var userDetails: User
    private lateinit var id: String
    private val storage by lazy { FirebaseStorage.getInstance() }
    private var downloadUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        id = auth.currentUser?.uid.toString()

        if (savedInstanceState != null) {
            downloadUri = savedInstanceState.getParcelable("downloadUri")
        }

        setupUI()
        checkPermissionForImage()
    }

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

    private fun setupUI() {
        // Fetch user details from Firestore
        firestore.collection("Users").document(id).get().addOnSuccessListener { document ->
            userDetails = document.toObject(User::class.java) ?: return@addOnSuccessListener
            binding.appCompatEditText.setText(userDetails.name)
            binding.About.setText(userDetails.status)

            // Load profile image
            Glide.with(binding.profileImage)
                .load(userDetails.imageUrl)
                .placeholder(R.drawable.circle)
                .error(R.drawable.man303792640)
                .into(binding.profileImage)

            downloadUri = Uri.parse(userDetails.imageUrl) // Initialize with current image URL
        }.addOnFailureListener {
            Toast.makeText(this, "Error fetching user details", Toast.LENGTH_SHORT).show()
        }

        // Select new image on profile image click
        binding.profileImage.setOnClickListener {
            pickImageFromGallery()
        }

        // Update user details when button is clicked
        binding.btn.setOnClickListener {
            uploadUserDetails()
        }
    }

    private fun pickImageFromGallery() {
        if (hasImagePermission()) {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, 1000)
        } else {
            requestImagePermission()
        }
    }

    private fun hasImagePermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestImagePermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1001
        )
    }

    // Upload user details function
    private fun uploadUserDetails() {
        binding.btn.isEnabled = false
        binding.loader.visibility = View.VISIBLE

        // If no new image is selected, use the previously fetched image URL
        if (downloadUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            enableUI()
            return
        }

        // If downloadUri is the same as the one fetched initially, don't upload the image again
        if (downloadUri.toString() == userDetails.imageUrl) {
            // Update user details without uploading a new image
            updateUserDetails(userDetails.imageUrl)
        } else {
            // Upload the new image
            uploadImage(downloadUri!!) { downloadUrl ->
                updateUserDetails(downloadUrl.toString())
            }
        }
    }

    private fun updateUserDetails(imageUrl: String) {
        val user = hashMapOf(
            "name" to binding.appCompatEditText.text.toString(),
            "imageUrl" to imageUrl,
            "status" to binding.About.text.toString(),
            "phoneNumber" to userDetails.phoneNumber,
            "uid" to userDetails.uid,
            "deviceToken" to userDetails.deviceToken,
            "onlineStatus" to userDetails.onlineStatus
        )

        firestore.collection("Users").document(userDetails.uid)
            .update(user as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Details updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error updating user details", Toast.LENGTH_SHORT).show()
                enableUI()
            }
    }


    private fun uploadImage(uri: Uri, onComplete: (Uri) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            Log.d("TAG", "User not authenticated")
            enableUI()
            return
        }

        val storageRef = storage.reference.child("images/$userId")
        val uploadTask = storageRef.putFile(uri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result
                onComplete(downloadUrl)
            } else {
                Log.d("TAG", "Upload failed")
                enableUI()
            }
        }.addOnFailureListener { exception ->
            Log.d("TAG", "Upload error: ${exception.message}")
            enableUI()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                binding.profileImage.setImageURI(uri)
                downloadUri = uri
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImageFromGallery()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableUI() {
        binding.btn.isEnabled = true
        binding.loader.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("downloadUri", downloadUri)
    }
}
