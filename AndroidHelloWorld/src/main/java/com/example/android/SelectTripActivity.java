package com.example.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SelectTripActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapHandler mapHandler;
    Location lastLoc;
    EditText searchDest;
    Marker origMarker, destMarker;

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

        searchDest = (EditText) findViewById(R.id.searchDest);
        origMarker = null;
        destMarker = null;
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
        mMap.getUiSettings().setMapToolbarEnabled(false);

        searchDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(SelectTripActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.destination_input_box);
                final EditText destSearchName = (EditText) dialog.findViewById(R.id.searchDestTxt);
                final ListView destList = (ListView) dialog.findViewById(R.id.destList);
                int[] colors = {0, 0xFFFFFFFF, 0};
                destList.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
                destList.setDividerHeight(1);
                List<String> initialList = new ArrayList<>();
                final ArrayAdapter mAdapter = new ArrayAdapter(SelectTripActivity.this, R.layout.destinations_item, initialList);
                destList.setAdapter(mAdapter);

                Button bt = (Button) dialog.findViewById(R.id.btSearchDest);
                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Update list!
                        String desiredDest = destSearchName.getText().toString();
                        mAdapter.clear();
                        try {
                            FetchAddressService addServ = new FetchAddressService(SelectTripActivity.this, destList, mapHandler);
                            addServ.execute(desiredDest);
                        }
                        catch (Exception e){
                            Log.e("SelectTripActivity", "Exception: ", e);
                        }

                        destSearchName.setText("");
                    }
                });

                destList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mapHandler.pickFoundDestination(position);
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if((origMarker == null) || (destMarker == null))
                    return;
                // TODO: Get path from app-server
                ArrayList<LatLng> pathPoints = new ArrayList<>();
                pathPoints.add(origMarker.getPosition());
                pathPoints.add(destMarker.getPosition());
                mapHandler.drawPath(pathPoints);
            }
        });

        FusedLocationProviderClient flpc = LocationServices.getFusedLocationProviderClient(this);
        flpc.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        lastLoc = location;

                        // Add origin and destination markers
                        LatLng origin = new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude());
                        origMarker = mMap.addMarker(new MarkerOptions().position(origin).draggable(true).title("Pick me here!"));

                        mapHandler.drawNearest();
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
        private List<Address> destinations;
        private Polyline path;

        public MapHandler(ArrayList<NearUserInfo> nearest){
            this.nearest = nearest;
            destinations = null;
            path = null;
        }


        public void setFoundDestinations(List<Address> foundDestinations){
            this.destinations = foundDestinations;
            Log.d("SelectTripActivity", "MapHandler received destinations: " + destinations.size());
        }

        public void pickFoundDestination(int destOffset){
            Address addr = this.destinations.get(destOffset);
            LatLng dest = new LatLng(addr.getLatitude(), addr.getLongitude());
            if(destMarker == null) {
                destMarker = mMap.addMarker(new MarkerOptions().position(dest).draggable(true).title("Destination")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
            else
                destMarker.setPosition(dest);

            Log.d("SelectTripActivity", "MapHandler using " + destinations.size() + " destinations");
            // TODO: Get path from app-server
            ArrayList<LatLng> pathPoints = new ArrayList<>();
            pathPoints.add(origMarker.getPosition());
            pathPoints.add(dest);
            mapHandler.drawPath(pathPoints);
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
            if(path != null)
                path.remove();
            PolylineOptions pathOptions = new PolylineOptions();
            path = mMap.addPolyline(pathOptions);

            path.setPoints(pathPoints);
            path.setColor(Color.BLUE);
            path.setWidth(15);


        }

    }

}
