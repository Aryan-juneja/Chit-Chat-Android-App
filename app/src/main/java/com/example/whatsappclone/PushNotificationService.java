package com.example.whatsappclone;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.whatsappclone.Views.views.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        if (Build.VERSION.SDK_INT >= 34 && getApplicationInfo().targetSdkVersion >= 34) {
            return super.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            return super.registerReceiver(receiver, filter);
        }
    }

    private static final String CHANNEL_ID = "test";
    public static boolean isAppInForeground = false; // Flag to track app state
    @Override
    public void onNewToken(String token) {
        Log.d("TAG", "onNewToken: ");
        Log.d("FCM Token", "Token: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("hunter", "onMessageReceived: ");

        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String image = remoteMessage.getData().get("image");
            if (title != null && body != null) {
                showNotification(title, body, image);
            } else {
                Log.e("hunter", "Title or body is null in the data message.");
            }
        } else {
            Log.e("hunter", "Message does not contain data.");
        }
    }

    private void showNotification(String title, String message, String imageUrl) {
        createNotificationChannel();
        // Create an intent for MainActivity to be launched when the button is clicked
        Intent intent = new Intent(PushNotificationService.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear any existing tasks and start new

        // Create a pending intent that launches MainActivity
        PendingIntent pendingIntent = PendingIntent.getActivity(
                PushNotificationService.this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        @SuppressLint("RemoteViewLayout")
        RemoteViews collapseView = new RemoteViews(getPackageName(), R.layout.collapsemode);
        collapseView.setTextViewText(R.id.TITLE, title);
        collapseView.setTextViewText(R.id.desc, message);

        // Set the pending intent to the button click
        collapseView.setOnClickPendingIntent(R.id.btnnnnnnnnnnnnnnnnnnnnnnnn, pendingIntent);

        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        collapseView.setImageViewBitmap(R.id.profile_image, resource);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(PushNotificationService.this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.baseline_message_24)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setCustomBigContentView(collapseView)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent); // Set the intent for launching MainActivity on notification tap

                        NotificationManagerCompat manager = NotificationManagerCompat.from(PushNotificationService.this);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ActivityCompat.checkSelfPermission(PushNotificationService.this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                manager.notify(101, builder.build());
                            }
                        } else {
                            manager.notify(101, builder.build());
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle placeholder cleanup if necessary
                    }
                });
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel description");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}

class replyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("hunter", "Reply received");
    }

}
