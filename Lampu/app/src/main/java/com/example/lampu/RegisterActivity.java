package com.example.lampu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private EditText editNama, editEmail, editPassword;
    private Button btnRegister;
    private TextView btnlog;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editNama = findViewById(R.id.txtNama);
        editEmail = findViewById(R.id.txtEmail);
        editPassword = findViewById(R.id.txtPassword);
        btnRegister = findViewById(R.id.buttdaftar);
        btnlog = findViewById(R.id.txtMasuk);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Silahkan Tunggu");
        progressDialog.setCancelable(false);

        btnlog.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            if (editNama.getText().length() > 0 && editEmail.getText().length() > 0 && editPassword.getText().length() > 0) {
                register(editNama.getText().toString(), editEmail.getText().toString(), editPassword.getText().toString());
            } else {
                Toast.makeText(getApplicationContext(), "Data wajib diisi!", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void register (String txtNama, String txtEmail, String txtPassword){
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(txtEmail,txtPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser != null) {
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setDisplayName(txtNama)
                                .build();
                        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Task<Void> task) {
                                reload();
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Register Gagal", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    private void reload(){
        startActivity(new Intent(getApplicationContext(), HalamanUtama.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            reload();
        }
    }

}