package com.nouralzghoulpractice.safely;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Car_info extends AppCompatActivity {
    //1)identify texts& button
    private EditText textCarName,textCarColor,textCarModel,textCarNumber,textExpDate;

   public String FullName,PhoneNumber,Email,Age,licenceExp,Password;

    private Button signUpDriverButton;


    //3)identify firebase var and auth
    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info);
        getSupportActionBar().hide();

        //2)initialize the variables
        textCarName =(EditText) findViewById(R.id.textCarName);
        textCarColor=(EditText) findViewById(R.id.textCarColor);
        textCarModel=(EditText) findViewById(R.id.textCarModel);
        textCarNumber=(EditText) findViewById(R.id.textCarNumber);
        textExpDate=(EditText) findViewById(R.id.textExpDate);



        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("drivers").child(userID).child("carInfo");


        signUpDriverButton=(Button) findViewById(R.id.signUpDriverButton);
        //set click listeners for signUpDriverButton
        signUpDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FinalSignUpDriver();
            }
        });

    }



    private void FinalSignUpDriver() {


        //8)get car data from driver and store them in var
        String CarName=textCarName.getText().toString().trim();
        String CarColor=textCarColor.getText().toString().trim();
        String CarModel=textCarModel.getText().toString().trim();
        String CarNumber=textCarNumber.getText().toString().trim();
        String CarLicenceExpDate=textExpDate.getText().toString().trim();


//9) validate your values before storing them in real time database
        if (CarName.isEmpty()) {
            textCarName.setError("Car name is required!");
            textCarName.requestFocus();
            return;
        }

        if (CarColor.isEmpty()) {
            textCarColor.setError("Cra Color is required!");
            textCarColor.requestFocus();
            return;
        }

        if (CarModel.isEmpty()) {
            textCarModel.setError("Car Model is required!");
            textCarModel.requestFocus();
            return;
        }

        if (CarNumber.isEmpty()) {
            textCarNumber.setError("Car Number is required!");
            textCarNumber.requestFocus();
            return;
        }
        if (CarNumber.length()!=8) {
            textCarNumber.setError("Enter the right Car Number like (12-45678)!");
            textCarNumber.requestFocus();
            return;
        }

        if (CarLicenceExpDate.isEmpty()) {
            textExpDate.setError("Driver Licence Expiry Date is required!");
            textExpDate.requestFocus();
            return;
        }


//10) store the data of the car in the real time db with the same id of the current driver
        Log.i("info", "CarName "+CarName);
        Map car = new HashMap();

        car.put("CarName",CarName);
        car.put("CarColor",CarColor);
        car.put("CarModel",CarModel);
        car.put("CarNumber",CarNumber);
        car.put("CarLicenceExpDate",CarLicenceExpDate);

        mDriverDatabase.updateChildren(car);

        startActivity(new Intent(Car_info.this,MainActivity.class));







    }
}