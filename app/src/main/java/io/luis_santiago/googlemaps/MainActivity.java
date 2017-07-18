package io.luis_santiago.googlemaps;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import io.luis_santiago.googlemaps.services.MyService;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        View.OnClickListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private static final long INTERVAL = 2000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    // UI buttons
    private Button normal;
    private Button satelite;
    private Button location;
    // data structure for location
    private MarkerOptions mylocationMarker;
    private Location myLocation;
    // this is for the location of the user
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mgoogleMap;
    private Boolean mapReady = false;
    private GoogleApiClient mGoogleApiClient;
    private boolean weHavePermission = false;
    private LocationRequest updateLocation;
    private MarkerOptions a;
    private Marker m;

    /** This is for the bounded service*/

    boolean isBound = false;
    MyService service;

    // To set up correctly a new point map is Latitude and Longitude



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        /*
         * Setting the button's click Listener
         * */

        location.setOnClickListener(this);




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

        /*Setting up the service to send data on to the server*/
        Intent intent = new Intent(MainActivity.this, MyService.class);
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            weHavePermission = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
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
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == location.getId()) {
            if (mapReady) {
                if (myLocation != null) {
                    Log.e("Main Activity", "Latitude" + myLocation.getLatitude());
                    Log.e("Main Activity", "Longitude" + myLocation.getLongitude());
                }else{
                    Log.e("Main Activity", "Something is wrong with the location");
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        mgoogleMap = googleMap;


        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(18.134882,-94.457830))
                .zoom(14)
                .build();

        mgoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        Log.e("Main activity", "------GETTING THE MARKER------");
        a = new MarkerOptions().position(new LatLng( 20, -92))
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.firstperson));
        m = googleMap.addMarker(a);

        checkPermission();
        Log.e("Main Activity", "IM ADDING A NEW MARKER");
        addANewMarker(new LatLng( 20, -92));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    weHavePermission = true;
                } else {
                    // We don't have the permission
                    Log.e("Main activity", "I never got permission");
                }
            }
        }
    }


    private void init() {
        location = (Button) findViewById(R.id.location);
    }

    private void createLocationRequest() {
        updateLocation = new LocationRequest();
        updateLocation.setInterval(INTERVAL);
        updateLocation.setFastestInterval(FASTEST_INTERVAL);
        updateLocation.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        if(mGoogleApiClient.isConnected()){
            startLocationUpdates();
            Log.d("Main Activity", "Location update resumed.........");

        }
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

    // To set up correctly a new point map is longitude and latitude
    @Override
    public void onLocationChanged(Location location) {
        Log.d("Main activity", "Firing onLocationChanged..............................................");
        myLocation = location;
        Log.e("Location Change", "NEW LOCATION: LATITUDE"+ location.getLatitude());
        Log.e("Location Change", "NEW LOCATION LONGITUD"+ location.getLongitude());
        m.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
        if(isBound){
            service.setCurrentLocation(new LatLng(location.getLatitude(),location.getLongitude()));
        }
    }


    /*This is for the callbacks on to the local server*/

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("Main activity", "We are conneted on to the server");
            MyService.LocalBinder binder = (MyService.LocalBinder) iBinder;
            service = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    private void addANewMarker(LatLng lng){
        mgoogleMap.addMarker(new MarkerOptions()
                .position(lng)
                .title("Another person")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.firstperson)));
    }
}
