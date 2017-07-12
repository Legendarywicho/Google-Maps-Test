package io.luis_santiago.googlemaps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    // UI buttons
    private Button normal;
    private Button satelite;
    private Button location;
    // data structure for location
    private MarkerOptions mylocationMarker;
    private LatLng finalLocation;
    private Location myLocation;
    // this is for the location of the user
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mgoogleMap;
    private Boolean mapReady = false;
    private GoogleApiClient mGoogleApiClient;
    private boolean weHavePermission = false;
    private LocationRequest updateLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        /*
         * Setting the button's click Listener
         * */
        satelite.setOnClickListener(this);
        location.setOnClickListener(this);
        normal.setOnClickListener(this);

        // This is for setting up the google maps API
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        mGoogleApiClient.connect();
        createLocationRequest();

    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            weHavePermission = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }

        if (weHavePermission) {
            mgoogleMap.setMyLocationEnabled(true);
            mgoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mgoogleMap.setMyLocationEnabled(false);
            mgoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        if (weHavePermission) {
            //Creating an instance of the locationClient provided by google play services
            myLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            finalLocation = new LatLng(myLocation.getLongitude(), myLocation.getLatitude());
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == normal.getId()) {
            // Set the map to normal
        }

        if (v.getId() == satelite.getId()) {
            // Set the map to Satelite
        }
        if (v.getId() == location.getId()) {
            if (mapReady) {
                checkPermission();
                if (myLocation != null) {
                    Log.e("Main Activity", "Latitude" + myLocation.getLatitude());
                    Log.e("Main Activity", "Longitude" + myLocation.getLongitude());
                    flyTo(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
                    addMarker();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        mgoogleMap = googleMap;


        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(12032, 2434))
                .zoom(14)
                .build();
        mgoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    weHavePermission = true;
                } else {
                    // We don't have the permission
                }
            }
        }
    }

    private void addMarker(){
        mylocationMarker = new MarkerOptions().position(finalLocation)
                .title("My house")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_marker_48));

        /*
        * Putting the markers from the OnCreate method
        * **/
        mgoogleMap.addMarker(mylocationMarker);
        Log.e("Main Activity", "Ya agregue el MARKER a position:" + finalLocation);
    }
    private void init() {
        normal = (Button) findViewById(R.id.normal);
        satelite = (Button) findViewById(R.id.satelite);
        location = (Button) findViewById(R.id.location);
    }


    private void flyTo(LatLng lg) {
        mgoogleMap.animateCamera(CameraUpdateFactory.newLatLng(lg), 10000, null);
    }


    private void createLocationRequest() {
        updateLocation = new LocationRequest();
        updateLocation.setInterval(INTERVAL);
        updateLocation.setFastestInterval(FASTEST_INTERVAL);
        updateLocation.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
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
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, updateLocation, this);
        Log.d("Main activity", "Location update started ..............: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("Main activity", "Firing onLocationChanged..............................................");
        myLocation = location;
        Log.e("Location Change", "NEW LOCATION"+ location.getLongitude());
        Log.e("Location Change", "NEW LOCATION"+ location.getAltitude());

        if(myLocation!=null){
            finalLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            Log.e("Main activity", "UBICACION FINAL"+ finalLocation);
        }
    }
}
