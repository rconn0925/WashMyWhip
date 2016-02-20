package com.washmywhip.washmywhip;

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devmarvel.creditcardentry.library.CreditCard;
import com.devmarvel.creditcardentry.library.CreditCardForm;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddPaymentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddPaymentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddPaymentFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    private Button saveButton;
    private Button cancelButton;
    private LinearLayout linearLayout;
    private CreditCardForm form;
    private SharedPreferences mSharedPreferences;
    private WashMyWhipEngine mWashMyWhipEngine;

    private OnFragmentInteractionListener mListener;

    public AddPaymentFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AddPaymentFragment newInstance() {
        AddPaymentFragment fragment = new AddPaymentFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_payment, container, false);
        form = new CreditCardForm(getActivity());
        linearLayout = (LinearLayout)v.findViewById(R.id.paymentFrag);
        linearLayout.addView(form);
        saveButton = (Button)v.findViewById(R.id.addPayment);
        cancelButton = (Button) v.findViewById(R.id.cancelPayment);
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
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
        if(v.getId() == saveButton.getId()){

            //validate
            //send to server
            Log.d("addPayment", ""+ form.toString());
            if(form.isCreditCardValid())
            {
                CreditCard card = form.getCreditCard();

                //Pass credit card to service
                Log.d("addPayment", ""+ card.getCardNumber());
            }
            else
            {
                //Alert Credit card invalid
                Log.d("addPayment", "invalid card");
            }
        } else if (v.getId() == cancelButton.getId()){

            Fragment paymentFragment = PaymentFragment.newInstance();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.contentFrame, paymentFragment).commit();
        }
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
