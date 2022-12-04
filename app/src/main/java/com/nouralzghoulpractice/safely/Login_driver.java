package com.nouralzghoulpractice.safely;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login_driver extends AppCompatActivity implements View.OnClickListener{

    //1)identify texts& button
    private EditText editTextDriverEmail,editTextDriverPassword;
    private Button LoginDriverButton;

    //3)identify firebase var and auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_driver);
        getSupportActionBar().hide();


        //2)initialize the variables
        editTextDriverEmail=(EditText) findViewById(R.id.editTextDriverEmail);
        editTextDriverPassword=(EditText) findViewById(R.id.editTextDriverPassword);

        LoginDriverButton=(Button) findViewById(R.id.LoginDriverButton);

        //4)initialize the authentication var
        mAuth=FirebaseAuth.getInstance();

        //5)set click listeners
        LoginDriverButton.setOnClickListener(this);


    }

    //6)implement onClick method to make an action when you click in any button
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.LoginDriverButton:
                LoginDriver();
                break;

        }
    }

    private void LoginDriver() {
//7)get data from the user and store them in var
        String DriverEmail=editTextDriverEmail.getText().toString().trim();
        String DriverPassword=editTextDriverPassword.getText().toString().trim();

        //8)check if the user give an empty credentials
        if(DriverEmail.isEmpty()){
            editTextDriverEmail.setError("Email address Required!");
            editTextDriverEmail.requestFocus();
            return;
        }
        //one more validation in email is to check the pattern of it so we can actually use it in login
        if(!Patterns.EMAIL_ADDRESS.matcher(DriverEmail).matches()){
            editTextDriverEmail.setError("Please provide a valid email address!");
            editTextDriverEmail.requestFocus();
            return;
        }

        if(DriverPassword.isEmpty()){
            editTextDriverPassword.setError("Password Required!");
            editTextDriverPassword.requestFocus();
            return;
        }
//one more validation in Password we need to make sure that the password length
        //is not less than 8 as we asked in the registration process
        if(DriverPassword.length()<8){
            editTextDriverPassword.setError("Minimum Password length should be 8 Characters!");
            editTextDriverPassword.requestFocus();
            return;
        }

//9) start the firebase authentication and signing in procedure by:
        //this method authenticate the driver and if the driver signed up before,so if that is true
        // and the task was successful we just go to the driver home page.
        mAuth.signInWithEmailAndPassword(DriverEmail, DriverPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                        //    FirebaseUser DUser=FirebaseAuth.getInstance().getCurrentUser();
                         //   if(DUser.isEmailVerified()){
                                //redirect to the Driver home page
                                startActivity(new Intent(Login_driver.this,Driver_HomePage.class));
                         //   }else{
                         //       DUser.sendEmailVerification();
                          //      Toast.makeText(Login_driver.this, "check your email to verify!", Toast.LENGTH_LONG).show();

                         //   }

                        }else{
                            Toast.makeText(Login_driver.this, "Failed to Login! Please check your email and password.", Toast.LENGTH_LONG).show();
                        }


                    }
                });





    }
}