package io.luis_santiago.googlemaps.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class MyService extends Service {
    private final IBinder iBinder = new LocalBinder();
    private LatLng mCurrentLocation;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    public class LocalBinder extends Binder{
        public MyService getService(){
            return MyService.this;
        }
    }

    public void setCurrentLocation(LatLng lng){
        mCurrentLocation = lng;
        Log.e("Services", "NEW LOCATION SET UP IN SERVICES");
    }

}
