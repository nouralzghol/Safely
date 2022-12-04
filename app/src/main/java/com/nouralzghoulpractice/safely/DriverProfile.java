 package com.nouralzghoulpractice.safely;


 import androidx.annotation.NonNull;
 import androidx.appcompat.app.AppCompatActivity;

 import android.os.Bundle;
 import android.util.Log;
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

 public class DriverProfile extends AppCompatActivity {
     //1)identify the firebase var
     private DatabaseReference reference;  //to get the reference
     private String driverID;           //to store the user id in
     String dName,dPhone,dEmail,dRate;


     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getSupportActionBar().hide();
         setContentView(R.layout.activity_driver_profile);


         //2)get the current user and the reference then store the current user id in the customerID
         driverID=FirebaseAuth.getInstance().getCurrentUser().getUid();
         reference= FirebaseDatabase.getInstance().getReference("drivers");

         //3)identify and initialize the text views
          TextView DriverName=(TextView) findViewById(R.id.DriverName);
          TextView DriverPhoneNumber=(TextView) findViewById(R.id.DriverPhoneNumber);
          TextView DriverEmail=(TextView) findViewById(R.id.DriverEmail);
          TextView DriverRate=(TextView) findViewById(R.id.editRate_dProfile);


         //4)start getting the customer info from the firebase using the referenceDriverPhoneNumber
         reference.child(driverID).addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if(snapshot.exists()){
                     Map<String, Object> obj = (HashMap<String, Object>) snapshot.getValue();
                     if (obj==null)return;
                     dName=obj.get("FullName").toString();
                     dPhone=obj.get("PhoneNumber").toString();
                     dEmail=obj.get("Email").toString();

                     DriverName.setText(dName);
                     DriverPhoneNumber.setText(dPhone);
                     DriverEmail.setText(dEmail);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {
                 Toast.makeText(DriverProfile.this, "something went wrong !", Toast.LENGTH_LONG).show();

             }
         });

         reference.child(driverID).child("rating").addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if(snapshot.exists()){
                     Map<String, Object> obj = (HashMap<String, Object>) snapshot.getValue();
                     if (obj==null)return;
                     dRate=obj.get("rating").toString();
                     float rate=Float.parseFloat(dRate);
                     String format=String.format("%.2f",rate);
                     DriverRate.setText(format);

                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });

     }
 }