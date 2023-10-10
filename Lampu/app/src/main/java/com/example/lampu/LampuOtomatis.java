package com.example.lampu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LampuOtomatis extends AppCompatActivity {
    Button btnJadwal, btnManual;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lampu_otomatis);

        btnJadwal = findViewById(R.id.btnJadwal);
        btnJadwal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent LampuJadwal = new Intent(getApplicationContext(), LampuJadwal.class);
                startActivity(LampuJadwal);
            }
        });

        btnManual = findViewById(R.id.btnManual);
        btnManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent LampuManual = new Intent(getApplicationContext(), LampuManual.class);
                startActivity(LampuManual);
            }
        });
    }
}