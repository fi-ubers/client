package com.example.Principal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Iterator;

public class SelectTripActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapHandler mapHandler;
    Location lastLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_trip);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // TODO: Generate this via app-server
        ArrayList<NearUserInfo> nu = new ArrayList<>();
        nu.add(new NearUserInfo(new LatLng(-34.74, -58.44) , "Juan"));
        nu.add(new NearUserInfo(new LatLng(-34.7261, -58.419) , "Ale"));
        nu.add(new NearUserInfo(new LatLng(-34.7224, -58.39853) , "Cami"));
        nu.add(new NearUserInfo(new LatLng(-34.74205, -58.394) , "Euge"));

        mapHandler = new MapHandler(nu);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        FusedLocationProviderClient flpc = LocationServices.getFusedLocationProviderClient(this);
        flpc.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        lastLoc = location;

                        // Add origin and destination markers
                        LatLng origin = new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(origin).draggable(true).title("Pick me here!"));

                        LatLng dest = new LatLng(lastLoc.getLatitude()+0.009162, lastLoc.getLongitude()-0.003966);
                        mMap.addMarker(new MarkerOptions().position(dest).draggable(true).title("Destination")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        mapHandler.drawNearest();

                        // TODO: Get path from app-server
                        ArrayList<LatLng> pathPoints = new ArrayList<>();
                        pathPoints.add(origin);
                        pathPoints.add(new LatLng(-34.73021, -58.40887));
                        pathPoints.add(dest);
                        mapHandler.drawPath(pathPoints);

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                    }
                }
            });

        flpc.getLastLocation();

    }

// -----------------------------------------------------------------------------------------------------

    public class NearUserInfo{
        private LatLng pos;
        private String uName;

        public NearUserInfo(LatLng pos, String name){
            this.pos = pos;
            uName = name;
        }

        public LatLng getLocation(){
            return pos;
        }

        public String getName(){
            return uName;
        }

    }

// -----------------------------------------------------------------------------------------------------

    public class MapHandler{
        private ArrayList<NearUserInfo> nearest;

        public MapHandler(ArrayList<NearUserInfo> nearest){
            this.nearest = nearest;
        }

        public void drawNearest(){
            Iterator<NearUserInfo> it = nearest.iterator();

            while(it.hasNext()){
                NearUserInfo anUser = it.next();
                LatLng pos = anUser.getLocation();
                String name = anUser.getName();

                Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.car_marker);
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(img);

                mMap.addMarker(new MarkerOptions().position(pos).draggable(false).title(name)
                        .icon(bitmapDescriptor));
            }
        }

        public void drawPath(ArrayList<LatLng> pathPoints){
            PolylineOptions pathOptions = new PolylineOptions();
            Polyline path = mMap.addPolyline(pathOptions);

            path.setPoints(pathPoints);
            path.setColor(Color.BLUE);
            path.setWidth(15);

        }

    }

}
