package com.example.lampu;

import static android.app.ProgressDialog.show;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText editEmail, editPassword;
    private Button btnlogin;
    private TextView btnDaftar;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editEmail = findViewById(R.id.txtEmail);
        editPassword = findViewById(R.id.txtPassword);
        btnlogin = findViewById(R.id.buttlogin);
        btnDaftar = findViewById(R.id.txtDaftar);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Silahkan Tunggu");
        progressDialog.setCancelable(false);

        btnDaftar.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        });

        btnlogin.setOnClickListener(v -> {
            if(editEmail.getText().length()>0 && editPassword.getText().length()>0) {
                login(editEmail.getText().toString(), editPassword.getText().toString());
            }else {
                Toast.makeText(getApplicationContext(),"Data wajib diisi!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login(String txtEmail, String txtPassword){
        //coding login
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(txtEmail, txtPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful() && task.getResult()!=null) {
                    if (task.getResult().getUser()!=null){
                        reload();
                    }else {
                        Toast.makeText(getApplicationContext(), "Register Gagal", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "Login Gagal", Toast.LENGTH_SHORT).show();
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
