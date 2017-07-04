package io.luis_santiago.googlemaps;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.*;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.os.Build.VERSION_CODES.M;
import static android.support.v7.widget.AppCompatDrawableManager.get;
import static io.luis_santiago.googlemaps.R.id.map;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener{

    private Button normal;
    private Button satelite;
    private Button hybrid;
    private Boolean mapReady = false;
    private GoogleMap mgoogleMap;
    private LatLng seatle = new LatLng(47.606430, -122.320807);
    private LatLng dublin = new LatLng(53.349503, -6.269386);
    private LatLng toyko = new LatLng(35.707838, 139.728919);
    private ArrayList <MarkerOptions> mo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

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
            hybrid.setOnClickListener(this);
            normal.setOnClickListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
       if(v.getId() == hybrid.getId()){
           // Set the map to hybrid
           if(mapReady)
               flyTo(dublin);
       }
    }


    private void init(){
        normal = (Button) findViewById(R.id.normal);
        satelite = (Button) findViewById(R.id.satelite);
        hybrid = (Button) findViewById(R.id.hybrid);
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

    private void flyTo(LatLng lg){
        mgoogleMap.animateCamera(CameraUpdateFactory.newLatLng(lg), 10000, null);
    }
}
