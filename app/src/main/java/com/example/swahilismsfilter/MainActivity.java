package com.example.swahilismsfilter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;

    // UI Elements
    private TextView statusText;
    private TextView resultText;
    private TextView spamCountText;
    private TextView totalMessagesText;
    private EditText messageInput;
    private EditText senderInput;
    private Button testButton;
    private Button languageButton;
    private RecyclerView historyRecyclerView;

    // Database and Adapters
    private MessageDatabaseHelper dbHelper;
    private MessageHistoryAdapter historyAdapter;

    // Preferences for storing statistics and language
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale(); // Load saved language before setting content view
        setContentView(R.layout.activity_main);

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this);

        // Initialize UI elements and database
        initializeViews();
        initializeDatabase();

        // Initialize SharedPreferences
        preferences = getSharedPreferences("SpamFilterPrefs", MODE_PRIVATE);

        // Check and request permissions
        checkPermissions();

        // Initialize SpamClassifier
        SpamClassifier.init(this);

        // Set up click listeners
        setupClickListeners();

        // Update statistics display
        updateStatistics();

        // Load message history
        loadMessageHistory();
    }

    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        resultText = findViewById(R.id.resultText);
        spamCountText = findViewById(R.id.spamCountText);
        totalMessagesText = findViewById(R.id.totalMessagesText);
        messageInput = findViewById(R.id.messageInput);
        senderInput = findViewById(R.id.senderInput);
        testButton = findViewById(R.id.testButton);
        languageButton = findViewById(R.id.languageButton);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);

        // Set up RecyclerView
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new MessageHistoryAdapter();
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void initializeDatabase() {
        dbHelper = new MessageDatabaseHelper(this);
    }

    private void setupClickListeners() {
        // Existing click listeners
        testButton.setOnClickListener(v -> testMessage());
        languageButton.setOnClickListener(v -> changeLanguage());

        // Add new clear history click listener
        Button clearHistoryButton = findViewById(R.id.clearHistoryButton);
        clearHistoryButton.setOnClickListener(v -> clearMessageHistory());
    }

    private void changeLanguage() {
        String currentLang = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("language", "en");

        // Toggle between English and Swahili
        String newLang = currentLang.equals("en") ? "sw" : "en";

        // Save new language preference
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("language", newLang);
        editor.apply();

        // Update locale
        setLocale(newLang);

        // Restart activity properly
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

//    private void setLocale(String language) {
//        Locale locale = new Locale(language);
//        Locale.setDefault(locale);
//        Configuration configuration = new Configuration();
//        configuration.setLocale(locale);
//        getBaseContext().getResources().updateConfiguration(configuration,
//                getBaseContext().getResources().getDisplayMetrics());
//    }

    private void loadLocale() {
        String language = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("language", "en");
        setLocale(language);
    }

    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void testMessage() {
        String message = messageInput.getText().toString();
        String sender = senderInput.getText().toString();

        if (message.isEmpty() || sender.isEmpty()) {
            Toast.makeText(this, "Please enter both message and sender number", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isSpam = SpamClassifier.classify(message);
        resultText.setVisibility(View.VISIBLE);

        if (isSpam) {
            resultText.setText(R.string.result_spam);
            resultText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            resultText.setText(R.string.result_ham);
            resultText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        // Save message to database
        dbHelper.addMessage(sender, message, isSpam);

        // Update statistics and history
        updateStatisticsCount(isSpam);
        loadMessageHistory();

        // Clear input fields
        messageInput.setText("");
        senderInput.setText("");
    }

    private void loadMessageHistory() {
        Cursor cursor = dbHelper.getAllMessages();
        historyAdapter.swapCursor(cursor);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                    PERMISSION_REQUEST_CODE);

            statusText.setText(R.string.status_inactive);
            statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            statusText.setText(R.string.status_active);
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void updateStatistics() {
        int spamCount = preferences.getInt("spam_count", 0);
        int totalCount = preferences.getInt("total_count", 0);
        spamCountText.setText(getString(R.string.spam_detected, spamCount));
        totalMessagesText.setText(getString(R.string.total_messages, totalCount));
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
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                statusText.setText(R.string.status_active);
                statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                statusText.setText(R.string.status_inactive);
                statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }



    private void clearMessageHistory() {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_history)
                .setMessage("Are you sure you want to clear all message history?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear database
                    dbHelper.clearAllMessages();

                    // Reset statistics
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("spam_count", 0);
                    editor.putInt("total_count", 0);
                    editor.apply();

                    // Update UI
                    loadMessageHistory();
                    updateStatistics();

                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}