package com.nouralzghoulpractice.safely;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;


public class Sign_up_driver extends AppCompatActivity {

    //1)identify texts& button
    EditText editTextFirstNameDriver;
    EditText editTextLastNameDriver;
    EditText editTextPhoneNumberDriver;
    EditText editTextEmailDriver;
    EditText editTextAgeDriver ;
    EditText editTextLicenceExpDate;
    EditText editTextPasswordDriver;

    private FirebaseAuth mAuth;

    Button ContinueButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_driver);
        getSupportActionBar().hide();

        //2)initialize the variables
        editTextFirstNameDriver =(EditText) findViewById(R.id.editTextFirstNameDriver);
        editTextLastNameDriver=(EditText) findViewById(R.id.editTextLastNameDriver);
        editTextPhoneNumberDriver=(EditText) findViewById(R.id.editTextPhoneNumberDriver);
        editTextEmailDriver=(EditText) findViewById(R.id.editTextEmailDriver);
        editTextAgeDriver=(EditText) findViewById(R.id.editTextAgeDriver);
        editTextLicenceExpDate=(EditText) findViewById(R.id.editTextLicenceExpDate);
        editTextPasswordDriver=(EditText) findViewById(R.id.editTextPasswordDriver);



        mAuth=FirebaseAuth.getInstance();


        ContinueButton=(Button) findViewById(R.id.ContinueButton);

        //3)set click listeners for continueButton
        ContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpDriver();
            }
        });




    }


    private void signUpDriver() {
        //5)get data from the user and store them in var
        String FirstName = editTextFirstNameDriver.getText().toString().trim();
        String LastName = editTextLastNameDriver.getText().toString().trim();
        //concatenation to store the full name together
        String FullName = FirstName + " " + LastName;

        String PhoneNumber = editTextPhoneNumberDriver.getText().toString().trim();
        String Email = editTextEmailDriver.getText().toString().trim();
        String Age = editTextAgeDriver.getText().toString().trim();
        String licenceExp = editTextLicenceExpDate.getText().toString().trim();
        String Password = editTextPasswordDriver.getText().toString().trim();

        //6) validate your values before storing them in real time database
        if (FirstName.isEmpty() || LastName.isEmpty()) {
            editTextFirstNameDriver.setError("Full name is required!");
            editTextFirstNameDriver.requestFocus();
            editTextLastNameDriver.requestFocus();
            return;
        }

        if (PhoneNumber.isEmpty()) {
            editTextPhoneNumberDriver.setError("Phone number required!");
            editTextPhoneNumberDriver.requestFocus();
            return;
        }
        if (PhoneNumber.length() != 10) {
            editTextPhoneNumberDriver.setError("Phone number length must be 10 and start with 07 !");
            editTextPhoneNumberDriver.requestFocus();
            return;
        }

        if (Email.isEmpty()) {
            editTextEmailDriver.setError("Email Address required!");
            editTextEmailDriver.requestFocus();
            return;
        }
//one more validation in email is to check the pattern of it so we can actually use it in login
        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            editTextEmailDriver.setError("Please provide a valid email address!");
            editTextEmailDriver.requestFocus();
            return;
        }

        if (Age.isEmpty()) {
            editTextAgeDriver.setError("Age is required!");
            editTextAgeDriver.requestFocus();
            return;
        }
        if (Age.length() < 2) {
            editTextAgeDriver.setError("accepted Age is between 22-45!");
            editTextAgeDriver.requestFocus();
            return;
        }


        if (Password.isEmpty()) {
            editTextPasswordDriver.setError("Password is required!");
            editTextPasswordDriver.requestFocus();
            return;
        }
        //one more validation in Password we need to make sure that the password length
        //is not less than 6 because firebase won't accept it
        //but in our app we will make it 8
        if (Password.length() < 8) {
            editTextPasswordDriver.setError("Minimum Password length should be 8 characters!");
            editTextPasswordDriver.requestFocus();
            return;
        }


        Log.i("info", "FullName "+FullName);
        Log.i("info", "Password "+Password);


//10) start the firebase authentication and storing in real time db
        mAuth.createUserWithEmailAndPassword(Email, Password)
                //check if the customer has been registered
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                     Driver driverInfo=new Driver(FullName, PhoneNumber,Email,Age,licenceExp,Password);
                            FirebaseDatabase.getInstance().getReference("drivers")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(driverInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(Sign_up_driver.this, "great! now enter your car information.", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(Sign_up_driver.this,Car_info.class));
                                    }else
                                        Toast.makeText(Sign_up_driver.this, "Failed to Sign Up! try again", Toast.LENGTH_LONG).show();


                                }
                            });
                        }else
                            Toast.makeText(Sign_up_driver.this, "Failed to Sign Up! try again", Toast.LENGTH_LONG).show();


                    }
                });







    }
}