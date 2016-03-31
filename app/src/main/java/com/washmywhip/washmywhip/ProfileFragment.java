package com.washmywhip.washmywhip;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.KeyListener;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    SharedPreferences mSharedPreferences;

    String first, last, email, phone;



    @InjectView(R.id.pictureProfile)
    CircleImageView profilePicture;
    @InjectView(R.id.firstNameProfile)
    EditText firstNameEditText;
    @InjectView(R.id.lastNameProfile)
    EditText lastNameEditText;
    @InjectView(R.id.emailProfile)
    EditText emailEditText;
    @InjectView(R.id.phoneProfile)
    EditText phoneEditText;


   // @InjectView(R.id.cancelToolbarButton)
    TextView editButton;

    @InjectView(R.id.addCar)
    RelativeLayout addCar;

    KeyListener defaultKeyListener;

    private ArrayList<Car> mCars;

    private GridLayoutManager mLayoutManager;
    private CarAdapter mCarAdapter;
    private WashMyWhipEngine mWashMyWhipEngine;


    @InjectView(R.id.carGridView)
    RecyclerView mView;
    private String encodedProfile;

    @InjectView(R.id.addCarProfile)
    TextView cars;
    @InjectView(R.id.carsProfile)
    TextView addcar;
    @InjectView(R.id.accountProfile)
    TextView account;
    private Typeface mFont;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstance(ArrayList<Car> cars) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("cars",cars);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
           // mCars = (ArrayList<Car>) getArguments().getSerializable("cars");
        }
        //get car list from server
    }

    public void initEditable(){

        editButton.setText("Save");
        firstNameEditText.setKeyListener(defaultKeyListener);
        firstNameEditText.setEnabled(true);
        lastNameEditText.setKeyListener(defaultKeyListener);
        lastNameEditText.setEnabled(true);
        emailEditText.setKeyListener(defaultKeyListener);
        emailEditText.setEnabled(true);
        phoneEditText.setKeyListener(defaultKeyListener);
        phoneEditText.setEnabled(true);
        profilePicture.setOnClickListener(this);
        EditText[] fields = {firstNameEditText,lastNameEditText,emailEditText,phoneEditText};

        for(EditText field:fields){
            if(field.hasFocus()){
                hideKeyboard(field);
            }
        }



    }

    public void initNotEditable() {

        editButton.setText("Edit");
        firstNameEditText.setActivated(false);
        firstNameEditText.setKeyListener(null);
        firstNameEditText.setEnabled(false);
        lastNameEditText.setKeyListener(null);
        lastNameEditText.setEnabled(false);
        emailEditText.setKeyListener(null);
        emailEditText.setEnabled(false);
        phoneEditText.setKeyListener(null);
        phoneEditText.setEnabled(false);

        first = firstNameEditText.getText().toString();
        last = lastNameEditText.getText().toString();
        email = emailEditText.getText().toString();
        phone = phoneEditText.getText().toString();

        Log.d("updateUser", first + " " + last + " " + " " + email + " " + phone);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Hold on!");
        builder.setMessage("You are requesting to update your profile information. Is all the information correct?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int userid = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
                Log.d("updateUser", "id: " + userid);
                mWashMyWhipEngine.updateUserInfo(userid, email, first, last, phone, new Callback<Object>() {
                    @Override
                    public void success(Object o, Response response) {
                        Log.d("updateUser", "success " + o.toString());
                        mSharedPreferences.edit().putString("FirstName", first).commit();
                        mSharedPreferences.edit().putString("LastName", last).commit();
                        mSharedPreferences.edit().putString("Email", email).commit();
                        mSharedPreferences.edit().putString("Phone", phone).commit();


                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("updateUser", "failz " + error.toString());
                    }
                });
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.inject(this, v);
        mFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Archive.otf");
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        mWashMyWhipEngine = new WashMyWhipEngine();
        editButton = (TextView) getActivity().findViewById(R.id.cancelToolbarButton);
        editButton.setOnClickListener(this);

        profilePicture.setOnClickListener(null);
        addCar.setOnClickListener(this);

        addcar.setTypeface(mFont);
        cars.setTypeface(mFont);
        account.setTypeface(mFont);

        mLayoutManager = new GridLayoutManager(getActivity(), 1);
        mView.setLayoutManager(mLayoutManager);
        mCarAdapter = new CarAdapter(getActivity(),new ArrayList<Car>());
        mView.setAdapter(mCarAdapter);
        mView.addItemDecoration(new SpacesItemDecoration(8));
        mView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        // do whatever
                        Log.d("mView", "touched: " + position);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Hold on!");
                        builder.setMessage("Are you sure you want to delete this car?");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Car pendingDeleteCar = mCarAdapter.getCar(position);
                                // String s = mSharedPreferences.getString("carsString","");
                                Log.d("DELETE", "id: " + pendingDeleteCar.getCarID());
                                mCarAdapter.remove(position);
                                //now remove from server
                                mWashMyWhipEngine.deleteCar(pendingDeleteCar.getCarID(), new Callback<Object>() {
                                    @Override
                                    public void success(Object s, Response response) {
                                        // String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                                        Log.d("DELETEcar", "success: " + s.toString());
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        Log.d("DELETEcar", "failz: " + error.toString());
                                    }
                                });

                                dialog.cancel();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                })
        );
        int userID = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
        if(userID>=0){
            Picasso.with(getActivity())
                    .load("http://www.WashMyWhip.us/wmwapp/ClientAvatarImages/client" + userID+ "avatar.jpg")
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .resize(100, 100)
                    .centerCrop()
                    .into(profilePicture);
        }
        addCarsToView();

        /*
        int userid = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
        mWashMyWhipEngine.getUserWithID(userid, new Callback<Object>() {
            @Override
            public void success(Object object, Response response) {
                String s = object.toString();
                Log.d("getUser", s);

                String[] info = s.split(",");


                first =info[2].replace(" FirstName=","");
                last = info[3].replace(" LastName=","");;
                email= info[4].replace(" Email=","");;
                phone= info[5].replace(" Phone=","");;
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("getUser", error.toString());
            }
        });
        */


        //should be getting info from server, not shared prefs
        first = mSharedPreferences.getString("FirstName", "");
        last = mSharedPreferences.getString("LastName","");
        email = mSharedPreferences.getString("Email","");
        phone = mSharedPreferences.getString("Phone","");

        firstNameEditText.setText(first);
        lastNameEditText.setText(last);
        emailEditText.setText(email);
        phoneEditText.setText(phone);
        firstNameEditText.setTypeface(mFont);
        lastNameEditText.setTypeface(mFont);
        emailEditText.setTypeface(mFont);
        phoneEditText.setTypeface(mFont);

        defaultKeyListener = firstNameEditText.getKeyListener();

        editButton.setText("Edit");
        firstNameEditText.setActivated(false);
        firstNameEditText.setKeyListener(null);
        firstNameEditText.setEnabled(false);
        lastNameEditText.setKeyListener(null);
        lastNameEditText.setEnabled(false);
        emailEditText.setKeyListener(null);
        emailEditText.setEnabled(false);
        phoneEditText.setKeyListener(null);
        phoneEditText.setEnabled(false);


        return v;
    }

    public void addCarsToView(){
        int userIDnum = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
        final ArrayList<Car> theCars = new ArrayList<Car>();
        if (userIDnum >= 0) {
            mWashMyWhipEngine.getCars(userIDnum, new Callback<List<JSONObject>>() {
                @Override
                public void success(List<JSONObject> jsonObject, Response response) {

                    String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
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
                    if(mCars!=null){
                        if(mCars.size()>0){
                            for(int i = 0; i< mCars.size();i++){
                                mCarAdapter.add(mCars.get(i));
                            }
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("getCars","error: "+ error.toString());
                }
            });
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.d("PROFILE", "Detatching");
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == editButton.getId()) {
            if(editButton.getText().toString().equals("Save")){
                Log.d("FAPMENU TEXTVIEW", "SAVE CLICK");
                initNotEditable();

            } else if (editButton.getText().toString().equals("Edit")){
                Log.d("FAPMENU TEXTVIEW", "EDIT CLICK");
                initEditable();
            }
        } else if(v.getId() == addCar.getId()){
            Log.d("PROFILE", "ADD CAR");


            Fragment addCarFragment = AddCarFragment.newInstance();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.contentFrame, addCarFragment).commit();
        }  else if(v.getId()==profilePicture.getId()){
            selectImage();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(data!=null) {
            Log.d("BLAHBLAH", "requestCode: " + requestCode + " ResultCode: " + requestCode + " Data: " + data.getDataString());
            super.onActivityResult(requestCode, resultCode, data);


            Uri photoUri = data.getData();
            String selectedImagePath = null;
            Log.d("photoResult", "uri: " + photoUri.toString());


            Cursor cursor = getActivity().getContentResolver().query(
                    photoUri, null, null, null, null);
            if (cursor == null) {
                selectedImagePath = photoUri.getPath();
                Log.d("photoResult", "(null)path: " + selectedImagePath);
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                selectedImagePath = cursor.getString(idx);
                Log.d("photoResult", "path: " + selectedImagePath);
            }

            Bitmap selectedImage = null;
            byte[] byteArray = null;
            try {
                selectedImage =Bitmap.createBitmap(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 60, stream);

                byteArray = stream.toByteArray();
                encodedProfile = Base64.encodeToString(byteArray, Base64.DEFAULT);
                profilePicture.setImageBitmap(selectedImage);
                int userid = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
                mWashMyWhipEngine.uploadClientAvatarImageAndroid(userid, encodedProfile, new Callback<Object>() {
                    @Override
                    public void success(Object s, Response response) {
                        Log.d("vendorAvatarUpload", "Success " + s.toString());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        //  String json =  new String(((TypedByteArray)error.getResponse().getBody()).getBytes());

                        Log.d("vendorAvatarUpload", "error: " + error.getMessage());
                    }
                });


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //0 is request code
                    startActivityForResult(intent, 0);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");

                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"), 1);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void showKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
