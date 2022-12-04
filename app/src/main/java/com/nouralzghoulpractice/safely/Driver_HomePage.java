package com.nouralzghoulpractice.safely;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
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
import com.nouralzghoulpractice.safely.databinding.ActivityDriverHomePageBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Driver_HomePage extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityDriverHomePageBinding binding;


    //profile button
    ImageButton dprofile;
    LocationManager locationManager;
    LocationListener locationListener;
    ImageView whiteBox;
    TextView status, rating;
    Boolean check = false;
    Switch mWorkingSwitch;
    Location lastLocation;
    LatLng driverLatLng;
    Boolean working;
    String customerID;
    SupportMapFragment mapFragment;
    TextView state;
    String dRate;
    private DatabaseReference rateReference;  //to get the reference
    private String driverID;

    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        dprofile = (ImageButton) findViewById(R.id.profile);
        dprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile();
            }
        });

        // a location manager that's gonna get the user location
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            // this function tells us when there's a change in the location
            @Override
            public void onLocationChanged(@NonNull Location location) {
                //this method gives the user's location every few seconds
                Log.i("Location0: ", location.toString());
                if(check){
                    String UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("locationInfo");
                    GeoFire geoFireDriverLocation = new GeoFire(driverLocation);
                    geoFireDriverLocation.setLocation(UserID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    mMap.clear();
                    driverLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    int height = 80;
                    int width = 80;
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.carpink);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                    BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                    mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your current location.").icon(smallMarkerIcon));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 15));
                }

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


        // this white box should be tall enough for so we can manipulate its transition
        whiteBox = findViewById(R.id.whiteBox);
        state = findViewById(R.id.state); //it will be "you're offline"
        rating = findViewById(R.id.rating);

        //2)get the current user and the reference then store the current user id in the customerID
        driverID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        rateReference= FirebaseDatabase.getInstance().getReference("drivers");
        rateReference.child(driverID).child("rating").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Map<String, Object> obj = (HashMap<String, Object>) snapshot.getValue();
                    if (obj==null)return;
                    dRate=obj.get("rating").toString();
                    float rate=Float.parseFloat(dRate);
                    String format=String.format("%.2f",rate);
                    Log.i("format", "format rate= "+format);

                    rating.setText(format);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mHandler = new Handler();

        mWorkingSwitch = (Switch) findViewById(R.id.workingSwitch);
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    check=true;
                    Log.i("switch", "going online");
                    goOnline();
                } else {
                    goOffline();
                }
            }
        });


    }//end of onCreate


    public void goOnline() {
        mWorkingSwitch.setText("working");
        state.setText("you're online");
        Log.i("goOnline", "trying to get assigned customer");
        String UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("AvailableDrivers");
        GeoFire geoFireAvailable = new GeoFire(ref);
        geoFireAvailable.setLocation(UserID, new GeoLocation(driverLatLng.latitude, driverLatLng.longitude));


        if (check)
            mRunnable.run();
        else
            Log.i("goOnline", "check is false");
    }


    String DID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference().child("AvailableDrivers").child(DID);
    AlertDialog alert;

    private Runnable mRunnable = (new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < 1; i++) {
                checkRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.i("GetAssignedCustomer", "inside the onData");
                            for (DataSnapshot child : snapshot.getChildren()) {
                                if (child.getKey().equals("assignedCustomer")) {
                                    customerID = child.getValue().toString();
                                    if (customerID != null) {
                                        check = false;
                                        Log.i("GetAssignedCustomer", "making the dialog");
                                        Log.i("GetAssignedCustomer", "customer ID " + customerID);
                                        try {
                                            alert = new AlertDialog.Builder(Driver_HomePage.this).setTitle("Ride request")
                                                    .setMessage("you have a new ride request")
                                                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Log.i("in Driver_HomePage alert", "it works fine");
                                                            Intent i = new Intent(Driver_HomePage.this, CustomerInfoFD.class);
                                                            startActivity(i);
                                                            finish();
                                                        }
                                                    }).show();
                                        } catch (Exception e) {
                                            Log.i("in Driver_HomePage alert", e.toString());
                                        }

                                    }
                                } else {
                                    Log.i("GetAssignedCustomer", "customer ID doesn't exist");
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }
    });


    public void goOffline() {
        mWorkingSwitch.setText("start working");
        state.setText("you're offline");
        check=false;
        String dID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("AvailableDrivers");
        GeoFire geoFireAvailable = new GeoFire(refAvailable);
        geoFireAvailable.removeLocation(dID);
        DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("locationInfo");
        GeoFire geoFireDriverLocation = new GeoFire(driverLocation);
        geoFireDriverLocation.removeLocation(dID);

    }


    private void goToProfile() {
        startActivity(new Intent(Driver_HomePage.this, DriverProfile.class));
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //this line of code generates the blue dot (live location) on map.
        // mMap.setMyLocationEnabled(true);
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        //  List<Address> addressList = null;
        try {
            //  addressList = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
            // driverLatLng = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
            driverLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

            int height = 80;
            int width = 80;
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.carpink);
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
            mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your current location.").icon(smallMarkerIcon));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 15));
        } catch (Exception e) {
            Log.i("Error 1:", e.toString());
        }

    }

}