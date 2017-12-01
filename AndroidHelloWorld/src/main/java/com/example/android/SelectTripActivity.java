package com.example.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SelectTripActivity extends FragmentActivity implements OnMapReadyCallback {
    // Curious and useful fact: 0.1 degrees in latitude/longitude are equivalent
    // to 11 km of the Earth surface
    private GoogleMap mMap;
    private MapHandler mapHandler;
    private NearUsersHandler nearHandler;
    Location lastLoc;
    EditText searchDest;
    Marker origMarker, destMarker;
    private Button confirmTripBtn;
    private boolean onThisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		UserStatus st = UserInfo.getInstance().getUserStatus();
		if((st != UserStatus.D_ON_DUTY) && (st != UserStatus.P_IDLE))
			Log.e("TripInfoActivity", "Critical bug: user shouldnt be here!");

        onThisActivity = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_trip);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO: Generate this via app-server
        ArrayList<NearUserInfo> nu = new ArrayList<>();
        nu.add(new NearUserInfo(new LatLng(-34.74, -58.44) , "Juan", true));
        nu.add(new NearUserInfo(new LatLng(-34.7261, -58.419) , "Ale", false));
        nu.add(new NearUserInfo(new LatLng(-34.7224, -58.39853) , "Cami", false));
        nu.add(new NearUserInfo(new LatLng(-34.74205, -58.394) , "Euge", true));

        mapHandler = new MapHandler();
        nearHandler = new NearUsersHandler(nu);

        searchDest = (EditText) findViewById(R.id.searchDest);
        origMarker = null;
        destMarker = null;

        confirmTripBtn = (Button) findViewById(R.id.confirmTripBtn);
        if(UserInfo.getInstance().isDriver()){
            confirmTripBtn.setText("FIND TRIPS");
            confirmTripBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityChanger.getInstance().gotoActivity(SelectTripActivity.this, ChoosePassengerActivity.class);
                }
            });
        } else {
            confirmTripBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (origMarker == null) {
                        Toast.makeText(getApplicationContext(), "Please turn on your GPS and reload", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (destMarker == null) {
                        Toast.makeText(getApplicationContext(), "Select a destination first", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mapHandler.createPathInfo();
                }
            });
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        if(nearHandler != null)
            nearHandler.startDrawingNearest();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(nearHandler != null)
            nearHandler.stopDrawingNearest();
    }

    /**
     * Overrided method for returning to parent {@link Activity}.
     * @param item {@link MenuItem} clicked on {@link android.app.ActionBar}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Log.d("SelectTripActivity", "Back button pressed on actionBar");
        ActivityChanger.getInstance().gotoActivity(SelectTripActivity.this, MainActivity.class);
        finish();
        return true;

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

        UserInfo ui = UserInfo.getInstance();

        if (ui.isDriver()) {
            // Driver cannot mark destinations
            searchDest.setClickable(false);
            searchDest.setVisibility(View.INVISIBLE);
        } else {
            searchDest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(origMarker == null) {
                        Toast.makeText(getApplicationContext(), "Please, turn on your GPS and reload", Toast.LENGTH_SHORT).show();
                        InputMethodManager im = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        im.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        return;
                    }

                    final Dialog dialog = new Dialog(SelectTripActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.destination_input_box);
                    final EditText destSearchName = (EditText) dialog.findViewById(R.id.searchDestTxt);
                    final ListView destList = (ListView) dialog.findViewById(R.id.destList);
                    int[] colors = {0, 0xFFFFFFFF, 0};
                    destList.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
                    destList.setDividerHeight(2);
                    List<String> initialList = new ArrayList<>();
                    final ArrayAdapter mAdapter = new ArrayAdapter(SelectTripActivity.this, R.layout.destinations_item, initialList);
                    destList.setAdapter(mAdapter);

                    Button bt = (Button) dialog.findViewById(R.id.btSearchDest);
                    bt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Update list!
                            String desiredDest = destSearchName.getText().toString();
                            if(desiredDest.length() == 0)   return;
                            // Add country to make it more specific
                            String country = UserInfo.getInstance().getCountry();
                            if(country.length() > 1)
                                if(!desiredDest.contains(country))
                                    desiredDest = desiredDest + ", " + country;
                            mAdapter.clear();
                            try {
                                FetchAddressService addServ = new FetchAddressService(SelectTripActivity.this, destList, mapHandler);
                                addServ.execute(desiredDest);
                            } catch (Exception e) {
                                Log.e("SelectTripActivity", "Exception: ", e);
                            }

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
        }

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                marker.hideInfoWindow();
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if((origMarker == null) || (destMarker == null))
                    return;
                mapHandler.generatePath(origMarker.getPosition(), destMarker.getPosition());
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(UserInfo.getInstance().isDriver())
                    return;
                if(origMarker == null) {
                    Toast.makeText(getApplicationContext(), "Please, turn on your GPS and reload", Toast.LENGTH_SHORT).show();g
                    return;
                }
                if(destMarker == null) {
                    destMarker = mMap.addMarker(new MarkerOptions().position(latLng).draggable(true).title("Destination")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
                else
                    destMarker.setPosition(latLng);
                mapHandler.generatePath(origMarker.getPosition(), destMarker.getPosition());
            }
        });

        FusedLocationProviderClient flpc = LocationServices.getFusedLocationProviderClient(this);
        flpc.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        lastLoc = location;
                        LatLng origin = new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude());
                        // Add origin and destination marker if user aint driver
                        if(!UserInfo.getInstance().isDriver()) {
                            origMarker = mMap.addMarker(new MarkerOptions().position(origin).draggable(true).title("Pick me here!"));
                            mapHandler.addTextToMarker(origMarker);

                      //      LatLng dest = new LatLng(lastLoc.getLatitude() + 0.01, lastLoc.getLongitude() + 0.01);
                      //      destMarker = mMap.addMarker(new MarkerOptions().position(dest).draggable(true).title("Destination")
                       //             .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        //    mapHandler.addTextToMarker(destMarker);
                        }





                        // Creates a border of around 1km
                        LatLngBounds border = new LatLngBounds(new LatLng(origin.latitude - 0.0455, origin.longitude - 0.0455),
                                                    new LatLng(origin.latitude + 0.0455, origin.longitude + 0.0455));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(border, 0));
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Please, turn on your GPS and reload", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        nearHandler.startDrawingNearest();
        flpc.getLastLocation();

    }

// -----------------------------------------------------------------------------------------------------

    public static class NearUserInfo{
        private LatLng pos;
        private String uName;
        private boolean isDriver;

        public NearUserInfo(LatLng pos, String name, boolean isDriver){
            this.pos = pos;
            uName = name;
            this.isDriver = isDriver;
        }

        public LatLng getLocation(){
            return pos;
        }

        public String getName(){
            return uName;
        }

        public boolean isDriver(){
            return isDriver;
        }

    }

// -----------------------------------------------------------------------------------------------------

    public class MapHandler implements RestUpdate {
        private List<Address> destinations;
        private Polyline path;
        private Marker infoMarker;

        public MapHandler(){
            destinations = null;
            path = null;
            infoMarker = null;
        }


        public void setFoundDestinations(List<Address> foundDestinations){
            this.destinations = foundDestinations;
            Log.d("SelectTripActivity", "MapHandler received destinations: " + destinations.size());
        }

        public void pickFoundDestination(int destOffset){
            // Sanity check
            if(UserInfo.getInstance().isDriver())
                return;

            if(origMarker == null) {
                Toast.makeText(getApplicationContext(), "Please, turn on your GPS and reload", Toast.LENGTH_SHORT).show();
                return;
            }
            Address addr = this.destinations.get(destOffset);
            LatLng dest = new LatLng(addr.getLatitude(), addr.getLongitude());
            if(destMarker == null) {
                destMarker = mMap.addMarker(new MarkerOptions().position(dest).draggable(true).title("Destination")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
            else
                destMarker.setPosition(dest);

            Log.d("SelectTripActivity", "MapHandler using " + destinations.size() + " destinations");
            generatePath(origMarker.getPosition(), dest);
        }

        public void addTextToMarker(Marker mMarker){
            if(mMarker == null) return;
            FetchAddressService addServ= new FetchAddressService(SelectTripActivity.this, mMarker);
            addServ.execute((String) null);
        }

        private void drawPath(ArrayList<LatLng> pathPoints){
            if(path != null)
                path.remove();
            PolylineOptions pathOptions = new PolylineOptions();
            path = mMap.addPolyline(pathOptions);

            path.setPoints(pathPoints);
            path.setColor(Color.BLUE);
            path.setWidth(12);
            origMarker.showInfoWindow();

            int middle = pathPoints.size()/2;
            LatLng middlePoint = pathPoints.get(middle);
            Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.info_marker);
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(img);
            String distance = PathInfo.getInstance().getDistance() + "km";
            infoMarker = mMap.addMarker(new MarkerOptions().position(middlePoint).draggable(false)
                    .title(distance).icon(bitmapDescriptor));
            infoMarker.showInfoWindow();

            addTextToMarker(origMarker);
            addTextToMarker(destMarker);
        }

        public void createPathInfo() {
            PathInfo pi = PathInfo.getInstance();
            pi.setPath(path.getPoints());
            String origTxt = origMarker.getSnippet();
            String destTxt = destMarker.getSnippet();
            pi.setAddresses(origTxt, destTxt);

            ActivityChanger.getInstance().gotoActivity(SelectTripActivity.this, TripInfoActivity.class);
        }

        public void generatePath(LatLng orig, LatLng dest){
            if(infoMarker != null)
                infoMarker.remove();
            if(path != null)
                path.remove();
            try {
                Jsonator jnator = new Jsonator();
                String toSendJson = jnator.writeDirectionsInfo(orig, dest);
                Log.d("SelectTripActivity", "JSON to send: " + toSendJson);
                ConexionRest conn = new ConexionRest(this);
                String dirUrl = conn.getBaseUrl() + "/directions";
                Log.d("SelectTripActivity", "URL to POST directions: " + dirUrl);
                conn.generatePost(toSendJson, dirUrl, null);
            }
            catch(Exception e){
                Log.e("SelectTripActivity", "Sunmitting PUT error: ", e);
            }
        }

        @Override
        public void executeUpdate(String servResponse) {
            Log.d("SelectTripActivity", "Received path: " + servResponse);
            Jsonator jnator = new Jsonator();
            ArrayList<LatLng> pathPoints = jnator.readDirectionsPath(servResponse, false);
            drawPath(pathPoints);
        }
    }


// ----------------------------------------------------------------------------------------

    public class NearUsersHandler implements RestUpdate{
        private ArrayList<Marker> nearUsersMarkers;
        private ArrayList<NearUserInfo> nearest;
        private boolean meDrawing;
        Handler handler;
        Thread tUpdate;

        public NearUsersHandler(ArrayList<NearUserInfo> nearest){
            this.nearest = nearest;
            this.nearUsersMarkers = new ArrayList<>();
            handler = new Handler();
            meDrawing = false;
        }

        public void updateNearest(){
            try {
                ConexionRest conn = new ConexionRest(this);
                String nearestUrl = conn.getBaseUrl() + "/users?limit=36&sort=near";
                Log.d("SelectTripActivity", "URL to GET near users: " + nearestUrl);
                conn.generateGet(nearestUrl, null);
            }
            catch(Exception e){
                Log.e("SelectTripActivity", "GET trips error: ", e);
            }
        }

        public void startDrawingNearest(){
            meDrawing = true;
            tUpdate = new Thread() {
                public void run(){
                    while(meDrawing){
                        try {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("NearUsersHandler", "Updating loop");
                                    updateNearest();
                                }
                            });
                            sleep(10000);
                        } catch (Exception e) {
                            Log.e("SelectTripActivity", "Error: ", e);
                        }
                    }
                }
            };
            tUpdate.start();
        }

        public void stopDrawingNearest(){
            meDrawing = false;
            //tUpdate.interrupt();
        }


        public void drawNearest(){
            Iterator<Marker> itm = nearUsersMarkers.iterator();
            while(itm.hasNext()) {
                // Remove all markers from map
                itm.next().remove();
            }
            nearUsersMarkers.clear();

            Iterator<NearUserInfo> it = nearest.iterator();
            while(it.hasNext()){
                NearUserInfo anUser = it.next();
                LatLng pos = anUser.getLocation();
                String name = anUser.getName();
                Bitmap img;
                if(!anUser.isDriver())
                    img = BitmapFactory.decodeResource(getResources(), R.drawable.passenger_marker);
                else
                    img = BitmapFactory.decodeResource(getResources(), R.drawable.car_marker);
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(img);
                Marker nMarker = mMap.addMarker(new MarkerOptions().position(pos).draggable(false).title(name)
                        .icon(bitmapDescriptor));
                nearUsersMarkers.add(nMarker);
            }
        }

        @Override
        public void executeUpdate(String servResponse) {
            Log.d("SelectTripActivity", "Received users: " + servResponse);
            Jsonator jnator = new Jsonator();
            ArrayList<NearUserInfo> newNearest = jnator.readNearUsers(servResponse);
            if(newNearest != null){
                this.nearest = newNearest;
                this.drawNearest();
            }
        }
    }

}
