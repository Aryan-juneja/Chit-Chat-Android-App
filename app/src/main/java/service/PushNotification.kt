package service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class pushNotification :FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        Log.d("TAG", "onNewToken: $token")
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("TAG", "onMessageReceived: ${message.data}")
        super.onMessageReceived(message)
    }
}