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
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.nouralzghoulpractice.safely.databinding.ActivityCustomerInfoFdBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerInfoFD extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityCustomerInfoFdBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;
    LatLng driverLatLng;
    Button pickUpButton;
    private boolean assign=true;
    public String customerID;
    Location lastLocation;

    //customer info
    TextView editCustomerName_info;
    TextView editCustomerPhoneNumber_info;
    TextView editNumberOfKids_info;
    TextView editPickUpLocation_info;
    TextView editDestination_info;

    String Cname, Cphone, NumOfKids, pickupName, destinationName;
    Intent i1, i2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerInfoFdBinding.inflate(getLayoutInflater());
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
                    DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("locationInfo");
                    GeoFire geoFireDriverLocation = new GeoFire(driverLocation);
                    geoFireDriverLocation.setLocation(UserID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    mMap.clear();
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



        editCustomerName_info = findViewById(R.id.editCustomerName_info);
        editCustomerPhoneNumber_info = findViewById(R.id.editCustomerPhoneNumber_info);
        editNumberOfKids_info = findViewById(R.id.editNumberOfKids_info);
        editPickUpLocation_info = findViewById(R.id.editPickUpLocation_info);
        editDestination_info = findViewById(R.id.editDestination_info);


        pickUpButton = (Button) findViewById(R.id.pickUpButton);
        pickUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i2 = new Intent(CustomerInfoFD.this, AfterAcceptanceDriver.class);
                startActivity(i2);
                finish();
            }
        });

        getDriverLocation();
        //getCustomerID();

    }//end onCreate


    //bring the driver location
    private String DID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DatabaseReference DRef = FirebaseDatabase.getInstance().getReference("AvailableDrivers").child(DID);
    private ValueEventListener driverLocationRefListener;

    private void getDriverLocation() {

        // get the customer ID
        DRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.i("in get driver location", "driver2");

                    Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                    if (info == null) {
                        Log.i("getting customerID", "in getDriverLocation");
                        return;
                    } else if(assign){
                        assign=false;
                        customerID = info.get("assignedCustomer").toString();
                        Log.i("in get driver location", "customerID= " + customerID);

                        // get the Customer Info
                        DatabaseReference assignedCustomerInfo = FirebaseDatabase.getInstance().getReference("customers").child(customerID);
                        assignedCustomerInfo.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Log.i("in get driver location", "driver3");

                                    Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                                    if (info == null) return;
                                    Log.i("in getCustomerInfo", "getCustomerInfo()");

                                    Cname = info.get("FullName").toString();
                                    Cphone = info.get("PhoneNumber").toString();

                                    editCustomerName_info.setText(Cname);
                                    editCustomerPhoneNumber_info.setText(Cphone);
                                    Log.i("in getCustomerInfo", "FullName=" + Cname);


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });//***************************************

                        //get kids infooo

                        DatabaseReference assignedKidsInfo = FirebaseDatabase.getInstance().getReference("customerRideID").child(customerID).child("customerID");
                        assignedKidsInfo.child("kidsInfo").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                                    if (info == null) return;

                                    NumOfKids = info.get("numOfKids").toString();
                                    editNumberOfKids_info.setText(NumOfKids);


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        assignedKidsInfo.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                                    if (info == null) return;
                                    pickupName = info.get("pickupName").toString();
                                    destinationName = info.get("destinationName").toString();


                                    editPickUpLocation_info.setText(pickupName);
                                    editDestination_info.setText(destinationName);


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }
            }//*******************************


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        //10.5 we make a reference for the driver location, the l in .child("l") is an array that stores the LAtLng of the location
//        //10.6 adding a value listener
//        //13.7 we assigned the driverLocationRefListener the valueEvenListener (we assigned the value that we have here to the driverLocationRefListener that we've just create)
//        driverLocationRefListener = DRef.child("l").addValueEventListener(new ValueEventListener() {
//            @Override
//            //10.7 each time the location changes onDataChange is called
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                //10.8 it is important to make sure dataSnapshot exists because otherwise, the app will crash
//                if (dataSnapshot.exists()) {
//                    Log.i("in get driver location", "driver1");
//                    //10.9 this line of code will take all the info from the snapshot and put it in a nice list that we can work with much easier
//                    List<Object> map = (List<Object>) dataSnapshot.getValue();
//                    //10.10 calling the Lat and Lng
//                    double locationLat = 0;
//                    double locationLng = 0;
//                    //10.11 making sure the lat and Lng is not null
//                    if (map.get(0) != null) {
//                        locationLat = Double.parseDouble(map.get(0).toString());
//                    }
//                    if (map.get(1) != null) {
//                        locationLng = Double.parseDouble(map.get(1).toString());
//                    }
//                    //10.12 making a LatLng to put a marker
//                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
//
//
//                    //10.15 putting the marker 10.16 is in driver map activity
//                    int height = 70;
//                    int width = 70;
//                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.carpink);
//                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
//                    BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
//                    mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver location").icon(smallMarkerIcon));
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 15));
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });//**************************************



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