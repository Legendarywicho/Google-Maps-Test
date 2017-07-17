package io.luis_santiago.googlemaps.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.os.Build.VERSION_CODES.M;


public class MyService extends Service {
    private final IBinder iBinder = new LocalBinder();
    private LatLng mCurrentLocation;
    private Socket mSocket;
    private JSONObject jo = new JSONObject();
    private String URL = "http://192.168.0.3:8000";

    public MyService() {
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try{
            mSocket = IO.socket(URL);
        }
        catch (URISyntaxException e){
            // There was an error
        }

        mSocket.connect();
        Log.e("Service", "-------We are connected to the node js server--------");
    }

    public class LocalBinder extends Binder{
        public MyService getService(){
            return MyService.this;
        }
    }


    public void setCurrentLocation(LatLng lng){
        mCurrentLocation = lng;
        Log.e("Services", "NEW LOCATION SET UP IN SERVICES");


        if(mSocket!=null){
            io.luis_santiago.googlemaps.Location location = new io.luis_santiago.googlemaps.Location();
            location.setLatitude(mCurrentLocation.latitude);
            location.setLongitude(mCurrentLocation.longitude);

            Gson gson = new Gson();
            String json = gson.toJson(location);
            if(mSocket!=null)
            mSocket.emit("new location", json);
        }
    }

}
