package com.washmywhip.washmywhip;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
public class ProfileFragment extends Fragment {
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

    @InjectView(R.id.firstNameProfile)
    EditText firstNameEditText;
    @InjectView(R.id.lastNameProfile)
    EditText lastNameEditText;
    @InjectView(R.id.emailProfile)
    EditText emailEditText;
    @InjectView(R.id.phoneProfile)
    EditText phoneEditText;


    @InjectView(R.id.cancelToolbarButton)
    TextView editButton;



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
        firstNameEditText.setKeyListener(defaultKeyListener);
        firstNameEditText.setEnabled(true);
        lastNameEditText.setKeyListener(defaultKeyListener);
        lastNameEditText.setEnabled(true);
        emailEditText.setKeyListener(defaultKeyListener);
        emailEditText.setEnabled(true);
        phoneEditText.setKeyListener(defaultKeyListener);
        phoneEditText.setEnabled(true);
    }

    public void initNotEditable() {

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
