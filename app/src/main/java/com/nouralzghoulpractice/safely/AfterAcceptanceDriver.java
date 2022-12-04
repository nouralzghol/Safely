package com.nouralzghoulpractice.safely;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.maps.android.SphericalUtil;
import com.nouralzghoulpractice.safely.databinding.ActivityAfterAcceptanceDrrBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfterAcceptanceDriver extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityAfterAcceptanceDrrBinding binding;
    public String customerID;

    private String cusName, cusPhone, kid1Name, kid1Age, kid2Name, kid2Age, pickup, destination;
    //define var
    TextView editCustomerName_AfterAcc;
    TextView editPhoneNumber_AfterAcc;
    TextView editKidOneName_AfterAcc;
    TextView editKidOneAge_AfterAcc;
    TextView editKidTwoName_AfterAcc;
    TextView editKidTwoAge_AfterAcc;
    TextView editPickUpLocation_AfterAcc;
    TextView editDestination_AfterAcc;
    TextView leftBracket2, YearsOld2_AfterAcc;
    Button arrived;
    AlertDialog cAlert;
    LatLng customerPickupLocation;
    String CID;
    LocationManager locationManager;
    LocationListener locationListener;
    LatLng driverLatLng;
    LatLng customerPickupLatLng;
    boolean isArrived = false;
   private boolean assign=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAfterAcceptanceDrrBinding.inflate(getLayoutInflater());
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
                mMap.addMarker(new MarkerOptions().position(customerPickupLatLng).title("customer PickUp location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerPickupLatLng, 15));

                driverLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                int height = 70;
                int width = 70;
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.carpink);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your current location.").icon(smallMarkerIcon));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 15));

                double d = SphericalUtil.computeDistanceBetween(customerPickupLatLng, driverLatLng);
                if (d < 500) {
                    Log.i("d=",Double.toString(d));
                    isArrived = true;
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


        //initialize var
        editCustomerName_AfterAcc = findViewById(R.id.editCustomerName_AfterAcc);
        editPhoneNumber_AfterAcc = findViewById(R.id.editPhoneNumber_AfterAcc);
        editKidOneName_AfterAcc = findViewById(R.id.editKidOneName_AfterAcc);
        editKidOneAge_AfterAcc = findViewById(R.id.editKidOneAge_AfterAcc);
        editKidTwoName_AfterAcc = findViewById(R.id.editKidTwoName_AfterAcc);
        editKidTwoAge_AfterAcc = findViewById(R.id.editKidTwoAge_AfterAcc);
        editPickUpLocation_AfterAcc = findViewById(R.id.editPickUpLocation_AfterAcc);
        editDestination_AfterAcc = findViewById(R.id.editDestination_AfterAcc);
        leftBracket2 = findViewById(R.id.leftBracket2);
        YearsOld2_AfterAcc = findViewById(R.id.YearsOld2_AfterAcc);

        arrived = findViewById(R.id.ButtonArrived);
        arrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (isArrived) {
                driverArrived();
                Intent i = new Intent(AfterAcceptanceDriver.this, startRide.class);
                startActivity(i);
                finish();
//                } else {
//                    Toast.makeText(AfterAcceptanceDriver.this, "you are not close to the pickup location!", Toast.LENGTH_LONG).show();
//
//                }

            }
        });
        getDriverLocation();

    }//end onCreate*****************************

    private String DID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DatabaseReference DRef = FirebaseDatabase.getInstance().getReference("AvailableDrivers").child(DID);
    //private ValueEventListener driverLocationRefListener;

    private void driverArrived() {
        DatabaseReference arrivedRef = DRef.child("arrived");
        arrivedRef.setValue("true");
    }
    private void getDriverLocation() {
        // get the customer ID
        DRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.i("in get driver location", "driver2");

                    Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                    if (info == null) return;
                    else if (assign) {
                        assign = false;
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
                                    cusName = info.get("FullName").toString();
                                    cusPhone = info.get("PhoneNumber").toString();
                                    editCustomerName_AfterAcc.setText(cusName);
                                    editPhoneNumber_AfterAcc.setText(cusPhone);
                                    Log.i("in getCustomerInfo", "FullName=" + cusName);


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });//end of get customer info***************************************

                        //get kids infooo

                        DatabaseReference assignedKidsInfo = FirebaseDatabase.getInstance().getReference("customerRideID").child(customerID).child("customerID");
                        assignedKidsInfo.child("kidsInfo").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Map<String, Object> info = (HashMap<String, Object>) snapshot.getValue();
                                    if (info == null) return;
                                    kid1Name = info.get("firstChildName").toString();
                                    kid1Age = info.get("child1Age").toString();
                                    editKidOneName_AfterAcc.setText(kid1Name);
                                    editKidOneAge_AfterAcc.setText(kid1Age);
                                    if (info.containsKey("secondChildName")) {
                                        kid2Name = info.get("secondChildName").toString();
                                        kid2Age = info.get("child2Age").toString();
                                        editKidTwoName_AfterAcc.setText(kid2Name);
                                        editKidTwoAge_AfterAcc.setText(kid2Age);
                                    } else {
                                        editKidTwoName_AfterAcc.animate().alpha(0);
                                        leftBracket2.animate().alpha(0);
                                        editKidTwoAge_AfterAcc.animate().alpha(0);
                                        YearsOld2_AfterAcc.animate().alpha(0);
                                    }
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
                                    pickup = info.get("pickupName").toString();
                                    destination = info.get("destinationName").toString();
                                    editPickUpLocation_AfterAcc.setText(pickup);
                                    editDestination_AfterAcc.setText(destination);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //get customer pickup location

                        DatabaseReference customerPickUpLocRef = FirebaseDatabase.getInstance().getReference().child("customerRideID").child(customerID).child("customerID").child("pickup").child(customerID).child("l");
                        customerPickUpLocRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Log.i("in get customer pickup location", "pickup location");
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
                                    customerPickupLatLng = new LatLng(locationLat, locationLng);

                                    //10.15 putting the marker 10.16 is in driver map activity

                                    mMap.addMarker(new MarkerOptions().position(customerPickupLatLng).title("customer PickUp location"));
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerPickupLatLng, 15));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }
                }//*************end of the existed snapshot******************
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//
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
//        });//end of get driver location**************************************
//


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }
}