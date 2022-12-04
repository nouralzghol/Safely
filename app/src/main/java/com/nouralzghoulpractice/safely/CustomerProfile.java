package com.nouralzghoulpractice.safely;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CustomerProfile extends AppCompatActivity {
    //1)identify the firebase var
    private DatabaseReference reference;  //to get the reference
    private String customerID;           //to store the user id in
    String name,phone,email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_customer_profile);

        //2)get the current user and the reference then store the current user id in the customerID
        customerID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference= FirebaseDatabase.getInstance().getReference("customers");


        //3)identify and initialize the text views
        TextView CustomerName=(TextView) findViewById(R.id.CustomerName);
        TextView CustomerPhoneNumber=(TextView) findViewById(R.id.CustomerPhoneNumber);
        TextView CustomerEmail=(TextView) findViewById(R.id.CustomerEmail);

        //4)start getting the customer info from the firebase using the reference

        reference.child(customerID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Map<String, Object> obj = (HashMap<String, Object>) snapshot.getValue();
                    if (obj==null)return;
                    name=obj.get("FullName").toString();
                    phone=obj.get("PhoneNumber").toString();
                    email=obj.get("Email").toString();

                    CustomerName.setText(name);
                    CustomerPhoneNumber.setText(phone);
                    CustomerEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
             Toast.makeText(CustomerProfile.this, "something went wrong !", Toast.LENGTH_LONG).show();

            }
        });


    }
}