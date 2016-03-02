package com.washmywhip.washmywhip;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionBarContainer;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity implements AboutFragment.OnFragmentInteractionListener, PaymentFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, OnMapReadyCallback, View.OnClickListener, AdapterView.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private GoogleMap mMap;
    private Marker mMarker;
    private Context mContext;
    private View currentView;
    private UserState userState;
    private Fragment currentFragment;
    private LatLng currentLocation;
    private SharedPreferences mSharedPreferences;
    private Geocoder mGeocoder;
    private String mAddress;
    private static final String SET_LOCATION = "Set Location";

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(),location.getLongitude());;
            if(currentLocation!=null && mMap!=null){
                //Only force a camera update if user has traveled 0.1 miles from last location
                if(distance(currentLocation.latitude,currentLocation.longitude,loc.latitude,loc.longitude)>0.1){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                    currentLocation = loc;
                }

            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            }
            currentLocation = loc;
            //makes custome icon for markers... might be useful to mark vendors
            //IconGenerator factory = new IconGenerator(getApplicationContext());
            //Bitmap icon = factory.makeIcon("Set Location");
            // mMarker = mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(icon)));
        }
    };


    private GoogleMap.OnCameraChangeListener myCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {

            //mGeocoder = new Geocoder(MainActivity.this);
            LatLng cameraLocation = cameraPosition.target;
            try {
                List<Address> addressList = mGeocoder.getFromLocation(cameraLocation.latitude, cameraLocation.longitude, 1);
                if (addressList.size() > 0) {
                    String address = addressList.get(0).getAddressLine(0);
                    String city = addressList.get(0).getAddressLine(1);
                    String country = addressList.get(0).getAddressLine(2);
                    mAddress = address + " " + city + ", " + country;

                    addressText.setText(mAddress);
                    if(addressText.hasFocus()){
                        hideKeyboard(addressText);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private GoogleMap.OnMapClickListener myMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            Log.d(TAG, "clearing focus on address text");
            if(addressText.hasFocus()){
                Log.d(TAG, "address text has focus");
                hideKeyboard(addressText);
            }
        }
    };


    private ActionBarDrawerToggle mDrawerToggle;
    private String[] navigationOptions;
    private CharSequence mTitle, mDrawerTitle;
    @InjectView(R.id.setLocationButton)
    Button setLocationButton;
    @InjectView(R.id.addressText)
    EditText addressText;
    @InjectView(R.id.requestingLayout)
    RelativeLayout requestingLayout;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.cancelToolbarButton)
    TextView cancelButton;
    @InjectView(R.id.mDrawerLayout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.mListView)
    ListView navDrawerList;

    FrameLayout contentFrame;
    Button signOutButton;
    Button requestWashButton;
    TextView confirmedAddress;
    SupportMapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TEST", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_nav_drawer);
        ButterKnife.inject(this);
        toolbar.setTitle("");
        currentFragment = null;
        userState = UserState.CONFIRMING;
        setSupportActionBar(toolbar);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGeocoder = new Geocoder(this);
        navigationOptions = new String[]{"Wash My Whip", "Profile", "Payment", "About", "Sign out"};
        initDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        currentView = requestingLayout;
        cancelButton.setText("");
        setLocationButton.setOnClickListener(this);
        addressText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.d(TAG, "entered address");
                    setCameraToUserInput();
                }
                return false;
            }
        });
        addressText.setOnFocusChangeListener(new TextView.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "edir text focus: " + hasFocus);
            }
        });
    }


    @Override
    protected void onPostResume(){
        Log.d("TEST", "onPostResume");
        super.onPostResume();
    }


    private void initDrawer() {
        Log.d("TEST", "initDrawer");
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawerOpen, R.string.drawerClose) {
            @Override
            public void onDrawerClosed(View drawerView) {
                // getSupportActionBar().setTitle("Closed");
                invalidateOptionsMenu();

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //  getSupportActionBar().setTitle("Open");
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        navDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, navigationOptions));
        navDrawerList.setOnItemClickListener(this);

        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);

        Log.d("initDrawer Method", "isfiring");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("TEST", "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_nav_drawer_drawer, menu);
        //initMenuTextView(userState,fra);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("TEST", "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("TEST","map ready");
        allowLocationServices(true);
        addressText.setText(getMyLocationAddress());
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mMap.setOnCameraChangeListener(myCameraChangeListener);
        mMap.setOnMapClickListener(myMapClickListener);
        if(addressText.hasFocus()){
            hideKeyboard(addressText);
        }
    }


    public String getMyLocationAddress(){
        String mAddress = "";
        LatLng newLocation = mMap.getCameraPosition().target;
        try {
            List<Address> addressList = mGeocoder.getFromLocation(newLocation.latitude, newLocation.longitude, 1);
            if(addressList.size()>0){
                String address = addressList.get(0).getAddressLine(0);
                String city = addressList.get(0).getAddressLine(1);
                String country = addressList.get(0).getAddressLine(2);
                mAddress = address + " " + city + ", " + country;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mAddress;
    }

    public void setCameraToUserInput(){

        List<Address> addresses = null;
        try {
            addresses = mGeocoder.getFromLocationName(addressText.getText().toString(),5);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses == null){

        }
        else if(addresses.size() > 0)
        {
            LatLng inputLocation = new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(inputLocation, 16.0f));
            addressText.setText(addresses.get(0).getAddressLine(0));
        }
        else
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("Invalid Location");
            adb.setMessage("Please Provide the Proper Place");
            adb.setPositiveButton("OK",null);
            adb.show();
        }

    }

    public void swapView(int v) {
        Log.d("TEST", "swapView");
        ViewGroup parent = (ViewGroup) currentView.getParent();
        int index = parent.indexOfChild(currentView);
        parent.removeView(currentView);
        int view = v;
        //updates currentView
        currentView = getLayoutInflater().inflate(view, parent, false);
        parent.addView(currentView, index);
    }

    public void allowLocationServices(boolean allow){

        if(allow) {
    //enable
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
            mMap.setMyLocationEnabled(true);
        } else {
    //disable
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
            mMap.setMyLocationEnabled(false);
        }


    }


    public void initMenuTextView(UserState state){
        if(state == UserState.REQUESTING){
            //invis
            cancelButton.setVisibility(View.GONE);
        } else {
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setText("Cancel");
        }
    }


    @Override
    public void onClick(View v) {
        Log.d("TEST", "onClick");
        if(v.getId()==R.id.setLocationButton){
            initConfirming();
        } else if(v.getId() == R.id.requestWashButton){
            initQueued();
        } else if (v.getId() == cancelButton.getId()){
            if(cancelButton.getText().toString().equals("Cancel")){
                Log.d("MENU TEXTVIEW", "CANCEL CLICK");


                int view = R.layout.requesting_layout;
                swapView(view);
                initRequesting();

            } else if (cancelButton.getText().toString().equals("Edit")){
                Log.d("MENU TEXTVIEW", "EDIT CLICK");

            }


        } else if(v.getId() == R.id.cancelQueue) {
            initArrived();
        } else if(v.getId() == R.id.contactButton) {
            Log.d("contactRequest","contect requested");
            initWashing();
        } else if(v.getId() == R.id.washingContact) {
            Log.d("contactRequest","contect requested");
            initFinalizing();
        }
    }

    public void initRequesting() {

        userState = UserState.REQUESTING;
        setLocationButton = (Button) findViewById(R.id.setLocationButton);
        setLocationButton.setOnClickListener(this);

        addressText = (EditText) findViewById(R.id.addressText);
        addressText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.d(TAG, "entered address");
                }
                return false;
            }
        });
        mMap.setOnCameraChangeListener(myCameraChangeListener);
        //unlock camera postion
        mMap.getUiSettings().setAllGesturesEnabled(true);
        allowLocationServices(true);
        addressText.setText(getMyLocationAddress());
        if(addressText.hasFocus()){
            hideKeyboard(addressText);
        }
        cancelButton = (TextView) findViewById(R.id.cancelToolbarButton);
        cancelButton.setOnClickListener(null);
        cancelButton.setVisibility(View.GONE);
    }
    public void initConfirming() {
        userState = UserState.CONFIRMING;
        if(mMarker!= null){
            mMarker.remove();
        }

        LatLng newLocation = mMap.getCameraPosition().target;
        mMarker = mMap.addMarker(new MarkerOptions().position(newLocation).visible(false));

        //Lock camera postion
        mMap.getUiSettings().setAllGesturesEnabled(false);
        allowLocationServices(false);

        //add cancel button to action bar
        cancelButton = (TextView) findViewById(R.id.cancelToolbarButton);
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setOnClickListener(this);
        cancelButton.setText("Cancel");


        int view = R.layout.confirming_request_layout;
        swapView(view);

        //RecyclerView carSelector = (RecyclerView) findViewById(R.id.carSelect);
        //carSelector.setAdapter(null);
        requestWashButton = (Button) findViewById(R.id.requestWashButton);
        requestWashButton.setOnClickListener(this);


        confirmedAddress = (TextView) findViewById(R.id.confirmedAddress);
        confirmedAddress.setText(addressText.getText());
    }
    public void initQueued(){
        Log.d(TAG, "Wash Requested");
        userState = UserState.QUEUED;
        //userState - UserState.WASHING
        //View view = this.getLayoutInflater().inflate(R.layout.queued_layout,9, false);
        //this.addContentView(R.layout.queued_layout);

        int view = R.layout.queued_layout;
        swapView(view);

        cancelButton = (TextView) findViewById(R.id.cancelToolbarButton);
        cancelButton.setOnClickListener(this);
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setText("Cancel");

        Button cancelQueueButton = (Button)findViewById(R.id.cancelQueue);
        cancelQueueButton.setOnClickListener(this);
        //Queued popup

    }
    public void initWaiting() {
        int view = R.layout.waiting_layout;
        swapView(view);
        cancelButton = (TextView) findViewById(R.id.cancelToolbarButton);
        cancelButton.setOnClickListener(this);
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setText("Cancel");
    }
    public void initArrived() {
//can no longer cancel
        int view = R.layout.arrived_layout;
        swapView(view);
        cancelButton = (TextView) findViewById(R.id.cancelToolbarButton);
        cancelButton.setOnClickListener(null);
        cancelButton.setVisibility(View.INVISIBLE);

        Button contactButton = (Button) findViewById(R.id.contactButton);
        contactButton.setOnClickListener(this);

    }
    public void initWashing() {
        int view = R.layout.washing_layout;
        swapView(view);
        cancelButton = (TextView) findViewById(R.id.cancelToolbarButton);
        cancelButton.setOnClickListener(null);
        cancelButton.setVisibility(View.INVISIBLE);

        Button contactButton = (Button) findViewById(R.id.washingContact);
        contactButton.setOnClickListener(this);
    }
    public void initFinalizing(){
        /*
        int view = R.layout.washing_layout;
        swapView(view);
        cancelButton = (TextView) findViewById(R.id.cancelToolbarButton);
        cancelButton.setOnClickListener(null);
        cancelButton.setVisibility(View.INVISIBLE);
        */
    }



    private void selectItem(int position) {
        Log.d("TEST", "selectItem");
        // Create a new fragment and specify the planet to show based on position
        if (position == 0) {
            // retunr to mapview + state view
            //moar states
            addCurrentStateView();
            currentFragment = new Fragment();

            mapFragment.getView().setVisibility(View.VISIBLE);
            initMenuTextView(userState);
            //In the initial state, nothing to go back to



        } else if (position == 1) {
            Log.d("TEST", "PROFILE");
            currentFragment = ProfileFragment.newInstance();
            mapFragment.getView().setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setText("Edit");
            removeCurrentStateView();

        } else if (position == 2) {
            Log.d("TEST", "PAYMENT");
            currentFragment = PaymentFragment.newInstance();
            mapFragment.getView().setVisibility(View.INVISIBLE);
            removeCurrentStateView();
            cancelButton.setVisibility(View.GONE);

        } else if (position == 3) {
            Log.d("TEST", "ABOUT");
            currentFragment = AboutFragment.newInstance();
            mapFragment.getView().setVisibility(View.INVISIBLE);
            removeCurrentStateView();
            cancelButton.setVisibility(View.GONE);
        } else if(position == 4) {
            //log out
            attemptLogout();

        }

        if(addressText.hasFocus()){
            hideKeyboard(addressText);
        }
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        if(currentFragment!=null){
            fragmentManager.beginTransaction().replace(R.id.contentFrame, currentFragment).commit();
        }
        // Highlight the selected item, update the title, and close the drawer
        navDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(navDrawerList);
    }

    private void attemptLogout() {
        //check sharedPreferences for userstate... if passed arrived cannot log out
        mSharedPreferences.edit().clear().commit();
        Intent i = new Intent(this,LoginActivity.class);
        startActivity(i);
        finish();
    }

    /** calculates the distance between two locations in MILES */
    // http://stackoverflow.com/questions/18170131/comparing-two-locations-using-their-longitude-and-latitude
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;
        return dist; // output distance, in MILES
    }

    public void removeCurrentStateView(){
        /*
        if(userState==UserState.REQUESTING){

        } else if(userState == UserState.CONFIRMING) {

        } else if(userState == UserState.QUEUED) {

        } else if(userState == UserState.WAITING) {

        } else if(userState == UserState.ARRIVED) {

        } else if(userState == UserState.WASHING) {

        } else if(userState == UserState.FINALIZING) {

        }
        */
        currentView.setVisibility(View.GONE);
    }

    public void addCurrentStateView(){

        if(userState==UserState.REQUESTING){
            initRequesting();
        } else if(userState == UserState.CONFIRMING) {
            initConfirming();
        } else if(userState == UserState.QUEUED) {
            initQueued();
        } else if(userState == UserState.WAITING) {
            initWaiting();
        } else if(userState == UserState.ARRIVED) {
            initArrived();
        } else if(userState == UserState.WASHING) {
            initWashing();
        } else if(userState == UserState.FINALIZING) {
            initFinalizing();
        }
        currentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("TEST", "onItemClick");
        selectItem(position);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void showKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
