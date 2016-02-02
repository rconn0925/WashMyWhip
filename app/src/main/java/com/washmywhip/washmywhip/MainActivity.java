package com.washmywhip.washmywhip;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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

            String mAddress = "";
            Geocoder mGeocoder = new Geocoder(MainActivity.this);
            LatLng cameraLocation = cameraPosition.target;
            try {
                List<Address> addressList = mGeocoder.getFromLocation(cameraLocation.latitude, cameraLocation.longitude, 1);
                if (addressList.size() > 0) {
                    String address = addressList.get(0).getAddressLine(0);
                    String city = addressList.get(0).getAddressLine(1);
                    String country = addressList.get(0).getAddressLine(2);
                    mAddress = address + " " + city + ", " + country;
                    addressText.setText(mAddress);
                    addressText.clearFocus();
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
            addressText.clearFocus();
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
        setSupportActionBar(toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setLocationButton.setOnClickListener(this);

        navigationOptions = new String[]{"Wash My Whip", "Profile", "Payment", "About"};
        initDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        currentView = requestingLayout;
        addressText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.d(TAG, "entered address");
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
        //addressText.setText(getMyLocationAddress());
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
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mMap.setOnCameraChangeListener(myCameraChangeListener);
        mMap.setOnMapClickListener(myMapClickListener);
    }


    public String getMyLocationAddress(){
        String mAddress = "";
        LatLng newLocation = mMap.getCameraPosition().target;
        Geocoder mGeocoder = new Geocoder(this);
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

    public void swapView(int v) {
        Log.d("TEST", "swapView");
        ViewGroup parent = (ViewGroup) currentView.getParent();
        int index = parent.indexOfChild(currentView);
        parent.removeView(currentView);
        int view = v;
        currentView = getLayoutInflater().inflate(view, parent, false);
        parent.addView(currentView, index);
    }

    @Override
    public void onClick(View v) {
        Log.d("TEST", "onClick");
        if(v.getId()==R.id.setLocationButton){
            if(mMarker!= null){
                mMarker.remove();
            }

            LatLng newLocation = mMap.getCameraPosition().target;
            mMarker = mMap.addMarker(new MarkerOptions().position(newLocation).visible(false));

            addressText.setText(getMyLocationAddress());

            //Lock camera postion
            mMap.getUiSettings().setAllGesturesEnabled(false);

            int view = R.layout.confirming_request_layout;
            swapView(view);

            requestWashButton = (Button) findViewById(R.id.requestWashButton);
            requestWashButton.setOnClickListener(this);


            confirmedAddress = (TextView) findViewById(R.id.confirmedAddress);
            confirmedAddress.setText(addressText.getText());

        }
        if(v.getId() == R.id.requestWashButton){
            Log.d(TAG, "Wash Requested");

            int view = R.layout.waiting_layout;
            swapView(view);

        }
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


        } else if (position == 1) {
            Log.d("TEST", "PROFILE");
            currentFragment = ProfileFragment.newInstance();
            mapFragment.getView().setVisibility(View.INVISIBLE);
            removeCurrentStateView();

        } else if (position == 2) {
            Log.d("TEST", "PAYMENT");
            currentFragment = PaymentFragment.newInstance();
            mapFragment.getView().setVisibility(View.INVISIBLE);
            removeCurrentStateView();

        } else if (position == 3) {
            Log.d("TEST", "ABOUT");
            currentFragment = AboutFragment.newInstance();
            mapFragment.getView().setVisibility(View.INVISIBLE);
            removeCurrentStateView();
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
        currentView.setVisibility(View.INVISIBLE);
    }

    public void addCurrentStateView(){
        /*
        if(userState==UserState.REQUESTING){

        } else if(userState == UserState.CONFIRMING) {

        } else if(userState == UserState.QUEUED) {

        } else if(userState == UserState.WAITING) {

        } else if(userState == UserState.ARRIVED) {

        } else if(userState == UserState.WASHING) {

        } else if(userState == UserState.FINALIZING) {

        }
        currentView.setVisibility(View.VISIBLE);
        */
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
}
