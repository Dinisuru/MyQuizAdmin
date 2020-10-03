package com.example.myquizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText email , password;
    private Button login;
    private FirebaseAuth firebaseAuth;
    private Dialog loadingdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);

        loadingdialog = new Dialog(MainActivity.this);
        loadingdialog.setContentView(R.layout.loading_progressbar);
        loadingdialog.setCancelable(false);
        loadingdialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingdialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        firebaseAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadingdialog.show();

                String memail = email.getText().toString();
                String mpass = password.getText().toString();

                if(!memail.isEmpty() && !mpass.isEmpty()){

                    firebaseAuth.signInWithEmailAndPassword(memail,mpass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                //sign in complete
                                //Toast.makeText(MainActivity.this,"Logged in Successfull!",Toast.LENGTH_SHORT).show();
                                //Intent intent = new Intent(MainActivity.this,CategoryActivity.class);
                                startActivity(intent);
                                loadingdialog.dismiss();
                                finish();

                            }else{

                                Toast.makeText(MainActivity.this,"Something went Worng",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
                            loadingdialog.dismiss();

                        }
                    }).addOnCanceledListener(new OnCanceledListener() {
                        @Override
                        public void onCanceled() {
                            Toast.makeText(MainActivity.this,"Canceled",Toast.LENGTH_SHORT).show();
                            loadingdialog.dismiss();
                        }
                    });

                    }else{
                    Toast.makeText(MainActivity.this,"Enter values",Toast.LENGTH_SHORT).show();
                    loadingdialog.dismiss();
                }

            }
        });

        if(firebaseAuth.getCurrentUser() != null){
            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
            startActivity(intent);
            finish();
        }

    }

}