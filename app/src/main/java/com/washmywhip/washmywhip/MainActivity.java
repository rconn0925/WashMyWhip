package com.washmywhip.washmywhip;



import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity  implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private GoogleMap mMap;
    private Marker mMarker;
    private Context mContext;
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLongitude(),location.getLatitude());

            IconGenerator factory = new IconGenerator(getApplicationContext());
            Bitmap icon = factory.makeIcon("Set Location");

           // mMarker = mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(icon)));

            if(mMap!=null){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            }
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
                if(addressList.size()>0){
                    String address = addressList.get(0).getAddressLine(0);
                    String city = addressList.get(0).getAddressLine(1);
                    String country = addressList.get(0).getAddressLine(2);
                    mAddress = address + " " + city + ", " + country;
                    addressText.setText(mAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @InjectView(R.id.setLocationButton)
    Button setLocationButton;

    Button requestWashButton;

    @InjectView(R.id.pin)
    ImageView pin;

    @InjectView(R.id.triangle)
    ImageView triangle;

    @InjectView(R.id.addressText)
    EditText addressText;

    @InjectView(R.id.inactiveLayout)
    RelativeLayout inactiveLayout;

    RelativeLayout confirmingRequestLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setLocationButton.setOnClickListener(this);
        //addressText.setText(getMyLocationAddress());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mMap.setOnCameraChangeListener(myCameraChangeListener);
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

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.setLocationButton){
            if(mMarker!= null){
                mMarker.remove();
            }

            LatLng newLocation = mMap.getCameraPosition().target;
            mMarker = mMap.addMarker(new MarkerOptions().position(newLocation).visible(false));


            addressText.setText(getMyLocationAddress());


            View currentView = inactiveLayout;
            ViewGroup parent = (ViewGroup) inactiveLayout.getParent();
            int index = parent.indexOfChild(currentView);
            parent.removeView(currentView);
            int view = R.layout.confirming_request_layout;
            currentView = getLayoutInflater().inflate(view, parent, false);
            parent.addView(currentView, index);

            requestWashButton = (Button) findViewById(R.id.requestWashButton);
            requestWashButton.setOnClickListener(this);

        }
        if(v.getId()==R.id.requestWashButton){
            Log.d(TAG, "Wash Requested");
        }
    }
}
