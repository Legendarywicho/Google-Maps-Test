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
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;

    private Button normal;
    private Button satelite;
    private Button location;
    private ArrayList <MarkerOptions> mo;
    private LatLng seatle;
    private LatLng dublin;
    private LatLng toyko;
    private LatLng myLocation;
    private GoogleApiClient client;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mgoogleMap;
    private Boolean mapReady = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        seatle = new LatLng(47.606430, -122.320807);
        dublin = new LatLng(53.349503, -6.269386);
        toyko = new LatLng(35.707838, 139.728919);

        mo = new ArrayList<>();

        mo.add(new MarkerOptions().position(dublin)
                .title("Dublin")
                .icon(BitmapDescriptorFactory
                .fromResource(R.drawable.icons8_marker_48)));
        mo.add(new MarkerOptions().position(toyko)
                .title("Toyko")
                .icon(BitmapDescriptorFactory
                .fromResource(R.drawable.icons8_marker_48)));
        mo.add(new MarkerOptions().position(seatle)
                .title("Seatle")
                .icon(BitmapDescriptorFactory
                .fromResource(R.drawable.icons8_marker_48)));

        /**
         * Setting the button's click Listener
         * */
            satelite.setOnClickListener(this);
            location.setOnClickListener(this);
            normal.setOnClickListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        client = new GoogleApiClient.Builder(this)
                .enableAutoManage(MainActivity.this, this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();

        checkPermission();
    }

    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                //  we don't have to block this thread
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    myLocation = new LatLng(location.getLongitude(), location.getAltitude());
                }
            }
        });
    }

    @Override
    public void onClick(View v){
       if(v.getId() == normal.getId()){
           // Set the map to normal
           if(mapReady)
               flyTo(seatle);
       }

       if(v.getId() == satelite.getId()){
           // Set the map to Satelite
           if(mapReady)
               flyTo(toyko);
       }
       if(v.getId() == location.getId()){
           // Set the map to hybrid
           if(mapReady){
               if(myLocation!=null){
                   flyTo(myLocation);
               }
               else{
                   Toast.makeText(getBaseContext(), "GPS EMPTY", Toast.LENGTH_SHORT).show();
               }
           }

       }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady =  true;
        mgoogleMap = googleMap;


        CameraPosition cameraPosition = CameraPosition.builder()
                .target(seatle)
                .zoom(14)
                .build();
        mgoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        /*
        * Putting the markers from the OnCreate method
        * **/
        for (int i = 0; i<mo.size(); i++)
        mgoogleMap.addMarker(mo.get(i));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
                else{
                    // We don't have the permission
                }
            }
        }
    }

    private void init(){
        normal = (Button) findViewById(R.id.normal);
        satelite = (Button) findViewById(R.id.satelite);
        location = (Button) findViewById(R.id.location);
    }



    private void flyTo(LatLng lg){
        mgoogleMap.animateCamera(CameraUpdateFactory.newLatLng(lg), 10000, null);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
