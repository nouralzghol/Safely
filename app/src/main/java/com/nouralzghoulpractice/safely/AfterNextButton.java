package com.nouralzghoulpractice.safely;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nouralzghoulpractice.safely.databinding.ActivityAfterNextButtonBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfterNextButton extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityAfterNextButtonBinding binding;

    //new elements
    Button Confirm, Calculate;
    RelativeLayout SecondRelativeLayout;
    EditText firstChildName, secondChildName;
    Spinner child1Gender, child1Age, child2Gender, child2Age, babySittingHours;
    CheckBox babysitting;
    private FirebaseAuth mAuth;
    String cId;
    double ridePrice;
    int kidsFee;
    String distance;
    DatabaseReference kidsRef;
    TextView editPrice;
    int numOfKids=1;
    ImageButton backArrow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAfterNextButtonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        firstChildName = findViewById(R.id.firstChildName);
        secondChildName = findViewById(R.id.secondChildName);
        child1Gender = findViewById(R.id.child1Gender);
        child1Age = findViewById(R.id.child1Age);
        child2Gender = findViewById(R.id.child2Gender);
        child2Age = findViewById(R.id.child2Age);
        babySittingHours = findViewById(R.id.babySittingHours);
        babysitting = findViewById(R.id.babysitting);
        editPrice = findViewById(R.id.editPrice);

        SecondRelativeLayout = findViewById(R.id.secondRelativeLayout);

        mAuth = FirebaseAuth.getInstance();
        cId = mAuth.getCurrentUser().getUid();

        kidsRef = FirebaseDatabase.getInstance().getReference("customerRideID").child(cId).child("customerID").child("kidsInfo");
        Confirm = (Button) findViewById(R.id.confirmButton);
        Calculate = (Button) findViewById(R.id.calculateButton);
        Calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference distanceRef = FirebaseDatabase.getInstance().getReference().child("customerRideID").child(cId).child("customerID");
                distanceRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.i("inside calculatePrice", "trying to get the price");
                            for (DataSnapshot child : snapshot.getChildren()) {
                                if (child.getKey().equals("distance")) {
                                    if (babysitting.isChecked()) {
                                        int hours = Integer.parseInt(babySittingHours.getSelectedItem().toString());
                                        kidsFee = hours * 3;
                                    } else
                                        kidsFee = 0;
                                    Log.i("KidsFee", String.valueOf(kidsFee));
                                    distance = child.getValue().toString();
                                    ridePrice = 0.35 + Double.parseDouble(distance) * 0.5 + kidsFee;
                                    String ridePriceString = String.format("%.2f", ridePrice);
                                    DatabaseReference PriceRef = FirebaseDatabase.getInstance().getReference("customerRideID").child(cId).child("customerID").child("price");
                                    PriceRef.setValue(ridePriceString);
                                    editPrice.setText(ridePriceString + " JD");
                                    Log.i("calculatePrice","leaving the method");
                                    return;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });
        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kidsInfo();
            }
        });

    }//end onCreate


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    private void kidsInfo() {

        String firstChildNameValue = firstChildName.getText().toString().trim();
        String genderValue1 = child1Gender.getSelectedItem().toString().trim();
        String ageValue1 = child1Age.getSelectedItem().toString().trim();

        String secondChildNameValue = secondChildName.getText().toString().trim();
        String genderValue2 = child2Gender.getSelectedItem().toString().trim();
        String ageValue2 = child2Age.getSelectedItem().toString().trim();

        String hoursValue = babySittingHours.getSelectedItem().toString().trim();
        String babysittingValue = "";

        if (babysitting.isChecked())
            babysittingValue = "true";
        else
            babysittingValue = "false";
        if (firstChildNameValue.isEmpty()) {
            firstChildName.setError("First child name is required.");
            firstChildName.requestFocus();
            return;
        }

        if (genderValue1.isEmpty()) {
            ((TextView) child1Gender.getSelectedView()).setError("Gender is required.");
            child1Gender.requestFocus();
            return;
        }

        if (ageValue1.isEmpty()) {
            ((TextView) child1Age.getSelectedView()).setError("Age is required.");
            child1Age.requestFocus();
            return;
        }


        Map kids = new HashMap();
        kids.put("firstChildName", firstChildNameValue);
        kids.put("child1Gender", genderValue1);
        kids.put("child1Age", ageValue1);

        if (!secondChildNameValue.isEmpty()) {
            numOfKids++;
            kids.put("secondChildName", secondChildNameValue);
            kids.put("child2Gender", genderValue2);
            kids.put("child2Age", ageValue2);

        }

        if (babysittingValue.equals("true")) {
            kids.put("babysitting", babysittingValue);
            if (hoursValue.isEmpty())
                ((TextView) babySittingHours.getSelectedView()).setError("Number of hours is required.");
            else
                kids.put("babySittingHours", hoursValue);
        }

        kids.put("numOfKids", numOfKids);
        kidsRef.updateChildren(kids);

        startActivity(new Intent(AfterNextButton.this, DriverInfoFC.class));


    }


}