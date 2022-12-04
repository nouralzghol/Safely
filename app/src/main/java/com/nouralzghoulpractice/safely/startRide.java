package com.nouralzghoulpractice.safely;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.nouralzghoulpractice.safely.databinding.ActivityStartRideBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class startRide extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityStartRideBinding binding;
    String customerID, customerName, customerPhone, destinationName;
    TextView destName, editCustomerName_startRide, editPhoneNumber_startRide;
    LocationListener locationListener;
    LocationManager locationManager;
    LatLng driverLatLng;
    Location lastLocation;
    Button endTrip;
    LatLng customerDestLatLng;
    Intent i1;
    private boolean assign=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStartRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // a location manager that's gonna get the user location
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            // this function tells us when there's a change in the location
            @Override
            public void onLocationChanged(@NonNull Location location) {
                //this method gives the user's location every few seconds
                Log.i("Location0: ", location.toString());

                String UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("locationInfo");
                GeoFire geoFireDriverLocation = new GeoFire(driverLocation);
                geoFireDriverLocation.setLocation(UserID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                mMap.clear();
                if (customerDestLatLng != null)
                    mMap.addMarker(new MarkerOptions().position(customerDestLatLng).title("customer destination location"));
                driverLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                int height = 70;
                int width = 70;
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.carpink);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your current location.").icon(smallMarkerIcon));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 15));


            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }
        };

        // here we're checkin if the user gave us the permission to use their location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //if the user didn't ive us the permission.. we need to ask them again
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } // but if the user gave us the permission:
        else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }

        //no touching aboveeeeeeeeee*************************

        destName = findViewById(R.id.destName);
        editCustomerName_startRide = findViewById(R.id.editCustomerName_startRide);
        editPhoneNumber_startRide = findViewById(R.id.editPhoneNumber_startRide);
        getCustomerDest();


        endTrip = findViewById(R.id.endTrip);
        endTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(startRide.this, EndTrip.class));
                finish();
            }
        });


    }

    private String DID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DatabaseReference DRef = FirebaseDatabase.getInstance().getReference("AvailableDrivers").child(DID);
    private ValueEventListener driverLocationRefListener;

    private void getCustomerDest() {
        // get the customer ID
        DRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.i("in get driver location", "driver2");

                    Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                    if (info != null && assign) {
                        assign=false;
                        customerID = info.get("assignedCustomer").toString();
                        Log.i("in get driver location", "customerID in start ride1= " + customerID);
                    }

                    //getting the destination to put a marker on map
                    DatabaseReference customerDestNameRef = FirebaseDatabase.getInstance().getReference().child("customerRideID").child(customerID).child("customerID");
                    DatabaseReference customerDestRef = FirebaseDatabase.getInstance().getReference().child("customerRideID").child(customerID).child("customerID").child("destination").child(customerID).child("l");
                    DatabaseReference customerInfoRef = FirebaseDatabase.getInstance().getReference().child("customers").child(customerID);
                    Log.i("in get driver location", "customerID in start ride2= " + customerID);

                    //to add marker on customer destination
                    customerDestRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Log.i("in get customer destination", "destination");
                                //10.9 this line of code will take all the info from the snapshot and put it in a nice list that we can work with much easier
                                List<Object> map = (List<Object>) snapshot.getValue();
                                //10.10 calling the Lat and Lng
                                double locationLat = 0;
                                double locationLng = 0;
                                //10.11 making sure the lat and Lng is not null
                                if (map.get(0) != null) {
                                    locationLat = Double.parseDouble(map.get(0).toString());
                                }
                                if (map.get(1) != null) {
                                    locationLng = Double.parseDouble(map.get(1).toString());
                                }
                                //10.12 making a LatLng to put a marker
                                customerDestLatLng = new LatLng(locationLat, locationLng);

                                //10.15 putting the marker 10.16 is in driver map activity

                                mMap.addMarker(new MarkerOptions().position(customerDestLatLng).title("customer destination location"));
                            } else {
                                Log.i("customer dest", "snapshot doesn't exist");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    //to get destination name
                    customerDestNameRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                                if (info == null) return;

                                destinationName = info.get("destinationName").toString();
                                destName.setText(destinationName);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    //to set customer name and phone number
                    customerInfoRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                                if (info == null) return;

                                customerName = info.get("FullName").toString();
                                editCustomerName_startRide.setText(customerName);
                                customerPhone = info.get("PhoneNumber").toString();
                                editPhoneNumber_startRide.setText(customerPhone);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }//*************end of the existed snapshot******************


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        if (ActivityCompat.checkSelfPermission(startRide.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(startRide.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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