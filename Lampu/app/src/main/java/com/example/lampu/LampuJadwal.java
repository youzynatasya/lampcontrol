package com.example.lampu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LampuJadwal extends AppCompatActivity {
    Button btnManual, btnOtomatis;
    private EditText editTextTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lampu_jadwal);

        btnManual = findViewById(R.id.btnManual);
        btnManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent LampuManual = new Intent(getApplicationContext(), LampuManual.class);
                startActivity(LampuManual);
            }
        });

        btnOtomatis = findViewById(R.id.btnOtomatis);
        btnOtomatis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent LampuOtomatis = new Intent(getApplicationContext(), LampuOtomatis.class);
                startActivity(LampuOtomatis);
            }
        });
        editTextTime = findViewById(R.id.timepickerstart);
        String url = "https://lampu-f8a57-default-rtdb.firebaseio.com/";
    }

    public void showTimePickerDialog(View view) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                editTextTime.setText(selectedTime);
                // Mengirim waktu yang dipilih ke Firebase
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("lampJad");
                databaseReference.setValue(selectedTime)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LampuJadwal.this, "Waktu berhasil dikirim ke Firebase", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LampuJadwal.this, "Gagal mengirim waktu ke Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }
}