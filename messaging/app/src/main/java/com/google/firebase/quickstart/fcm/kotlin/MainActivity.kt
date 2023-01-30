package com.google.firebase.quickstart.fcm.kotlin

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.quickstart.fcm.R
import com.google.firebase.quickstart.fcm.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(this, "FCM can't post notifications without POST_NOTIFICATIONS permission",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW))
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras?.get(key)
                Log.d(TAG, "Key: $key Value: $value")
            }
        }
        // [END handle_data_extras]

        binding.subscribeButton.setOnClickListener {
            Log.d(TAG, "Subscribing to weather topic")
            // [START subscribe_topics]
            Firebase.messaging.subscribeToTopic("weather")
                    .addOnCompleteListener { task ->
                        var msg = getString(R.string.msg_subscribed)
                        if (!task.isSuccessful) {
                            msg = getString(R.string.msg_subscribe_failed)
                        }
                        Log.d(TAG, msg)
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
            // [END subscribe_topics]
        }

        binding.logTokenButton.setOnClickListener {
            // Get token
            // [START log_reg_token]
            GlobalScope.launch {
                val token = getAndStoreRegToken()
                // Log and toast
                Log.d(TAG, "on click listener: $token")
                val deviceToken = hashMapOf(
                    "token" to token,
                    "timestamp" to FieldValue.serverTimestamp(),
                )

                // Get user ID from Firebase Auth or your own server
                Firebase.firestore.collection("fcmTokens").document("myuserid")
                    .set(deviceToken)
            }
            // [END log_reg_token]
        }

        Toast.makeText(this, "See README for setup instructions", Toast.LENGTH_SHORT).show()
        askNotificationPermission()

        // In the app’s first Activity
        val preferences = this.getPreferences(Context.MODE_PRIVATE)
        val lastRefreshLong = preferences.getLong("lastRefreshDate", -1)
        Log.d(TAG, "last refresh in preferences: $lastRefreshLong")
        GlobalScope.launch {
            val document = Firebase.firestore.collection("refresh").document("refreshDate").get().await()
            val updatedTime = (document.data!!["lastRefreshDate"] as Timestamp).seconds * 1000
            Log.d(TAG, "updatedTime: $updatedTime from Firestore")
            val lastRefreshDate = Date(if (lastRefreshLong == -1L) updatedTime else lastRefreshLong)
            val c = Calendar.getInstance()
            c.time = lastRefreshDate
            c.add(Calendar.DATE, 30)
            val today = Date()
            if (today.after(c.time)) {
                // get token and store into Firestore (see function above)
                getAndStoreRegToken()
                // update device cache
                Log.d(TAG, "get registration token and store")
                preferences.edit().putLong("lastRefreshDate", updatedTime)
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private suspend fun getAndStoreRegToken(): String {
        var token = ""
        runBlocking {
            token = Firebase.messaging.token.await()
            Log.d(TAG, "getRegistrationToken(): $token")
            // Add token and timestamp to Firestore for this user
            val deviceToken = hashMapOf(
                "token" to token,
                "timestamp" to FieldValue.serverTimestamp(),
            )

            // Get user ID from Firebase Auth or your own server
            Firebase.firestore.collection("fcmTokens").document("myuserid")
                .set(deviceToken)
        }
        return token
    }

    companion object {

        private const val TAG = "MainActivity"
    }
}
