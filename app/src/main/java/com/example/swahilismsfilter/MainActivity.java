package com.example.swahilismsfilter;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;

    // UI Elements
    private TextView statusText;
    private TextView resultText;
    private TextView spamCountText;
    private TextView totalMessagesText;
    private EditText messageInput;
    private Button testButton;

    // Preferences for storing statistics
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize notification channel (from your existing code)
        NotificationHelper.createNotificationChannel(this);

        // Initialize UI elements
        initializeViews();

        // Initialize SharedPreferences
        preferences = getSharedPreferences("SpamFilterPrefs", MODE_PRIVATE);

        // Check and request permissions
        checkPermissions();

        // Initialize SpamClassifier
        SpamClassifier.init(this);

        // Set up test button click listener
        testButton.setOnClickListener(v -> testMessage());

        // Update statistics display
        updateStatistics();
    }

    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        resultText = findViewById(R.id.resultText);
        spamCountText = findViewById(R.id.spamCountText);
        totalMessagesText = findViewById(R.id.totalMessagesText);
        messageInput = findViewById(R.id.messageInput);
        testButton = findViewById(R.id.testButton);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                    PERMISSION_REQUEST_CODE);

            statusText.setText("Waiting for Permissions");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            statusText.setText("Active");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void testMessage() {
        String message = messageInput.getText().toString();
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isSpam = SpamClassifier.classify(message);
        resultText.setVisibility(View.VISIBLE);

        if (isSpam) {
            resultText.setText("Result: This message appears to be SPAM");
            resultText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            resultText.setText("Result: This message appears to be HAM (not spam)");
            resultText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        // Update statistics for test messages
        updateStatisticsCount(isSpam);
    }

    private void updateStatistics() {
        int spamCount = preferences.getInt("spam_count", 0);
        int totalCount = preferences.getInt("total_count", 0);
        spamCountText.setText("Spam messages detected: " + spamCount);
        totalMessagesText.setText("Total messages processed: " + totalCount);
    }

    private void updateStatisticsCount(boolean isSpam) {
        SharedPreferences.Editor editor = preferences.edit();
        int spamCount = preferences.getInt("spam_count", 0);
        int totalCount = preferences.getInt("total_count", 0);

        if (isSpam) {
            editor.putInt("spam_count", spamCount + 1);
        }
        editor.putInt("total_count", totalCount + 1);
        editor.apply();

        updateStatistics();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                statusText.setText("Active");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                statusText.setText("SMS Permissions Required");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }
}