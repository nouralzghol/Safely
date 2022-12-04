
package com.nouralzghoulpractice.safely;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nouralzghoulpractice.safely.databinding.ActivityEndTripBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EndTrip extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityEndTripBinding binding;
    Button confirm;
    Location lastLocation;
    LocationManager locationManager;
    LatLng driverLatLng;
    TextView editDriversName_arrival, editTotal_arrival;
    String driverName, total, customerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEndTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        editDriversName_arrival = findViewById(R.id.editDriversName_arrival);
        editTotal_arrival = findViewById(R.id.editTotal_arrival);

        getDriverName();
        getTotal();

        confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("clicking confirm", "confirm button");
                removeLocationInfo();
                removeAssignedCustomer();
                removeArrivedFlag();
                removeDriver();
                startActivity(new Intent(EndTrip.this, Driver_HomePage.class));
            }
        });
    }

    private String DID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private void removeLocationInfo(){
        Log.i("1. in removeLocationInfo", "removing LocationInfo");
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference("locationInfo");
        GeoFire locationInfoD=new GeoFire(driverLocationRef);
        locationInfoD.removeLocation(DID);
    }

    private void removeAssignedCustomer() {
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("AvailableDrivers").child(DID);
        driverRef.child("assignedCustomer").removeValue();
        Log.i("2. in removeAssignedCustomer", "removing assigned customer");


    }
    private void removeArrivedFlag() {
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("AvailableDrivers").child(DID);
        driverRef.child("arrived").removeValue();
        Log.i("2. in removeAssignedCustomer", "removing arrived flag");

    }

    private void removeDriver(){
        Log.i("3. in removeDriver", "removing the whole driver");
        DatabaseReference driverRefLoc = FirebaseDatabase.getInstance().getReference("AvailableDrivers");
        GeoFire removerRef=new GeoFire(driverRefLoc);
        removerRef.removeLocation(DID);
        Log.i("3. in removeDriver", "removed the whole driver");

        //driverRefLoc.removeValue();
    }

    DatabaseReference driverNameRef = FirebaseDatabase.getInstance().getReference("drivers").child(DID);
    private void getDriverName() {
        driverNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("in getDriverName", "getting driver name");
                Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                if (info == null) return;

                driverName = info.get("FullName").toString();
                editDriversName_arrival.setText(driverName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    DatabaseReference customerIDRef = FirebaseDatabase.getInstance().getReference("AvailableDrivers").child(DID);
    DatabaseReference totalRef;
    private void getTotal() {
        customerIDRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("in customerIDRef", "getting customer ID");
                try {
                    Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                    customerID = info.get("assignedCustomer").toString();
                    Log.i("customer ID", "customer id in end trip"+customerID);

                    Log.i("inside getTotal", customerID);
                    totalRef = FirebaseDatabase.getInstance().getReference("customerRideID").child(customerID).child("customerID");
                    totalRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                            if (info == null) return;

                            total = info.get("price").toString();

                            editTotal_arrival.setText(total);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } catch (Exception e) {
                    Log.i("exception", e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        if (ActivityCompat.checkSelfPermission(EndTrip.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(EndTrip.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
            int height = 70;
            int width = 70;
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.carpink);
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
            driverLatLng = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
            mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver location").icon(smallMarkerIcon));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 15));
        } catch (Exception e) {
            Log.i("Error 1:", e.toString());
        }

    }

}