package com.nouralzghoulpractice.safely;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

@RequiresApi(api = Build.VERSION_CODES.M)
public class Sign_up_cust extends AppCompatActivity implements View.OnClickListener {
    //first you setup the gradle scripts and connect with firebase console

    //2)identify texts& button
    private EditText textFirstName, textLastName, editTextPhone, editTextEmail, editTextPassword;
    private Button signUpButton;

    //4)identify firebase var and auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_cust);
        getSupportActionBar().hide();

        //3)initialize the variables
        textFirstName = (EditText) findViewById(R.id.textFirstName);
        textLastName = (EditText) findViewById(R.id.textLastName);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        signUpButton = (Button) findViewById(R.id.signUpButton);

        //5)initialize the authentication var
        mAuth = FirebaseAuth.getInstance();

        //6)set click listeners
        signUpButton.setOnClickListener(this);


    }

    //7)implement onClick method to make an action when you click in any button
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signUpButton:
                signUpCustomer();
                break;

        }
    }

    private void signUpCustomer() {
//8)get data from the user and store them in var
        String FirstName = textFirstName.getText().toString().trim();
        String LastName = textLastName.getText().toString().trim();
        //concatenation to store the full name together
        String FullName = FirstName + " " + LastName;
        String PhoneNumber = editTextPhone.getText().toString().trim();
        String Email = editTextEmail.getText().toString().trim();
        String Password = editTextPassword.getText().toString().trim();

        //9) validate your values before storing them in real time database
        if (FirstName.isEmpty() || LastName.isEmpty()) {
            textLastName.setError("Full name is required!");
            textFirstName.requestFocus();
            textLastName.requestFocus();
            return;
        }

        if (PhoneNumber.isEmpty()) {
            editTextPhone.setError("Phone number required!");
            editTextPhone.requestFocus();
            return;
        }
        if (PhoneNumber.length() != 10) {
            editTextPhone.setError("Phone number length must be 10 and start with 07 !");
            editTextPhone.requestFocus();
            return;
        }

        if (Email.isEmpty()) {
            editTextEmail.setError("Email Address required!");
            editTextEmail.requestFocus();
            return;
        }
//one more validation in email is to check the pattern of it so we can actually use it in login
        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            editTextEmail.setError("Please provide a valid email address!");
            editTextEmail.requestFocus();
            return;
        }

        if (Password.isEmpty()) {
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
            return;
        }
        //one more validation in Password we need to make sure that the password length
        //is not less than 6 because firebase won't accept it
        //but in our app we will make it 8
        if (Password.length() < 8) {
            editTextPassword.setError("Minimum Password length should be 8 characters!");
            editTextPassword.requestFocus();
            return;
        }

//10) now we will make java class that store the customer information
//and send the object to the firebase real time db (create customer java class)

//11) start the firebase authentication and storing in real time db
        mAuth.createUserWithEmailAndPassword(Email, Password)
                //check if the customer has been registered
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Customer customer = new Customer(FullName, PhoneNumber, Email, Password);

                            FirebaseDatabase.getInstance().getReference("customers")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(customer).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Sign_up_cust.this, "Customer has been Signed Up Successfully!", Toast.LENGTH_LONG).show();

                                    } else
                                        Toast.makeText(Sign_up_cust.this, "Failed to Sign Up! try again", Toast.LENGTH_LONG).show();


                                }
                            });
                        } else
                            Toast.makeText(Sign_up_cust.this, "Failed to Sign Up! try again", Toast.LENGTH_LONG).show();


                    }
                });

    }
}