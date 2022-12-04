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

public class Login_cust extends AppCompatActivity implements View.OnClickListener{

    //1)identify texts& button
    private EditText editTextCustomerEmail,editTextCustomerPassword;
    private Button LoginCustomerButton;

    //3)identify firebase var and auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_cust);
        getSupportActionBar().hide();


        //2)initialize the variables
        editTextCustomerEmail=(EditText) findViewById(R.id.editTextCustomerEmail);
        editTextCustomerPassword=(EditText) findViewById(R.id.editTextCustomerPassword);

        LoginCustomerButton=(Button) findViewById(R.id.LoginCustomerButton);

        //4)initialize the authentication var
        mAuth=FirebaseAuth.getInstance();

        //5)set click listeners
        LoginCustomerButton.setOnClickListener(this);


    }

    //6)implement onClick method to make an action when you click in any button
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.LoginCustomerButton:
                LoginCustomer();
                break;

        }

    }

    private void LoginCustomer() {
//7)get data from the user and store them in var
        String CustomerEmail=editTextCustomerEmail.getText().toString().trim();
        String CustomerPassword=editTextCustomerPassword.getText().toString().trim();

        //8)check if the user give an empty credentials
        if(CustomerEmail.isEmpty()){
            editTextCustomerEmail.setError("Email address Required!");
            editTextCustomerEmail.requestFocus();
            return;
        }
        //one more validation in email is to check the pattern of it so we can actually use it in login
        if(!Patterns.EMAIL_ADDRESS.matcher(CustomerEmail).matches()){
            editTextCustomerEmail.setError("Please provide a valid email address!");
            editTextCustomerEmail.requestFocus();
            return;
        }

        if(CustomerPassword.isEmpty()){
            editTextCustomerPassword.setError("Password Required!");
            editTextCustomerPassword.requestFocus();
            return;
        }
//one more validation in Password we need to make sure that the password length
        //is not less than 8 as we asked in the registration process
        if(CustomerPassword.length()<8){
            editTextCustomerPassword.setError("Minimum Password length should be 8 Characters!");
            editTextCustomerPassword.requestFocus();
            return;
        }

//9) start the firebase authentication and signing in procedure by:
        //this method authenticate the customer and if the customer signed up before,so if that is true
        // and the task was successful we just go to the customer home page.
        mAuth.signInWithEmailAndPassword(CustomerEmail, CustomerPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                          //  FirebaseUser CUser=FirebaseAuth.getInstance().getCurrentUser();
                            //if(CUser.isEmailVerified()){
                                //redirect to the customer home page
                                startActivity(new Intent(Login_cust.this,Customer_HomePage.class));
                          //  }else{
                            //    CUser.sendEmailVerification();
                            //    Toast.makeText(Login_cust.this, "check your email to verify!", Toast.LENGTH_LONG).show();

                          //  }


                        }else{
                            Toast.makeText(Login_cust.this, "Failed to Login! Please check your email and password.", Toast.LENGTH_LONG).show();
                        }


                    }
                });




    }




}