package com.nouralzghoulpractice.safely;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class Rating extends AppCompatActivity {
    TextView driverName, priceTextView;
    RatingBar ratingBar;
    Intent i;
    String Dname, DID;
    Button okay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("rating activity", "entered rating activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        driverName = (TextView) findViewById(R.id.editDriverName_info);
        priceTextView = (TextView) findViewById(R.id.editPrice_rating);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        okay = findViewById(R.id.button2);

        i = getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            Dname = (String) b.get("Dname");
            DID = (String) b.get("DriverID");
            driverName.setText(Dname);
            rate(DID);
            Log.i("in bundle", "they are not nuuuuulllllllll");
        } else {
            Log.i("in bundle", "they are nuuuuulllllllll");
        }
        getRidePrice();
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeRideInfo();
                Intent i = new Intent(Rating.this, Customer_HomePage.class);
                startActivity(i);
                finish();
            }
        });

    }


    private void rate(String DID) {
        DatabaseReference rateRef = FirebaseDatabase.getInstance().getReference("drivers").child(DID).child("rating");
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()){
                            Log.i("onDataChange","testing 1");
                            rateRef.child("rating").setValue(rating);
                        } else {
                            Log.i("onDataChange","testing 2");
                            Map<String, Object> rateInfo = (Map<String, Object>) snapshot.getValue();
                            String oldRateString = rateInfo.get("rating").toString();
                            float oldRate = Float.parseFloat(oldRateString);
                            Log.i("onDataChange","oldRate");
                            float newRate = (oldRate+rating)/2;
                            rateRef.child("rating").setValue(newRate);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Log.i("calling saveRating", "entered saveRating");

            }
        });
    }

    String CID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference priceRef = FirebaseDatabase.getInstance().getReference("customerRideID").child(CID).child("customerID");
    private void getRidePrice() {
        Log.i("calling getRidePrice", "entered getRidePrice");
        priceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Log.i("calling getRidePrice", "inside onDataChange");
                    Map<String, Object> priceInfo = (Map<String, Object>) snapshot.getValue();
                    String price = priceInfo.get("price").toString();

                    if (!price.equals("")) {
                        Log.i("calling getRidePrice", "price = "+ price );
                        priceTextView.setText(price);
                    }
                }
                else
                    Log.i("msg","I don't know what to do.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    DatabaseReference RideInfoRef = FirebaseDatabase.getInstance().getReference("customerRideID").child(CID);
    private void removeRideInfo() {
        RideInfoRef.removeValue();
    }
}