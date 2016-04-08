package com.washmywhip.washmywhip;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;


public class AddCarFragment extends Fragment implements View.OnClickListener{

    private static final String COLOR = "color";
    private static final String MODEL = "model";
    private static final String MAKE = "make";
    private static final String PLATE = "plate";
    private static final String PIC = "pic";

    private String carMake;
    private String carModel;
    private String carColor;
    private String carPlate;
    private String carPic;

    private int index = -1;
    private int camera_request = 0;
    private int storage_request = 0;


    @InjectView(R.id.addCarPicture)
    ImageView carImage;
    @InjectView(R.id.addCarColor)
    EditText colorEditText;
    @InjectView(R.id.addCarMake)
    Spinner makeDropdown;
    @InjectView(R.id.addCarModel)
    Spinner modelDropdown;
    @InjectView(R.id.addCarPlate)
    EditText plateEditText;
    @InjectView(R.id.saveCar)
    Button saveButton;

    @InjectView(R.id.addCarFragment)
    RelativeLayout background;

    TextView menuTextEdit;
    String encodedCarImage;

    Map<String,String[]> carData = new HashMap<>();
    ArrayList<Car> mCars;

    private SharedPreferences mSharedPreferences;
    private WashMyWhipEngine mWashMyWhipEngine;

    private OnFragmentInteractionListener mListener;

    public AddCarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddCarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment newInstance() {
        AddCarFragment fragment = new AddCarFragment();
        return fragment;
    }


    //used to update existing car?
    public static Fragment newInstance(String make,String model,String color,String plate, int pic) {
        AddCarFragment fragment = new AddCarFragment();
        Bundle args = new Bundle();
        args.putString(MAKE, make);
        args.putString(MODEL, model);
        args.putString(COLOR, color);
        args.putString(PLATE, plate);
        args.putInt(PIC, pic);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            carMake = getArguments().getString(MAKE);
            carModel = getArguments().getString(MODEL);
            carColor = getArguments().getString(COLOR);
            carPlate = getArguments().getString(PLATE);
            carPic = getArguments().getString(PIC);
        }
        parseCarXML();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        mWashMyWhipEngine = new WashMyWhipEngine();
        mCars = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        menuTextEdit = (TextView) getActivity().findViewById(R.id.cancelToolbarButton);
        menuTextEdit.setText("Cancel");
        menuTextEdit.setOnClickListener(this);
      //  background = (RelativeLayout) getActivity().findViewById(R.id.profileFragment);

        View v = inflater.inflate(R.layout.fragment_add_car, container, false);
        ButterKnife.inject(this, v);
        initMakeList();
        saveButton.setOnClickListener(null);
        carImage.setOnClickListener(this);
        background.setOnClickListener(this);

        return v;
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
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == saveButton.getId()) {
            //Pop up succuss or failure

            //validate input info

            saveButton.setOnClickListener(null);
            carColor = colorEditText.getText().toString();
            carPlate = plateEditText.getText().toString();
            hideKeyboard(colorEditText);
            hideKeyboard(plateEditText);

            if(carColor ==null||carPlate ==null||carModel==null||carMake==null
                    || carColor.equals("")||carPlate.equals("")||carModel.equals("")
                    ||carMake.equals("")){

                //ALERT FAILURE
                Log.d("addCar","add car failure... need to enter all the info");
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Error adding car");
                builder.setMessage("Please enter all the info and try again!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            } else {

                String userID =  mSharedPreferences.getString("UserID", null);
                //get cars to find car number
                if(userID!=null){
                    final int userIDnum = Integer.parseInt(userID);

                    mWashMyWhipEngine.createCar(userIDnum, carColor, carMake, carModel, carPlate, encodedCarImage, new Callback<String>() {
                        @Override
                        public void success(String jsonObject, Response response) {
                            String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                            Log.d("CREATEcar",responseString);

                            EditText[] fields = {plateEditText,colorEditText};
                            for(EditText field:fields){
                                if(field.hasFocus()){
                                    hideKeyboard(field);
                                }
                            }

                            Fragment profileFragment = ProfileFragment.newInstance();
                            FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.contentFrame, profileFragment).commit();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.d("CREATEcar",error.toString());
                            saveButton.setOnClickListener(AddCarFragment.this);
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Error adding car");
                            builder.setMessage(error.toString());
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.show();

                        }
                    });
                    //Car createdCar = new Car();
                    //submit car data to server, and save into shared preferences

                }
            }

        } else if (v.getId() == carImage.getId()){
            selectImage();
        } else if(v.getId() == menuTextEdit.getId()){
            Fragment profileFragment = ProfileFragment.newInstance();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.contentFrame, profileFragment).commit();
        } else if (v.getId() == background.getId()){
            if(colorEditText.hasFocus()){
                hideKeyboard(colorEditText);
            }
            if(plateEditText.hasFocus()){
                hideKeyboard(plateEditText);
            }
        }
    }

    public ArrayList<Car> readCarJSON(String s){
        ArrayList<Car> list = new ArrayList<>();

        String [] elements = s.split("'}'");
        Log.d("CARSnum","num cars: " + elements.length);

        return list;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(data!=null) {
            Log.d("BLAHBLAH", "requestCode: " + requestCode + " ResultCode: " + requestCode + " Data: " + data.getDataString());
            super.onActivityResult(requestCode, resultCode, data);

            Uri photoUri = data.getData();
            String selectedImagePath = null;
          //  Log.d("photoResult", "uri: " + photoUri.toString());


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
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, stream);

                byteArray = stream.toByteArray();
                encodedCarImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                Log.d("encodedCarImage","encodedImage: "+ encodedCarImage);
                carImage.setImageBitmap(selectedImage);

                saveButton.setOnClickListener(this);
                saveButton.setBackgroundResource(R.drawable.rounded_corner_blue);


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
                    startActivityForResult(intent, camera_request);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    //Bundle data = intent.getExtras();
                    //data.get

                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"), storage_request);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public static Bitmap getRoundedCornerImage(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 100;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    public void parseCarXML(){
        //Get Document Builder
        AssetManager assetManager = getActivity().getAssets();
        InputStream instream = null;
        try {
            instream = assetManager.open("cars.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        //Build Document
        Document document = null;
        if(instream!=null){
            try {
                document = builder.parse(instream);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Normalize the XML Structure; It's just too important !!
            document.getDocumentElement().normalize();

            //Get all employees
            NodeList mModelList = document.getElementsByTagName("carmodellist");
            NodeList mCarList = document.getElementsByTagName("carname");
            for(int i = 0; i < mModelList.getLength();i++){
                String info = mModelList.item(i).getTextContent();
                String key = mCarList.item(i).getTextContent();

                String[] elems = info.split("\n");
                for(int k = 0; k<elems.length;k++){
                    elems[k]= elems[k].trim();
                    carData.put(key,elems);
                }
            }
        } else {
            Log.d("lolz", "inputstream is null");
        }

    }


    public void initMakeList(){
        String[] elems = carData.keySet().toArray(new String[carData.size()]);
        Arrays.sort(elems);
        final String[] items = elems;
        //List<String> list = new LinkedList<String>(Arrays.asList(items));
      //  list.add(0, "Select Make");
       // items = list.toArray(new String[list.size()]);
       // makeDropdown.setPrompt("Select Make");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, items);
        makeDropdown.setAdapter(new NothingSelectedSpinnerAdapter(adapter, R.layout.car_make, getActivity()));
        makeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Log.d("item", (String) parent.getItemAtPosition(position));
                index = position;
                if (index > 0) {
                    carMake = items[position - 1];
                    Log.d("addCar", "carMake: " + carMake);
                }
                initModelList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void initModelList(){
        if(index>0){
            String [] keySet = carData.keySet().toArray(new String[carData.size()]);
            Arrays.sort(keySet);
            final String[] items = carData.get(keySet[index-1]);
            final String[] newItems = Arrays.copyOfRange(items, 1, items.length - 1);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newItems);
            modelDropdown.setAdapter(new NothingSelectedSpinnerAdapter(adapter, R.layout.car_model,getActivity()));
            modelDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(position>0) {
                        carModel = items[position];
                        Log.d("addCar", "careModel: " + carModel);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } else if (index == 0){
            ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),R.layout.car_model,new String[]{"Please select a make first"});
            modelDropdown.setAdapter(new NothingSelectedSpinnerAdapter(adapter, R.layout.car_model,getActivity()));
            modelDropdown.setOnItemSelectedListener(null);
            Log.d("addCar","this is gheto");
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void showKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
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
}
