package com.example.lampu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.Slider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LampuManual extends AppCompatActivity {

    Button btnJadwal, btnOtomatis;
    Slider slider;
    FirebaseDatabase mDatabase;
    DatabaseReference dref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lampu_manual);

        btnJadwal = findViewById(R.id.btnJadwal);
        btnJadwal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent LampuJadwal = new Intent(getApplicationContext(), LampuJadwal.class);
                startActivity(LampuJadwal);
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

        // Inisialisasi Firebase
        String url = "https://lampu-f8a57-default-rtdb.firebaseio.com/";
        mDatabase = FirebaseDatabase.getInstance(url);
        dref = mDatabase.getReference();

        slider = findViewById(R.id.slider);

        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                int brightness = (int) value;
                dref.child("lampMan").setValue(brightness).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Tindakan yang dilakukan saat berhasil mengubah nilai di Firebase
                            // Misalnya, menampilkan pesan atau mengubah tampilan
                        }
                    }
                });
            }
        });
    }
}
