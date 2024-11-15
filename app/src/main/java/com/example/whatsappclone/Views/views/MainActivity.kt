package com.example.whatsappclone.Views.views

import Adapters.ScreenSliderAdapter
import Models.SharedViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.whatsappclone.PushNotificationService
import com.example.whatsappclone.R
import com.example.whatsappclone.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.permissionx.guolindev.PermissionX
import com.vanniktech.ui.backgroundColor
import com.zegocloud.uikit.internal.ZegoUIKitLanguage
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener
import com.zegocloud.uikit.prebuilt.call.event.SignalPluginConnectListener
import com.zegocloud.uikit.prebuilt.call.event.ZegoCallEndReason
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import im.zego.zim.enums.ZIMConnectionEvent
import im.zego.zim.enums.ZIMConnectionState
import kotlinx.coroutines.launch
import localService.dbClass
import org.json.JSONObject
import timber.log.Timber
import utils.loadUserPreferences
import utils.saveUserPreferences


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val viewModel: SharedViewModel by viewModels()
    private lateinit var roomDb:dbClass
    val REQUEST_NOTIFICATION_PERMISSION: Int = 1
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val lastPref = loadUserPreferences(this@MainActivity)
        if(lastPref){
            viewModel.toggleDarkMode()
        }
        setContentView(binding.root)
        permissionHandling(this)
        setSupportActionBar(binding.topAppBar)
        requestNotificationPermission()
        roomDb = dbClass.getDatabase(this)
         // Initialize FirebaseAuth
        binding.viewPagerContainer.adapter = ScreenSliderAdapter(this)
        createFCMToken()
        viewModel.darkMode.observe(this) { isDarkMode ->
            if(isDarkMode){
                binding.appBarLayout.setBackgroundColor(Color.BLACK)
                binding. tabLayout.setBackgroundColor(Color.BLACK)
                binding. tabLayout.setTabTextColors(Color.WHITE, Color.WHITE)
                binding.tabLayout.requestLayout()
                binding.topAppBar.setTitleTextColor(Color.WHITE)
                 binding.viewPagerContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
            }
            else{
                binding.appBarLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.cyan))
                binding.tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.cyan))
                binding.tabLayout.setTabTextColors(
                    ContextCompat.getColor(this, R.color.black),  // Color for unselected tabs
                    ContextCompat.getColor(this, R.color.black)   // Color for selected tab
                )
                binding.viewPagerContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.white))

            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPagerContainer) { tab, position ->
            when(position){
                0->{
                    tab.text ="Chats"
                }
                else->{
                    tab.text ="People"
                }
            }
        }.attach()
        binding.tabLayout.setTabTextColors(Color.BLACK, Color.WHITE)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission if it is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(android.Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        val item = menu?.findItem(R.id.search)
        val searchView = item?.actionView as SearchView

        // Set hint text and icon color
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(Color.WHITE)
        searchEditText.setHintTextColor(Color.WHITE)
        val lastPref = loadUserPreferences(this@MainActivity)
        val toggleItem = menu.findItem(R.id.toggle)
        toggleItem.title = if (lastPref) {
            "Disable Dark Mode"
        } else {
            "Enable Dark Mode"
        }
        // Set search icon color
        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                Log.d("TAG", "onMenuItemActionCollapse: ")
                viewModel.setRefetch(true)
                return true
            }
        })
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.setRefetch(true) // Clear the search and notify the ViewModel
            }
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.toString())
                Toast.makeText(this@MainActivity, "Searching for: $newText", Toast.LENGTH_SHORT).show()
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                    auth.signOut()
                Toast.makeText(this@MainActivity,"Logout Successfully", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    roomDb.chatsDao().deleteAllChats()
                    roomDb.personalizedChatsDao().deleteAllPersonalizedChats()
                }
                val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                        true
            }
            R.id.Details_update ->{
                val intent = Intent(this, UpdateDetails::class.java)
                startActivity(intent)
                true
            }
            R.id.toggle -> {

                viewModel.toggleDarkMode()
                viewModel.darkMode.observe(this) { isDarkMode ->
                    saveUserPreferences(this@MainActivity, isDarkMode)
                    item.title = if (isDarkMode == true) {
                        "Disable Dark Mode"
                    } else {
                        "Enable Dark Mode"
                    }
                }
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onResume() {
        super.onResume()
        PushNotificationService.isAppInForeground = true // App is in foreground
    }

    override fun onPause() {
        super.onPause()
        PushNotificationService.isAppInForeground = false // App is in background
    }

    fun createFCMToken(){
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful) {
                    Log.w(
                        "FCM Token",
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@addOnCompleteListener
                }
                val token = task.result
                auth.currentUser?.let {
                    FirebaseFirestore.getInstance().collection("Users").document(
                        it.uid).update("deviceToken",token)
                }
                Log.d("FCM Token", token!!)
            }
    }
    private fun permissionHandling(activityContext: FragmentActivity) {
        PermissionX.init(activityContext).permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
            .onExplainRequestReason { scope, deniedList ->
                val message =
                    "We need your consent for the following permissions in order to use the offline call function properly"
                scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny")
            }.request { allGranted, grantedList, deniedList -> }
    }

}
