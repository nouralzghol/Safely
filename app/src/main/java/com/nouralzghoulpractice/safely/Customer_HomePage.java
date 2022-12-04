package com.nouralzghoulpractice.safely;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.GnssAntennaInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.nouralzghoulpractice.safely.databinding.ActivityCustomerHomePageBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Customer_HomePage extends FragmentActivity implements  OnMapReadyCallback, AdapterView.OnItemSelectedListener  {

    private GoogleMap mMap;
    private ActivityCustomerHomePageBinding binding;

    //profile button
    ImageButton cprofile;
    Button Next;
    SearchView PickUpLoc, DestLoc;
    LocationManager locationManager;
    LocationListener locationListener;
    public String location1, location2;
    public LatLng latLng1, latLng2, userLatLng;
    public Location lastLocation;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        cprofile=(ImageButton) findViewById(R.id.cprofile);
        cprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTocProfile();
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

        // pick up and dest search view
        PickUpLoc = findViewById(R.id.pickUpLoc);
        DestLoc = findViewById(R.id.destLoc);

        //set the pickup searchview to be able to search for locations
        PickUpLoc.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                location1 = PickUpLoc.getQuery().toString();
                List<android.location.Address> addressList = null;
                if (location1 != null && !location1.equals("")) {
                    Log.i("in loc1 ","loc1= "+location1);

                    Geocoder geocoder = new Geocoder(Customer_HomePage.this);
                    try {
                        addressList = geocoder.getFromLocationName(location1, 1);
                    } catch (Exception e) {
                        Log.i("Error in PickUpLoc ", e.toString());
                    }
                    android.location.Address address = addressList.get(0);
                    latLng1 = new LatLng(address.getLatitude(), address.getLongitude());
                    Log.i("latLng1", latLng1.toString());
                    mMap.addMarker(new MarkerOptions().position(latLng1));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 15));

                    //this will give us the ID of the user that is currently logged in
                    String UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRideID").child(UserID).child("customerID").child("pickup");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(UserID, new GeoLocation(address.getLatitude(), address.getLongitude()));
                    DatabaseReference pickupNameRef = FirebaseDatabase.getInstance().getReference("customerRideID").child(UserID).child("customerID").child("pickupName");
                    pickupNameRef.setValue(location1);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //set the destination searchview to be able to search for locations
        DestLoc.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                location2 = DestLoc.getQuery().toString();
                List<android.location.Address> addressList = null;
                if (location2 != null && !location2.equals("")) {
                    Log.i("in loc2 ","loc2= "+location2);
                    Geocoder geocoder = new Geocoder(Customer_HomePage.this);
                    try {
                        addressList = geocoder.getFromLocationName(location2, 1);
                    } catch (Exception e) {
                        Log.i("Error in DestLoc ", e.toString());
                    }
                    Address address = addressList.get(0);
                    latLng2 = new LatLng(address.getLatitude(), address.getLongitude());
                    Log.i("latLng2", latLng2.toString());
                    mMap.addMarker(new MarkerOptions().position(latLng2).title(location2).draggable(true));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng2, 15));

                    //this will give us the ID of the user that is currently logged in
                    String UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRideID").child(UserID).child("customerID").child("destination");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(UserID, new GeoLocation(address.getLatitude(), address.getLongitude()));
                    DatabaseReference destinationNameRef = FirebaseDatabase.getInstance().getReference("customerRideID").child(UserID).child("customerID").child("destinationName");
                    destinationNameRef.setValue(location2);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });






        //Next button
        Next = findViewById(R.id.next);
        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double d= SphericalUtil.computeDistanceBetween(latLng1, latLng2);
                Log.i("distance","distance"+ d/1000);//check if you calculate price based on km or m
                String CID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference Cref = FirebaseDatabase.getInstance().getReference("customerRideID").child(CID).child("customerID").child("distance");
                Cref.setValue(d/1000);

                Intent intent=new Intent(Customer_HomePage.this,AfterNextButton.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void goTocProfile() {
        startActivity(new Intent(Customer_HomePage.this,CustomerProfile.class));

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //this line of code generates the blue dot (live location) on map.
        mMap.setMyLocationEnabled(true);
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        List <Address> addressList = null;
        try {
            Log.i("onMapReady", "trying to get the customer live location");
            addressList = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
            userLatLng = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
            //this line generates a red marker in the user location
            //mMap.addMarker(new MarkerOptions().position(userLatLng));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
        }
        catch (Exception e) {
            Log.i("Error 1:", e.toString());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}