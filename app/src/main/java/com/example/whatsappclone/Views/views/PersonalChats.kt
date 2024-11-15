package com.example.whatsappclone.Views.views
import Adapters.ChatAdapter
import Models.DateHeader
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import Models.MessageDto
import Models.SharedViewModel
import Models.User
import Models.chatEvent
import Models.chats
import Models.messages
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.bumptech.glide.Glide
import com.example.whatsappclone.PushNotificationService
import com.example.whatsappclone.R
import com.example.whatsappclone.databinding.ActivityPersonalChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vanniktech.emoji.EmojiPopup
import com.zegocloud.uikit.internal.ZegoUIKitLanguage
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener
import com.zegocloud.uikit.prebuilt.call.event.SignalPluginConnectListener
import com.zegocloud.uikit.prebuilt.call.event.ZegoCallEndReason
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallType
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import im.zego.zim.enums.ZIMConnectionEvent
import im.zego.zim.enums.ZIMConnectionState
import kotlinx.coroutines.launch
import localService.PersonalizedChatsEntity
import localService.dbClass
import org.json.JSONObject
import service.RetrofitHelper
import timber.log.Timber
import utils.loadUserPreferences
import utils.sameDayAs
import java.io.File
import java.util.Collections


class PersonalChats : AppCompatActivity() {
    private val viewModel: SharedViewModel by viewModels()
    private lateinit var file: File
    private lateinit var roomDb:dbClass
    private lateinit var dbPersonalizedChats:List<PersonalizedChatsEntity>
    private val friendId by lazy {
        intent.getStringExtra("uid")
    }
    private val friendName by lazy {
        intent.getStringExtra("name")
    }
    private val friendImage by lazy {
        intent.getStringExtra("image")
    }

    private val currentUser by lazy {
        FirebaseAuth.getInstance().currentUser?.uid
    }
    private val user by lazy {
        FirebaseFirestore.getInstance()
    }
    private val db by lazy {
        FirebaseDatabase.getInstance()
    }
    private lateinit var chatadapter: ChatAdapter
    private val Messages = mutableListOf<chatEvent>() // This holds the chat events (messages)
    private lateinit var sender: User
    private lateinit var binding: ActivityPersonalChatsBinding
    private lateinit var emojiPopup: EmojiPopup

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        EmojiManager.install(IosEmojiProvider())
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("TAG", "First: $friendName $friendImage $friendId")
        binding = ActivityPersonalChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        emojiPopup = EmojiPopup(binding.root,binding.text)
        user?.let {
                initZegoInviteService(816828417,"49607b31f66b7ccf4660a542ca693a1d4b4d4cc85d449a7d986df3db586ef4b7",currentUser.toString(),currentUser.toString())
        }
        binding.emoji.setOnClickListener {
            emojiPopup.toggle()
        }
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
        val lastPref = loadUserPreferences(this)
            if (lastPref) {
                Toast.makeText(this, "Dark mode enabled", Toast.LENGTH_SHORT).show()
                binding.appBarLayout2.setBackgroundColor(Color.BLACK)
                binding.refresh.setBackgroundColor(Color.BLACK)
                binding.mssg.setBackgroundColor(Color.BLACK)
                binding.search.setBackgroundColor(Color.BLACK)
                binding.text.setTextColor(Color.WHITE)
                binding.text.setBackgroundColor(Color.BLACK)
            }
                setUpvoiceCall(friendId)
                setUpvideoCall(friendId)
        binding.refresh.setOnRefreshListener(object :OnRefreshListener{

            override fun onRefresh() {
                if(isNetworkAvailable(this@PersonalChats)){
                    binding.refresh.isRefreshing=true
                    Toast.makeText(this@PersonalChats,"Chats Refreshed Successfully",Toast.LENGTH_LONG).show()
                    binding.refresh.isRefreshing=false
                }
                else{
                    binding.refresh.isRefreshing=true
                    Toast.makeText(this@PersonalChats,"No Internet Connection",Toast.LENGTH_LONG).show()
                    binding.refresh.isRefreshing=false
                }
            }
        })
        roomDb =dbClass.getDatabase(this)
        currentUser?.let { uid ->
            user.collection("Users").document(uid).get().addOnSuccessListener {
                sender = it.toObject(User::class.java) ?: return@addOnSuccessListener
            }
        }
        readMessage()
        if(isNetworkAvailable(this)){
            listenForNewMessages()  // Fetch existing messages
        }
        else{
            fetchFromDb()
        }
         // Fetch existing messages

        // Set friend profile information
        binding.profileName.text = friendName
        Glide.with(this)
            .load(friendImage)
            .placeholder(R.drawable.man303792640)
            .into(binding.profileImage)

        // Initialize the adapter and RecyclerView
        Log.d("TAG", "onCreate: $Messages")
        chatadapter = ChatAdapter(Messages, currentUser.toString(), this )
        binding.rv.apply {
            layoutManager = LinearLayoutManager(this@PersonalChats)
            adapter = chatadapter
        }
        chatadapter.highFiveClick={ id,status->
            updateHighFive(id,status)
        }
        // Send message when the button is clicked
        binding.btn.setOnClickListener {
            val messageText = binding.text.text.toString().trim()
            Log.d("TAG", "onCreate: $messageText")
            if (messageText.isNotEmpty()) {
                Log.d("TAG", "First")
                sendMessage(messageText)
            } else {
                Toast.makeText(this, "Cannot send an empty message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpvoiceCall(friendId: String?) {
            binding.audio.apply {
                setIsVideoCall(false)
                resourceID="ZEGO_VOICE_"
                setInvitees(Collections.singletonList(ZegoUIKitUser(friendId,friendId)))
            }
    }
    private fun setUpvideoCall(friendId: String?) {
        binding.audio.apply {
            setIsVideoCall(true)
            resourceID="ZEGO_VOICE_"
            setInvitees(Collections.singletonList(ZegoUIKitUser(friendId,friendId)))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.file, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.file-> {
                CheckPermission(this)
                return true
            }
//            R.id.videoCall -> {
//                startVideoCall("your_inviter_user_id")
//                true
//            }
//            R.id.AudioCall ->{
//                return true
//            }
            else ->   return super.onOptionsItemSelected(item)
        }
    }

//    private fun startVideoCall(inviterUserID: String) {
//        val button = ZegoSendCallInvitationButton(this)
//        button.setCallType(ZegoCallType.Video) // Assuming you have a call type enum
//        button.inviterUserID = inviterUserID // Set the inviter user ID
//
//        // Optionally, configure other properties like invitees, call ID, etc.
//        button.callID = "ZEGO_CLOUD" // Your unique call ID
//        button.sendInvitation() // Trigger the invitation to start the call
//    }


    private fun uploadFile(fileUri: Uri, fileExtension: String?) {
        Log.d("TAG", "onActivityResult: $fileUri")
        val messageId = getMessages(friendId ?: return).push().key ?: return
        val fileName = System.currentTimeMillis().toString() // Unique file name
        val storageRef = FirebaseStorage.getInstance().reference.child("files/$fileName")
        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    Log.d("TAG", "onActivityResult: $downloadUrl")
                    val message = messages(downloadUrl.toString(), currentUser!!, messageId,"File",1,false)
                    Log.d("TAG", "sendMessage: $message")
                    getMessages(friendId!!).child(messageId).setValue(message).addOnSuccessListener {
                        Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show()
                        binding.text.text?.clear()  // Clear the input field
                        updateLastMessage(message)
                        sendNotification("FILE RECEIVED")
                        addToDb(message)
                    }.addOnFailureListener { e ->
                        Log.d("TAG", "Message sending failed: ${e.message}")
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "File upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data) // Call super at the beginning

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val fileUri = data.data
            if (fileUri != null) {
                Log.d("TAG", "onActivityResult: $fileUri")
                // Get the file extension
                val fileExtension = getFileExtensionFromUri(fileUri)
                Log.d("TAG", "File extension: $fileExtension")
                // Proceed with uploading the file
                uploadFile(fileUri,fileExtension)
            }
        }
    }

    // Function to get file extension from URI
    private fun getFileExtensionFromUri(uri: Uri): String? {
        // Use content resolver to get file type
        val contentResolver = contentResolver
        val mimeType = contentResolver.getType(uri)
        return mimeType?.substringAfterLast("/")
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // This allows all types of files; you can change it to specific MIME types like "image/*", "application/pdf", etc.
        }
        startActivityForResult(intent, 1)
    }
    private fun CheckPermission(personalChats: PersonalChats) {
        if(ContextCompat.checkSelfPermission(personalChats,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
           pickFile()
        }
        else{
            ActivityCompat.requestPermissions(this@PersonalChats, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),101)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFile()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchFromDb() {
        val id=getID(friendId.toString())
        Toast.makeText(this,"Kindly Connect To Internet",Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                dbPersonalizedChats =roomDb.personalizedChatsDao().getAllPersonalizedChats(id)
                for (msg in dbPersonalizedChats) {
                    val liked = msg.liked != 0
                    val Msg=messages(
                        msg.message,
                        msg.senderId,
                        msg.id,
                        msg.type,
                        1,
                        liked,
                        msg.sentAt
                    )
                    addMessage(Msg)
                }
            }
    }

    private fun readMessage() {
        getInbox(currentUser.toString(),friendId.toString()).child("count").setValue(0)
    }

    private fun updateHighFive(id: String, status: Boolean) {
            getMessages(friendId.toString()).child(id.toString()).updateChildren(mapOf("liked" to status))

    }
    private fun addToDb(mssg:messages){
        val liked = if(mssg.liked ==true) 1 else 0
        val id =getID(friendId.toString())
        val Msg =PersonalizedChatsEntity(
            mssg.msgId.toString(),
            id,
            mssg.senderId.toString(),
            mssg.mssg.toString(),
            liked,
            mssg.sentAt,
            mssg.type
        )
        lifecycleScope.launch {
            try {
                roomDb.personalizedChatsDao().insertPersonalizedChat(Msg)
                Log.d("TAG", "Message successfully added to Room DB")
            } catch (e: Exception) {
                Log.e("TAG", "Error adding message to Room DB: ${e.message}")
            }
        }
    }
    private fun clearDb() {
        lifecycleScope.launch {
            try {
                roomDb.personalizedChatsDao().deleteAllPersonalizedChats()
                Log.d("TAG", "All messages successfully deleted from Room DB")
            } catch (e: Exception) {
                Log.e("TAG", "Error deleting messages from Room DB: ${e.message}")
            }
        }
    }
    // Listens for new messages being added in real-time
    private fun listenForNewMessages() {
        getMessages(friendId.toString()).orderByKey().addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val mssg = snapshot.getValue(messages::class.java)
                Log.d("TAG", "Message fetched: $mssg")  // Debug log to check if the message is fetched
                if (mssg != null) {
                    addToDb(mssg)
//                    clearDb()
                    Log.d("TAG", "onChildAdded: ######################")
                    addMessage(mssg)

                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updatedMessage = snapshot.getValue(messages::class.java)
                if (updatedMessage != null) {
                    addToDb(updatedMessage)
                    updateMessageInRecyclerView(updatedMessage)
                }
            }

            private fun updateMessageInRecyclerView(updatedMessage: messages) {
                val messageIndex = Messages.indexOfFirst {
                    it is messages && it.msgId == updatedMessage.msgId
                }

                if (messageIndex != -1) {
                    Messages[messageIndex] = updatedMessage // Replace the message with the updated one
                    chatadapter.notifyItemChanged(messageIndex) // Notify the adapter that the item has changed
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle message removal if necessary
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle message move if necessary
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", "Error listening for messages: ${error.message}")
            }
        })
    }

    // Adds a message to the message list and updates the RecyclerView
    private fun addMessage(mssg: messages) {
        val lastMessage = Messages.lastOrNull()
        addToDb(mssg)
        // Only add a DateHeader if the last message exists and the dates are different
        if (lastMessage == null || !lastMessage.sentAt.sameDayAs(mssg.sentAt)) {
            Messages.add(DateHeader(mssg.sentAt, this))
        }

        Messages.add(mssg)  // Add the message to the list
        chatadapter.notifyItemInserted(Messages.size - 1)  // Notify the adapter of the new message
        binding.rv.scrollToPosition(Messages.size - 1)  // Scroll to the last message
    }


    // Sends a message to the database
    private fun sendMessage(messageText: String) {
        Log.d("TAG", "Second")
        val messageId = getMessages(friendId ?: return).push().key ?: return
        Log.d("TAG", "sendMessage: $messageId")
        val message = messages(messageText, currentUser!!, messageId)
        Log.d("TAG", "sendMessage: $message")
        getMessages(friendId!!).child(messageId).setValue(message).addOnSuccessListener {
            Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show()
            binding.text.text?.clear()  // Clear the input field
            updateLastMessage(message)
            sendNotification(messageText)
            addToDb(message)
        }.addOnFailureListener { e ->
            Log.d("TAG", "Message sending failed: ${e.message}")
        }
    }

    private fun sendNotification(messageText: String) {
        user.collection("Users").document(friendId.toString()).get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject(User::class.java)
            val mssg = user?.let {
                MessageDto(
                    it.deviceToken,
                    it.name,
                    messageText,
                    friendImage.toString()
                )
            }

            if (mssg != null) {
                lifecycleScope.launch {
                    try {
                        val response = RetrofitHelper.getInstance().sendNotification(mssg)
                        Log.d("TAG", "Notification sent successfully,${response.toString()}")
                    } catch (e: Exception) {
                        Log.e("TAG", "Error sending notification: ${e.message}")
                    }
                }
            } else {
                Log.e("TAG", "User or message is null")
            }
        }.addOnFailureListener { exception ->
            Log.e("TAG", "Error fetching user: ${exception.message}")
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


    @SuppressLint("LogNotTimber")
    fun initZegoInviteService(appID: Long, appSign: String, userID: String, userName: String) {
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        callInvitationConfig.translationText = ZegoTranslationText(ZegoUIKitLanguage.ENGLISH)
        callInvitationConfig.provider =
            ZegoUIKitPrebuiltCallConfigProvider { invitationData: ZegoCallInvitationData? ->
                ZegoUIKitPrebuiltCallInvitationConfig.generateDefaultConfig(
                    invitationData
                )
            }
        ZegoUIKitPrebuiltCallService.events.errorEventsListener =
            ErrorEventsListener { errorCode: Int, message: String ->
                Timber.d("onError() called with: errorCode = [$errorCode], message = [$message]")
            }
        ZegoUIKitPrebuiltCallService.events.invitationEvents.pluginConnectListener =
            SignalPluginConnectListener { state: ZIMConnectionState, event: ZIMConnectionEvent, extendedData: JSONObject ->
                Timber.d("onSignalPluginConnectionStateChanged() called with: state = [$state], event = [$event], extendedData = [$extendedData$]")
            }
        ZegoUIKitPrebuiltCallService.init(
            application, appID, appSign, userID, userName, callInvitationConfig
        )
        ZegoUIKitPrebuiltCallService.enableFCMPush()

        ZegoUIKitPrebuiltCallService.events.callEvents.callEndListener =
            CallEndListener { callEndReason: ZegoCallEndReason?, jsonObject: String? ->
                Log.d(
                    "CallEndListener",
                    "Call Ended with reason: $callEndReason and json: $jsonObject"
                )
            }
    }

    // Updates the last message for the chat (to show in the chats list)
    private fun updateLastMessage(message: messages) {
        Log.d("TAG", "updateLastMessage: ${message}")
        val chat = chats(message.mssg, friendId.toString()   , friendName!!, friendImage!!, count = 0)
        Log.d("TAG", "updateLastMessage:$chat")
        getInbox(currentUser!!, friendId!!).setValue(chat).addOnSuccessListener {
            Log.d("TAG", "updateLastMessage: DONE")
            getInbox(friendId!!, currentUser!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(chats::class.java)
                    Log.d("TAG", "onDataChange: ${snapshot.getValue(chats::class.java)}")
                    chat.apply {
                        from = message.senderId
                        name = sender.name
                        image = sender.imageUrl
                        count = 1
                    }
                    value?.let {
                        if (it.from == message.senderId) {
                            chat.count = value.count + 1
                        }
                    }
                    getInbox(friendId!!, currentUser!!).setValue(chat)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("TAG", "Error updating chat: ${error.message}")
                }
            })
        }
    }



    // References the messages node in Firebase
    private fun getMessages(friendId: String): DatabaseReference {
        return db.reference.child("messages/${getID(friendId)}")
    }

    // Combines the IDs to form the message path
    private fun getID(friendId: String): String {
        return if (friendId > currentUser!!) currentUser + friendId else friendId + currentUser
    }

    // References the inbox node in Firebase
    private fun getInbox(to: String, from: String): DatabaseReference {
        return db.reference.child("chats/$to/$from")
    }

}
