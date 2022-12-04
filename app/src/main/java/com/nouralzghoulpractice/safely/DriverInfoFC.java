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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.nouralzghoulpractice.safely.databinding.ActivityDriverInfoFcBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverInfoFC extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityDriverInfoFcBinding binding;

    int radius = 1;
    boolean driverFound = false, checkStatus = true, checkStatus2 = true;
    LocationManager locationManager;
    LocationListener locationListener;

    private String CID, driverID;

    LatLng pickupLocation, driverLocation;
    GeoQuery geoQuery;

    private ProgressBar progressBar;
    private boolean assign=true;
    //driver info var
    TextView editDriverName_info;
    TextView editDriverAge_info;
    TextView editDriverPhoneNumber_info;
    TextView carName_dInfo;
    TextView carColor_dInfo;
    TextView carNumber_dInfo;
    TextView editRate_dProfile;
    String Dname, Dage, Dphone,Drate, Cname, Ccolor, Cnumber;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverInfoFcBinding.inflate(getLayoutInflater());
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
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //implement
        editDriverName_info = (TextView) findViewById(R.id.editDriverName_info);
        editDriverAge_info = (TextView) findViewById(R.id.editDriverAge_info);
        editDriverPhoneNumber_info = (TextView) findViewById(R.id.editDriverPhoneNumber_info);
        carName_dInfo = (TextView) findViewById(R.id.carName_dInfo);
        carColor_dInfo = (TextView) findViewById(R.id.carColor_dInfo);
        carNumber_dInfo = (TextView) findViewById(R.id.carNumber_dInfo);
        editRate_dProfile=(TextView) findViewById(R.id.editRate_dProfile);

        getCustomerPickUpLoc();

        mHandler = new Handler();

    }//end onCreate


    public void getCustomerPickUpLoc() {
        Log.i("getCustomerPickUpLoc", "welcome :) ");
        CID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerPickUpLocRef = FirebaseDatabase.getInstance().getReference().child("customerRideID").child(CID).child("customerID").child("pickup").child(CID).child("l");
        customerPickUpLocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("getCustomerPickUpLoc", "in onDataChange");
                if (snapshot.exists()) {
                    Log.i("getCustomerPickUpLoc", "Snapshot exists");
                    List<Object> map = (List<Object>) snapshot.getValue();
                    double lat = 0, lng = 0;
                    if (map.get(0) != null)
                        lat = Double.parseDouble(map.get(0).toString());
                    if (map.get(1) != null)
                        lng = Double.parseDouble(map.get(1).toString());

                    pickupLocation = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions().position(pickupLocation).title("customer pickup loc"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 15));
                    Log.i("getCustomerPickUpLoc", "calling getClosests inside getCustPickUpLoc");
                    getClosestDriver();


                } else
                    Log.i("getCustomerPickUpLoc", "Snapshot doesn't exist");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void AssignCustomerIDfd(String DID) {
        String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        driverID = DID;
        String nearestDID = DID;
        DatabaseReference assignRef = FirebaseDatabase.getInstance().getReference().child("AvailableDrivers").child(nearestDID);
        Map assignCID = new HashMap();
        assignCID.put("assignedCustomer", customerID);
        assignRef.updateChildren(assignCID);
    }


    private void getClosestDriver() {
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("locationInfo");
        GeoFire geoFireDriver = new GeoFire(driverLocationRef);
        geoQuery = geoFireDriver.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        Log.i("getClosestDriver", "radius = " + radius);
        geoQuery.removeAllListeners();
        Log.i("getClosestDriver", "removed all listeners");
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                DatabaseReference driversInfoRef = FirebaseDatabase.getInstance().getReference().child("drivers").child(key);
                driversInfoRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {

                            if (assign) {
                                AssignCustomerIDfd(key);
                                assign = false;
                                Log.i("AssignCustomerIDfd", "onDataChange I've been called");
                            }
                            driverID = key;
                            Map<String, Object> driverMapInfo = (Map<String, Object>) snapshot.getValue();
                            Dname = driverMapInfo.get("FullName").toString();
                            Dage = driverMapInfo.get("Age").toString();
                            Dphone = driverMapInfo.get("PhoneNumber").toString();

                            Log.i("getClosestDriver", "FullName" + Dname);
                            editDriverName_info.setText(Dname);
                            editDriverAge_info.setText(Dage);
                            editDriverPhoneNumber_info.setText(Dphone);
                            driverFound = true;

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i("onCancelled", "sth went wrong on the driver info");
                    }
                });


                driversInfoRef.child("rating").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Map<String, Object> driverinfo = (Map<String, Object>) snapshot.getValue();
                           Drate= driverinfo.get("rating").toString();
                            float rate=Float.parseFloat(Drate);
                            String format=String.format("%.2f",rate);
                            editRate_dProfile.setText(format);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                driversInfoRef.child("carInfo").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            Map<String, Object> carInfo = (Map<String, Object>) snapshot.getValue();
                            Cname = carInfo.get("CarName").toString();
                            Ccolor = carInfo.get("CarColor").toString();
                            Cnumber = carInfo.get("CarNumber").toString();
                            Log.i("getClosestDriver", "CarName" + Cname);
                            carName_dInfo.setText(Cname);
                            carColor_dInfo.setText(Ccolor);
                            carNumber_dInfo.setText(Cnumber);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i("onCancelled", "sth went wrong on the car info");
                    }
                });

                //this is for driver location
                DatabaseReference driverLocRef = FirebaseDatabase.getInstance().getReference().child("locationInfo").child(key).child("l");
                driverLocRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i("getDriverLocation", "in onDataChange");
                        if (snapshot.exists()) {
                            Log.i("getDriverLocation", "Snapshot exists");
                            List<Object> map = (List<Object>) snapshot.getValue();
                            double lat = 0, lng = 0;
                            if (map.get(0) != null)
                                lat = Double.parseDouble(map.get(0).toString());
                            if (map.get(1) != null)
                                lng = Double.parseDouble(map.get(1).toString());

                            driverLocation = new LatLng(lat, lng);

                            Location Dloc = new Location("");
                            Dloc.setLatitude(driverLocation.latitude);
                            Dloc.setLongitude(driverLocation.longitude);
                            Location Cloc = new Location("");
                            Cloc.setLatitude(pickupLocation.latitude);
                            Cloc.setLongitude(pickupLocation.longitude);

                            float distance = Dloc.distanceTo(Cloc);

                            if (distance < 20) {
                                Toast.makeText(DriverInfoFC.this, "Your Driver Arrived successfully!", Toast.LENGTH_LONG).show();

                            }

                            int height = 70;
                            int width = 70;
                            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.carpink);
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                            BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(pickupLocation).title("customer pickup loc"));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 15));
                            mMap.addMarker(new MarkerOptions().position(driverLocation).title("Your Driver location.").icon(smallMarkerIcon));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverLocation, 15));
                            progressBar.setVisibility(View.INVISIBLE);

                            return;
                        } else
                            Log.i("getDriverLocation", "Snapshot doesn't exist");


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @Override
            public void onKeyExited(String key) {
                Log.i("onKeyExited", "this was called");

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.i("onKeyMoved", "this was called");

            }

            @Override
            public void onGeoQueryReady() {
                Log.i("onGeoQueryReady", "this was called");
                Log.i("onGeoQueryReady", "radius = " + radius);
                if (driverFound == false) {
                    radius++;
                    getClosestDriver();
                } else {
                    Log.i("onGeoQueryReady", "driver found");
                    Log.i("onGeoQueryReady", "driver ID = " + driverID);
                    checkRideStatus(driverID);
                    return;
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.i("onGeoQueryError", "God forbid Error in query");
            }

        });

    }

    DatabaseReference checkStatusRef;

    public void checkRideStatus(String driverID) {
        Log.i("getDriverID", driverID);
        checkStatusRef = FirebaseDatabase.getInstance().getReference().child("AvailableDrivers").child(driverID).child("arrived");
        if (checkStatus)
            mRunnable.run();
        else
            Log.i("in checkRideStatus", "the ride is still running");
    }


    AlertDialog alert, alert2;
    private Runnable mRunnable = (new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < 1; i++) {
                checkStatusRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            checkStatus = false;
                            Log.i("mRunnable", "inside the onData");
                            alert = new AlertDialog.Builder(DriverInfoFC.this).setTitle("Driver Arrived")
                                    .setMessage("your driver has arrived")
                                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Log.i("mRunnable inside onClick", "going to press accept");
                                            checkRideStatus2(driverID);
                                        }
                                    }).show();
                        } else {
                            Log.i("mRunnable in runnable", "the ride is still going");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }
    });

    DatabaseReference checkStatusRef2;
    private void checkRideStatus2(String driverID) {
        checkStatusRef2 = FirebaseDatabase.getInstance().getReference().child("AvailableDrivers").child(driverID).child("assignedCustomer");
        if (checkStatus2)
            mRunnable2.run();
        else
            Log.i("in checkRideStatus", "the ride is still running");

    }

    private Runnable mRunnable2 = (new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < 1; i++) {
                checkStatusRef2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            checkStatus = false;
                            Log.i("mRunnable2", "inside the onData");
                            try {
                                alert2 = new AlertDialog.Builder(DriverInfoFC.this).setTitle("Ride Ended")
                                        .setMessage("the ride has ended")
                                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    Intent i = new Intent(DriverInfoFC.this, Rating.class);
                                                    if (Dname != null && driverID != null) {
                                                        Log.i("4. trying to pass Dname and driverID mRunnable", "driverID = " + driverID);
                                                        i.putExtra("PhoneNum", Dphone);
                                                        i.putExtra("Dname", Dname);
                                                        i.putExtra("DriverID", driverID);
                                                    } else {
                                                        Log.i("4. trying to pass Dname and driverID mRunnable", "they are null :) ");
                                                    }
                                                    startActivity(i);
                                                    finish();
                                                } catch (Exception e) {
                                                    Log.i("mRunnable2", "trying to move to rating page");
                                                    Log.i("mRunnable Exception2", e.toString());
                                                }
                                            }
                                        }).show();
                            } catch (Exception e) {
                                Log.i("in alert2:", e.toString());
                            }
                        } else {
                            Log.i("mRunnable2 in runnable", "the ride is still going");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }
    });

//    DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference().child("AvailableDrivers").child(driverID2);
//    private Runnable mRunnable = (new Runnable() {
//        @Override
//        public void run() {
//            for (int i = 0; i < 1; i++) {
//                checkRef.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()) {
//                            Log.i("mRunnable", "inside the onData");
//                            for (DataSnapshot child : snapshot.getChildren()) {
//                                if (child.getKey().equals("assignedCustomer")) {
//                                    customerID = child.getValue().toString();
//                                    if (customerID != null) {
//                                        check = false;
//                                        Log.i("GetAssignedCustomer", "making the dialog");
//                                        Log.i("GetAssignedCustomer", "customer ID " + customerID);
//                                        alert = new AlertDialog.Builder(Driver_HomePage.this).setTitle("Ride request")
//                                                .setMessage("you have a new ride request")
//                                                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        Intent i = new Intent(Driver_HomePage.this, CustomerInfoFD.class);
//                                                        startActivity(i);
//                                                        finish();
//                                                    }
//                                                }).show();
//
//                                    }
//                                } else {
//                                    Log.i("GetAssignedCustomer", "customer ID doesn't exist");
//                                }
//                            }
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//
//            }
//        }
//    });

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

}


// a working version of getCustomerPickUpLocation method
//    public void getCustomerPickUpLoc() {
//        Log.i("getCustomerPickUpLoc", "welcome :) ");
//        CID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference customerPickUpLocRef = FirebaseDatabase.getInstance().getReference().child("customerRideID").child(CID).child("customerID").child("pickup").child(CID).child("l");
//        customerPickUpLocRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.i("getCustomerPickUpLoc", "in onDataChange");
//                if(snapshot.exists()){
//                    Log.i("getCustomerPickUpLoc","Snapshot exists");
//                    List <Object> map = (List <Object>) snapshot.getValue();
//                    double lat = 0, lng = 0;
//                    if (map.get(0) != null)
//                        lat = Double.parseDouble(map.get(0).toString());
//                    if (map.get(1) != null)
//                        lng = Double.parseDouble(map.get(1).toString());
//
//                    pickupLocation = new LatLng(lat, lng);
//                    mMap.addMarker(new MarkerOptions().position(pickupLocation).title("customer pickup loc"));
//                }
//                else
//                    Log.i("getCustomerPickUpLoc", "Snapshot doesn't exist");
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }