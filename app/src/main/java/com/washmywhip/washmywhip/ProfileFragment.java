package com.washmywhip.washmywhip;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


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
    ImageView profilePicture;
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


    private GridLayoutManager mLayoutManager;
    private CarAdapter mCarAdapter;
    @InjectView(R.id.carGridView)
    RecyclerView mView;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


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
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.inject(this,v);

        editButton = (TextView) getActivity().findViewById(R.id.cancelToolbarButton);
        editButton.setOnClickListener(this);

        profilePicture.setOnClickListener(this);
        addCar.setOnClickListener(this);

        mLayoutManager = new GridLayoutManager(getActivity(), 1);
        mView.setLayoutManager(mLayoutManager);
        mCarAdapter = new CarAdapter(getActivity(),new ArrayList<Car>());
        mView.setAdapter(mCarAdapter);

        mCarAdapter.add(new Car(1, 2, "TestColor", "TestMake", "TestModel", "TestPlate", R.drawable.carsample));
        mCarAdapter.add(new Car(1, 2, "TestColor", "TestMake", "TestModel", "TestPlate", R.drawable.carsample));
        mCarAdapter.add(new Car(1, 2, "TestColor", "TestMake", "TestModel", "TestPlate", R.drawable.carsample));
        mCarAdapter.add(new Car(1, 2, "TestColor", "TestMake", "TestModel", "TestPlate", R.drawable.carsample));
        mCarAdapter.add(new Car(1, 2, "TestColor", "TestMake", "TestModel", "TestPlate", R.drawable.carsample));
        mCarAdapter.add(new Car(1, 2, "TestColor", "TestMake", "TestModel", "TestPlate", R.drawable.carsample));

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        first = mSharedPreferences.getString("FirstName", "");
        last = mSharedPreferences.getString("LastName","");
        email = mSharedPreferences.getString("Email","");
        phone = mSharedPreferences.getString("Phone","");

        firstNameEditText.setText(first);
        lastNameEditText.setText(last);
        emailEditText.setText(email);
        phoneEditText.setText(phone);


        defaultKeyListener = firstNameEditText.getKeyListener();



        initNotEditable();

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
