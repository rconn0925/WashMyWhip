package com.washmywhip.washmywhip;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionBarContainer;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

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
    private WashMyWhipEngine mWashMyWhipEngine;
    private Typeface mFont;
    boolean isSetup;

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

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            Log.d("LocationListener", "got this location: " + location.toString());
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            if(!isSetup){
                isSetup = true;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f));
            }


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

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

                    if(addressText!=null){
                        addressText.setText(mAddress);
                        if(addressText.hasFocus()){
                            hideKeyboard(addressText);
                        }
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
            if(isLoaded==1&&addressText.hasFocus()){
                Log.d(TAG, "address text has focus");
                hideKeyboard(addressText);
            }
        }
    };


    private ActionBarDrawerToggle mDrawerToggle;
    private String[] navigationOptions;
    private CharSequence mTitle, mDrawerTitle;

    Button setLocationButton;
    EditText addressText;
    @InjectView(R.id.loadingLayout)
    RelativeLayout loadingLayout;
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
    Button contactButton;
    TextView confirmedAddress;
    TextView textContact;
    TextView callContact;
    TextView doneContact;
    SupportMapFragment mapFragment;
    ConnectionManager mConnectionManager;
    int isLoaded;
    private boolean hasBeenRequested;
    private View contactView;
    private Button finalizingSubmit;
    private RatingBar ratingBar;
    private EditText finalizingComments;
    private BroadcastReceiver mMessageReceiver;
    private CircleImageView vendorWaitingImage;
    private CircleImageView arrivedVendorImage;
    private Button contactButtonArrived;
    private Button contactButtonWashing;
    private ArrayList<Car> mCars;
    private CircleImageView finalizingVendorImage;
    private RelativeLayout waitingContactLayout;
    private RecyclerView carDropDown;
    private Marker start;
    private Marker end;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TEST", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_nav_drawer);
        ButterKnife.inject(this);
        mContext = this;
        toolbar.setTitle("");
        currentFragment = null;
        userState = UserState.REQUESTING;
        mFont= Typeface.createFromAsset(getAssets(), "fonts/Archive.otf");
        setSupportActionBar(toolbar);
        isLoaded = -1;
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Extract data included in the Intent
                Log.d("server connection", "RECEIVER: Got INTENT: "+ intent.toString());


                if(intent.hasExtra("state")){
                    String state = intent.getStringExtra("state");
                    Log.d("server connection", "RECEIVER: Got state: " + state);
                    if(state.equals("inactive")){
                        initRequesting();
                    } else if(state.equals("queued")){
                        initQueued();
                    } else if(state.equals("waiting")){
                        initWaiting();
                    } else if(state.equals("arrived")){
                        initArrived();
                    } else if(state.equals("washing")){
                        initWashing();
                    } else if(state.equals("finalizing")){
                        initFinalizing();
                    } else {
                        //??
                    }
                } else if (intent.hasExtra("vendorInfo")){
                    String userInfo = intent.getStringExtra("vendorInfo");
                    Log.d("server connection", "RECEIVER: Got vendorInfo: " + userInfo);
                    String[] info = userInfo.split(", ");
                    int vendorID = Integer.parseInt(info[0]);
                    String vendorLat =info[1];
                    String vendorLong = info[2];

                    mSharedPreferences.edit().putInt("vendorID", vendorID).apply();
                    mSharedPreferences.edit().putString("vendorLat", vendorLat).apply();
                    mSharedPreferences.edit().putString("vendorLong", vendorLong).apply();

                    LatLng vendorLocation = new LatLng(Double.parseDouble(vendorLat),Double.parseDouble(vendorLong));

                    initWaiting();

                    mWashMyWhipEngine.getVendorWithID(vendorID, new Callback<JSONObject>() {
                        @Override
                        public void success(JSONObject o, Response response) {
                            String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                            Map<String, String> userInfo = new HashMap<String, String>();
                            userInfo = parseResponse(responseString);
                            String username = userInfo.get("Username");
                            String phoneNum = userInfo.get("Phone");
                            Log.d("getVendor", "success: " + responseString);
                            mSharedPreferences.edit().putString("vendorUsername", username).apply();
                            mSharedPreferences.edit().putString("vendorPhone", phoneNum).apply();
                            Log.d("getVendor", "success: " + responseString);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.d("getVendor", "success: " + error.getMessage());
                        }
                    });
                }
                else if (intent.hasExtra("vendorHasArrived")){
                    initArrived();
                } else if (intent.hasExtra("vendorHasStartedWash")){
                    initWashing();
                } else if (intent.hasExtra("vendorHasCompletedWash")){
                    initFinalizing();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("com.android.activity.SEND_DATA"));

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

        currentView = loadingLayout;
        int view = R.layout.loading_layout;
        swapView(view);
        cancelButton.setText("");
        mWashMyWhipEngine = new WashMyWhipEngine();
        //setup javaScript connection
        mConnectionManager = null;
        final Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mConnectionManager = new ConnectionManager(getApplicationContext());
                //Log.d("server connection", "isConnectedAfterAdd: " + mConnectionManager.isConnected());
            }
        });
        networkThread.run();
    }

    private Map<String,String> parseResponse(String s) {
        HashMap userData = new HashMap();
        s = s.substring(1, s.length() - 1);
        s = s.replace(" ", "").replace("\t", "").replace(",", "").replace("\"", "");
        String[] dataItem = s.split("\n");
        for (int i = 1; i < dataItem.length; i++) {
            if (dataItem[i].endsWith(":")) {
                dataItem[i] = dataItem[i] + " ";
            }
            String[] info = dataItem[i].split(":");
            String key = info[0];
            String value = info[1];
            userData.put(key, value);
        }
        return userData;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
       // mConnectionManager.disconnect();

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
        Log.d("TEST", "map ready");
        allowLocationServices(true);
        //mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mMap.setOnCameraChangeListener(myCameraChangeListener);
        mMap.setOnMapClickListener(myMapClickListener);
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
                showSettingsAlert();
                return;
            }
            mMap.setMyLocationEnabled(true);
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
            Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastKnownLocation!=null){
                Log.d("LocationTAG", "got last know location");
                currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f));
            }


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
                showSettingsAlert();
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
        if(v.getId()==R.id.setLocationButton) {
            initConfirming();
        } else if(v.getId() == R.id.requestWashButton){
            requestWashButton.setOnClickListener(null);
            final int selectedCarID = mSharedPreferences.getInt("SelectedCarID",-1);
            Log.d("REQUEST WASH", "CarID: " + selectedCarID);
            //VALIDATE DEFAULT CARD
            int userID = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
            if(userID>=0){
                mWashMyWhipEngine.getStripeCustomer(userID, new Callback<JSONObject>() {
                    @Override
                    public void success(JSONObject jsonObject, Response response) {
                        String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                        JSONObject json = null;
                        try {
                            json = new JSONObject(responseString);
                            Map<String, Object> paymentData = jsonToMap(json);
                            //  Log.d("getCards",""+json.toString());
                            Log.d("getCards", "default source: " + json.getString("default_source"));
                            String defaultCard = json.getString("default_source");
                            mSharedPreferences.edit().putString("default_source", defaultCard).apply();

                            if (defaultCard != null && defaultCard.length() > 5) {
                                Log.d("getCards", "default source valid");
                                int washType = 0;
                                if (mConnectionManager.isConnected()) {
                                    mConnectionManager.requestWash(mMarker.getPosition(), selectedCarID, washType);
                                }
                                initQueued();
                            } else {
                                Log.d("getCards", "default source invalid");
                                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("You have not added a payment method!");
                                builder.setMessage("Please add a payment method before requesting a wash!");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        initRequesting();
                                        dialog.cancel();
                                    }
                                });
                                builder.show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                            Log.d("getCards", "failz: " + error.getMessage());
                    }
                });
            }

        } else if (v.getId() == cancelButton.getId()){
            if(cancelButton.getText().toString().equals("Cancel")){
                Log.d("MENU TEXTVIEW", "CANCEL CLICK");

                int view = R.layout.requesting_layout;
                swapView(view);
                mMap.clear();
                initRequesting();
                if(mConnectionManager.isConnected()){
                    mConnectionManager.cancelRequest();
                }

            } else if (cancelButton.getText().toString().equals("Edit")){
                Log.d("MENU TEXTVIEW", "EDIT CLICK");

            }

        } else if(v.getId() == R.id.cancelQueue) {
           // initArrived();
        } else if(v.getId() == R.id.arrivedContact) {
            Log.d("contactRequest", "contact requested");
            contactButtonArrived.setOnClickListener(null);
            initContact();

        } else if(v.getId() == R.id.washingContact) {
            Log.d("contactRequest","contact requested");
            contactButtonWashing.setOnClickListener(null);
            initContact();
        } else if(v.getId() == R.id.finalizingSubmitButton) {
            int rating = ratingBar.getProgress();
            String comments = finalizingComments.getText().toString();
            int transactionID = Integer.parseInt(mSharedPreferences.getString("transactionID","-1"));
            hideKeyboard(finalizingComments);
            Log.d("finalizing", "submit: " + comments + ", " + rating +", "+ transactionID);

            if(transactionID>=0){
                mWashMyWhipEngine.rateVendor(transactionID, rating, comments, new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        Log.d("rateVendor", "success: " +s);
                        if(mConnectionManager.isConnected()){
                            mConnectionManager.userHasFinalized();
                        }
                        initRequesting();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("rateVendor", "failz: " +error.getMessage());
                    }
                });
            }

        } else if(v.getId() == R.id.contactCall) {
            Log.d("contact", "call");
            removeContact();
            contactCallVendor();
        } else if(v.getId() == R.id.contactText) {
            Log.d("contact", "call");
            removeContact();
            contactTextVendor();
        } else if(v.getId() == R.id.contactDone) {
            Log.d("contact", "call");
            removeContact();
        } else if(v.getId() == R.id.waitingContactLayout) {
            initContact();
            waitingContactLayout.setOnClickListener(null);
        }
    }

    public void getCarsAndAddToDropdown(){

        final SelectedCarAdapter mCarAdapter = new SelectedCarAdapter(mContext,new ArrayList<Car>());
        carDropDown = (RecyclerView) findViewById(R.id.confirmingCarDropdown);
        carDropDown.setAdapter(mCarAdapter);
        GridLayoutManager mLayoutManager = new GridLayoutManager(mContext, 1);
        carDropDown.setLayoutManager(mLayoutManager);
        carDropDown.addItemDecoration(new SpacesItemDecoration(8));
        /*
        carDropDown.addOnItemTouchListener( new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                Log.d("carSelector", "touched: " + position);
                Car pendingDeleteCar = mCarAdapter.getCar(position);
                view.setBackgroundResource(R.drawable.rounded_corner_blue_border);
            }
        }));
        */

        int userIDnum = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
        final ArrayList<Car> theCars = new ArrayList<Car>();
        //CarAdapter mCarAdapter = new CarAdapter(mContext,theCars);
        if (userIDnum >= 0) {
            mWashMyWhipEngine.getCars(userIDnum, new Callback<List<JSONObject>>() {
                @Override
                public void success(List<JSONObject> jsonObject, Response response) {

                    String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                    Log.d("getCars", " response: " + responseString);
                    if (responseString.equals("[]")) {
                        Log.d("getCars", " response is null ");
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("You don't have any cars!");
                        builder.setMessage("Please add a car to your profile before you request a wash!");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                initRequesting();
                                dialog.cancel();

                            }
                        });
                        builder.show();


                    } else {
                        carDropDown.setVisibility(View.VISIBLE);
                        //responseString = responseString.replace("[","{").replace("]","}");
                        // mSharedPreferences.edit().putString("carsString",responseString).apply();
                        JSONArray mArray = null;
                        try {
                            mArray = new JSONArray(responseString);
                            // JSONArray jsonArray = mArray.getJSONArray("Cars");
                            for (int i = 0; i < mArray.length(); i++) {
                                JSONObject car = mArray.getJSONObject(i);
                                Car newCar = new Car(car);
                                theCars.add(newCar);
                                Log.d("getCars", " car: " + car.toString());
                                //mSharedPreferences.edit().putString("car"+i,)
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mCars = theCars;
                        if (mCars != null) {
                            if (mCars.size() > 0) {
                                for (int i = 0; i < mCars.size(); i++) {
                                    mCarAdapter.add(mCars.get(i));
                                }
                            }
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("getCars", "error: " + error.toString());
                }
            });
        }
    }
    public void initRequesting() {

        userState = UserState.REQUESTING;
        int view = R.layout.requesting_layout;
        swapView(view);
        setLocationButton = (Button) findViewById(R.id.setLocationButton);
        setLocationButton.setTypeface(mFont);
        setLocationButton.setOnClickListener(this);

        addressText = (EditText) findViewById(R.id.addressText);
        addressText.setTypeface(mFont);
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

        TextView washType = (TextView)findViewById(R.id.washTypeRequesting);
        TextView washPrice = (TextView)findViewById(R.id.washPriceRequesting);
        washPrice.setTypeface(mFont);
        washType.setTypeface(mFont);

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
        requestWashButton.setTypeface(mFont);
        requestWashButton.setOnClickListener(this);


        confirmedAddress = (TextView) findViewById(R.id.confirmedAddress);
        confirmedAddress.setTypeface(mFont);
        confirmedAddress.setText(addressText.getText());


        getCarsAndAddToDropdown();

    }
    public void initQueued(){
        Log.d(TAG, "Wash Requested");
        hasBeenRequested = true;

        //server stuff
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
        hasBeenRequested = true;
        int view = R.layout.waiting_layout;
        swapView(view);
        userState = UserState.WAITING;
        cancelButton = (TextView) findViewById(R.id.cancelToolbarButton);
        cancelButton.setOnClickListener(this);
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setText("Cancel");


        String vendorLat = mSharedPreferences.getString("vendorLat","0");
        String vendorLong = mSharedPreferences.getString("vendorLong","0");
        LatLng vendorLocation = new LatLng(Double.parseDouble(vendorLat),Double.parseDouble(vendorLong));

        if (currentLocation != null) {
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            start = mMap.addMarker(new MarkerOptions()
                    .position(mMarker.getPosition())
                    .draggable(false).visible(false));
            end = mMap.addMarker(new MarkerOptions()
                    .position(vendorLocation)
                    .draggable(false).visible(true));
            Marker[] markers = {start, end};
            for (Marker m : markers) {
                b.include(m.getPosition());
            }
            LatLngBounds bounds = b.build();
            //Change the padding as per needed
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 244, 244, 0);
            mMap.animateCamera(cu);
        }
        waitingContactLayout = (RelativeLayout)findViewById(R.id.waitingContactLayout);
        waitingContactLayout.setOnClickListener(this);
        String vendorName = mSharedPreferences.getString("vendorUsername","Vendor Username");
        TextView vendorUsername = (TextView) findViewById(R.id.waitingVendorName);
        vendorUsername.setTypeface(mFont);
        vendorUsername.setText(vendorName);

        int vendorID = mSharedPreferences.getInt("vendorID", -1);
        vendorWaitingImage = (CircleImageView) findViewById(R.id.waitingPicture);
        if (vendorID > 0) {
            Picasso.with(this)
                    .load("http://www.WashMyWhip.us/wmwapp/VendorAvatarImages/vendor" + vendorID + "avatar.jpg")
                    .resize(100, 100)
                    .centerCrop()
                    .into(vendorWaitingImage);
        }
    }
    public void initArrived() {
        hasBeenRequested = true;
        userState = UserState.ARRIVED;
        int view = R.layout.arrived_layout;
        swapView(view);
        cancelButton = (TextView) findViewById(R.id.cancelToolbarButton);
        cancelButton.setOnClickListener(this);
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setText("Cancel");

        contactButtonArrived = (Button) findViewById(R.id.arrivedContact);
        contactButtonArrived.setTypeface(mFont);
        contactButtonArrived.setOnClickListener(this);

        TextView arrivedVendorUsername = (TextView) findViewById(R.id.arrivedVendorUsername);
        TextView arrivedWashCost = (TextView) findViewById(R.id.arrivedWashCost);
        TextView arrivedWashType = (TextView) findViewById(R.id.arrivedWashType);
        TextView hasArrived = (TextView) findViewById(R.id.hasArrived);
        hasArrived.setTypeface(mFont);
        arrivedVendorUsername.setTypeface(mFont);
        arrivedWashCost.setTypeface(mFont);
        arrivedWashType.setTypeface(mFont);
        String vendorName = mSharedPreferences.getString("vendorUsername","Vendor Username");
        arrivedVendorUsername.setText(vendorName);


        int vendorID = mSharedPreferences.getInt("vendorID", -1);
        arrivedVendorImage = (CircleImageView) findViewById(R.id.arrivedVendorImage);
        if (vendorID > 0) {
            Picasso.with(this)
                    .load("http://www.WashMyWhip.us/wmwapp/VendorAvatarImages/vendor" + vendorID + "avatar.jpg")
                    .resize(100, 100)
                    .centerCrop()
                    .into(arrivedVendorImage);
        }

    }
    public void initWashing() {
        hasBeenRequested = true;
        //can no longer cancel
        int view = R.layout.washing_layout;
        userState = UserState.WASHING;
        swapView(view);

        String vendorName = mSharedPreferences.getString("vendorUsername","Vendor Username");
        TextView vendorUserName = (TextView) findViewById(R.id.washingVendorName);
        vendorUserName.setTypeface(mFont);
        vendorUserName.setText(vendorName);


        cancelButton = (TextView) findViewById(R.id.cancelToolbarButton);
        cancelButton.setOnClickListener(null);
        cancelButton.setVisibility(View.INVISIBLE);

        contactButtonWashing = (Button) findViewById(R.id.washingContact);
        contactButtonWashing.setTypeface(mFont);
        contactButtonWashing.setOnClickListener(this);
    }
    public void initFinalizing(){
        hasBeenRequested = true;
        int view = R.layout.finalizing_layout;
        swapView(view);
        userState = UserState.FINALIZING;
        cancelButton = (TextView) findViewById(R.id.cancelToolbarButton);
        cancelButton.setOnClickListener(null);
        cancelButton.setVisibility(View.INVISIBLE);

        finalizingSubmit = (Button) findViewById(R.id.finalizingSubmitButton);
        finalizingSubmit.setOnClickListener(this);
        finalizingSubmit.setTypeface(mFont);

        TextView howWouldYouRate = (TextView) findViewById(R.id.howRate);
        howWouldYouRate.setTypeface(mFont);

        String vendorName = mSharedPreferences.getString("vendorUsername","Vendor Username");
        TextView vendorUsername = (TextView)findViewById(R.id.finalizingVendorName);
        vendorUsername.setTypeface(mFont);
        vendorUsername.setText(vendorName);

        ratingBar = (RatingBar) findViewById(R.id.finalizingRating);
        finalizingComments = (EditText) findViewById(R.id.finalizingComments);
        int vendorID = mSharedPreferences.getInt("vendorID", -1);
        finalizingVendorImage = (CircleImageView) findViewById(R.id.finalizingVendorImage);
        if (vendorID > 0) {
            Picasso.with(this)
                    .load("http://www.WashMyWhip.us/wmwapp/VendorAvatarImages/vendor" + vendorID + "avatar.jpg")
                    .resize(100, 100)
                    .centerCrop()
                    .into(finalizingVendorImage);
        }


    }

    private void initContact() {
        int view = R.layout.contact_layout;
        ViewGroup parent = (ViewGroup) currentView.getParent();
        contactView = getLayoutInflater().inflate(view, parent, false);
        parent.addView(contactView);
        textContact = (TextView) findViewById(R.id.contactText);
        textContact.setTypeface(mFont);
        textContact.setOnClickListener(this);
        callContact = (TextView) findViewById(R.id.contactCall);
        callContact.setTypeface(mFont);
        callContact.setOnClickListener(this);
        doneContact = (TextView) findViewById(R.id.contactDone);
        doneContact.setTypeface(mFont);
        doneContact.setOnClickListener(this);




    }

    public void removeContact() {
        //int view = R.layout.contact_layout;
        ViewGroup parent = (ViewGroup) currentView.getParent();
        // int index = parent.indexOfChild(contactView);
        parent.removeView(contactView);


        if(contactButtonArrived!=null){
            contactButtonArrived.setOnClickListener(this);
        }
        if(contactButtonWashing!=null){
            contactButtonWashing.setOnClickListener(this);
        }
        if(waitingContactLayout!=null) {
            waitingContactLayout.setOnClickListener(this);
        }
    }

    public void contactCallVendor() {
        String userNumber = mSharedPreferences.getString("vendorPhone", "null");
        if(!userNumber.equals("null")) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + userNumber));
            startActivity(intent);
        }


    }
    public void contactTextVendor(){
        String userNumber = mSharedPreferences.getString("vendorPhone", "null");
        if(!userNumber.equals("null")){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", userNumber, null)));
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

        if(addressText!=null &&addressText.hasFocus()){
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
        if(userState == UserState.REQUESTING || userState == UserState.CONFIRMING){
            mSharedPreferences.edit().clear().commit();
            Intent i = new Intent(this,LoginActivity.class);
            startActivity(i);
            finish();
        }
        else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error logging out");
            builder.setMessage("You cannot log out while you are requesting a wash. Finish or cancel your wash before you log out!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
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

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                dialog.cancel();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
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
    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
