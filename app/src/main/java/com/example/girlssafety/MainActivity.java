package com.example.girlssafety;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 1;
    private ArrayList<String> emergencyNumbers;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // If the user is not logged in, redirect to LoginActivity
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;  // Make sure to stop further execution if not logged in
        }

        // Receive the emergency numbers from AddNumbersActivity
        Intent intent = getIntent();
        emergencyNumbers = intent.getStringArrayListExtra("numbers");

        Button btnSendMessage = findViewById(R.id.btn_send_message);
        Button btnEditNumbers = findViewById(R.id.btn_edit_numbers);

        btnSendMessage.setOnClickListener(v -> sendEmergencyMessage());
        btnEditNumbers.setOnClickListener(v -> {
            // Navigate back to AddNumbersActivity to edit numbers
            Intent editIntent = new Intent(MainActivity.this, AddNumbersActivity.class);
            startActivity(editIntent);
        });
    }

    private void sendEmergencyMessage() {
        String message = "Help! I am in an emergency. Please contact me.";

        // Check if SMS permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request SMS permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        } else {
            // Permission granted, send the emergency SMS
            try {
                SmsManager smsManager = SmsManager.getDefault();
                for (String number : emergencyNumbers) {
                    smsManager.sendTextMessage(number, null, message, null, null);
                }
                Toast.makeText(this, "Emergency message sent!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to send message, please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            // Handle the result of SMS permission request
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, send the SMS
                sendEmergencyMessage();
            } else {
                Toast.makeText(this, "SMS permission is required to send messages", Toast.LENGTH_SHORT).show();
            }
        }
    }
}