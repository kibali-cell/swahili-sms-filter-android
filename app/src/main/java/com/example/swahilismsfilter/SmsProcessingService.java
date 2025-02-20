package com.example.swahilismsfilter;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import android.app.NotificationManager;

public class SmsProcessingService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String message = intent.getStringExtra("message");
        Log.d("SmsProcessingService", "Received message: " + message);
        SpamClassifier.init(this); // Initialize with context
        boolean isSpam = classifyMessage(message);
        if (isSpam) {
            Log.d("SmsProcessingService", "Spam detected: " + message);
            showNotification("Spam Detected", message);
        } else {
            Log.d("SmsProcessingService", "Not spam: " + message);
        }
        return START_NOT_STICKY;
    }



    private void showNotification(String title, String text) {
        String channelId = "spam_channel";
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean classifyMessage(String message) {
        return SpamClassifier.classify(message);
    }

}

