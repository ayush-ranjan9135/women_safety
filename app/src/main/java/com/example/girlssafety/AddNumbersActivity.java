package com.example.girlssafety;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AddNumbersActivity extends AppCompatActivity {

    private LinearLayout numberContainer;
    private List<EditText> numberFields = new ArrayList<>();
    private Button btnNext;
    private List<String> emergencyNumbers = new ArrayList<>();
    private FirebaseFirestore db; // Firestore instance

    private final int REQUEST_SMS_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_numbers);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        numberContainer = findViewById(R.id.number_container);
        btnNext = findViewById(R.id.btn_next);

        // Initially add one number field
        addNumberField();

        // Add new number field on floating button click
        findViewById(R.id.fab_add_number).setOnClickListener(v -> addNumberField());

        // Next button to proceed to main activity
        btnNext.setOnClickListener(v -> {
            if (saveNumbers()) {
                // Save numbers to Firestore
                saveEmergencyNumbersToFirestore(emergencyNumbers);

                // Navigate to MainActivity
                Intent intent = new Intent(AddNumbersActivity.this, MainActivity.class);
                intent.putStringArrayListExtra("numbers", (ArrayList<String>) emergencyNumbers);
                startActivity(intent);
                finish();
            }
        });
    }

    private void addNumberField() {
        EditText editText = new EditText(this);
        editText.setHint("Enter emergency contact number");
        editText.setInputType(InputType.TYPE_CLASS_PHONE);
        numberContainer.addView(editText);
        numberFields.add(editText);
    }

    private boolean saveNumbers() {
        emergencyNumbers.clear();
        for (EditText field : numberFields) {
            String number = field.getText().toString().trim();
            if (!number.isEmpty()) {
                emergencyNumbers.add(number);
            }
        }
        if (emergencyNumbers.isEmpty()) {
            Toast.makeText(this, "Please add at least one number", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveEmergencyNumbersToFirestore(List<String> numbers) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for (String number : numbers) {
            Contact contact = new Contact(number); // Create a Contact object (ensure you have a Contact class)
            db.collection("emergencyContacts")
                    .document(userId) // Store all numbers for the user under their UID
                    .collection("contacts") // Sub-collection for user contacts
                    .add(contact) // Add contact to Firestore
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(this, "Contact added: " + number, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error adding contact: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void sendSms(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        } else {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(this, "SMS Sent!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "SMS failed, please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with sending SMS
            } else {
                Toast.makeText(this, "SMS permission is required to send messages", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
