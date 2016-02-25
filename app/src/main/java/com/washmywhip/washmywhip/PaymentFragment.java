package com.washmywhip.washmywhip;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.app.AlertDialog;

import com.devmarvel.creditcardentry.library.CreditCard;
import com.devmarvel.creditcardentry.library.CreditCardForm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PaymentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PaymentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PaymentFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private WashMyWhipEngine mWashMyWhipEngine;
    private SharedPreferences mSharedPreferences;
    @InjectView(R.id.cardGridView)
    RecyclerView mView;
  //  @InjectView(R.id.paymentList)
  //  ListView paymentList;
    @InjectView(R.id.addPaymentButton)
    Button addPaymentButton;
    String [] list;
    private int userID;
    Map<String, Object> paymentData;
    Map<String, Object> cardData;
    ArrayList<Card> mCards;
    String defaultCard;
    private GridLayoutManager mLayoutManager;
    private CardAdapter mCardAdapter;

    private CreditCardForm form;





    private OnFragmentInteractionListener mListener;

    public PaymentFragment() {
        // Required empty public constructor
    }


    public static PaymentFragment newInstance() {
        PaymentFragment fragment = new PaymentFragment();
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
        defaultCard = null;
        mWashMyWhipEngine = new WashMyWhipEngine();
        mCards = new ArrayList<>();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        userID = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
    }

    public void getCards(){
        userID = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
        final ArrayList<Card> theCards = new ArrayList<>();
        if(userID>=0){

            mWashMyWhipEngine.getStripeCustomer(userID, new Callback<JSONObject>() {
                @Override
                public void success(JSONObject o, Response response) {
                    String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                    JSONObject json = null;
                    try {
                        json = new JSONObject(responseString);
                        paymentData = jsonToMap(json);
                      //  Log.d("getCards",""+json.toString());
                        Log.d("getCards","default source: "+json.getString("default_source"));
                        defaultCard = json.getString("default_source");
                        mSharedPreferences.edit().putString("default_source",defaultCard).apply();
                        String data = json.getString("sources");
                        JSONObject cards = new JSONObject(data);
                        Log.d("getCards",json.toString());
                        if(cards.get("object").equals("list")){
                            Log.d("getCards","multiple");
                            //multiple cards
                            String cardString = cards.getString("data");
                            Log.d("getCards",cardString);
                            JSONArray jsonArray = new JSONArray(cardString);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject card = jsonArray.getJSONObject(i);

                                String expMonth = card.getString("exp_month");
                                String expYear  = card.getString("exp_year");
                                String lastFour  = card.getString("last4");
                                String cardType = card.getString("brand");
                                String exYearShort = expYear.substring(expYear.length() - 2);
                                String expirationDate = expMonth + "/" + exYearShort;
                                String id = card.getString("id");
                                Card newCard;
                                if(id.equals(defaultCard)){
                                    newCard = new Card(id,cardType,lastFour,expirationDate,true);
                                } else {
                                    newCard = new Card(id,cardType,lastFour,expirationDate,false);
                                }
                                theCards.add(newCard);
                                Log.d("getCards", " card: " + card.toString());
                                //mSharedPreferences.edit().putString("card"+i,)
                            }
                            mCards = theCards;
                            int numCards = theCards.size();
                            list = new String[numCards];
                            for(int j = 0;j<numCards;j++){
                                list[j] = theCards.get(j).toString();
                                mCardAdapter.add(theCards.get(j));
                            }
                          //  paymentList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list));
                          //  mCardAdapter
                        } else {
                            //just one card
                            Log.d("getCards","one");
                            String card = cards.getString("data");
                            JSONObject theCard = new JSONObject(card);
                            String expMonth = theCard.getString("exp_month");
                            String expYear  = theCard.getString("exp_year");
                            String exYearShort = expYear.substring(expYear.length() - 2);
                            String expirationDate = expMonth + "/" + exYearShort;
                            String lastFour  = theCard.getString("last4");
                            String cardType = theCard.getString("brand");
                            String id = theCard.getString("id");
                            Card newCard;
                            if(id.equals(defaultCard)){
                                newCard = new Card(id,cardType,lastFour,expirationDate,true);
                            } else {
                                newCard = new Card(id,cardType,lastFour,expirationDate,false);
                            }
                            theCards.add(newCard);
                            mCards = theCards;
                            //mSharedPreferences.edit().putString("card"+i,)
                            list = new String[1];
                            list[0] = newCard.toString();
                            mCardAdapter.add(newCard);
                           // paymentList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //put data into hashmap

                    //default source = prefered card
                    //get brand
                    //get last 4
                    //get experation month/year
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("getCards", "failz: " + error.toString());
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_payment, container, false);
        ButterKnife.inject(this, v);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        userID = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
        mLayoutManager = new GridLayoutManager(getActivity(), 1);
        mView.setLayoutManager(mLayoutManager);
        mCardAdapter = new CardAdapter(getActivity(),new ArrayList<Card>());
        mView.setAdapter(mCardAdapter);
        mView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        // do whatever
                        Log.d("mView", "touched: " + position);
                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                        builder.setTitle("Hold on!");
                        builder.setMessage("Would you like to delete this card or make it your default payment method?");
                        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                final AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(getActivity());
                                confirmBuilder.setTitle("Hold on!");
                                confirmBuilder.setMessage("Are you sure you want to delete this card?");
                                confirmBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //SERVER STUFF HERE
                                        Card pendingDeleteCard = mCardAdapter.getCard(position);
                                        // String s = mSharedPreferences.getString("carsString","");
                                        Log.d("DELETE", "id: " + pendingDeleteCard.getId());
                                        mCardAdapter.remove(position);
                                        //now remove from server

                                        mWashMyWhipEngine.deleteStripeCard(userID, pendingDeleteCard.getId(), new Callback<Object>() {
                                            @Override
                                            public void success(Object s, Response response) {
                                                // String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                                                Log.d("DELETEcard", "success: " + s.toString());
                                            }

                                            @Override
                                            public void failure(RetrofitError error) {
                                                Log.d("DELETEcard", "failz: " + error.toString());
                                            }
                                        });

                                        dialog.cancel();
                                    }
                                });
                                confirmBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                confirmBuilder.show();
                                dialog.cancel();
                            }
                        });
                        builder.setPositiveButton("Default", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(getActivity());
                                confirmBuilder.setTitle("Hold on!");
                                confirmBuilder.setMessage("Are you sure you want to make this card your default payment method?");
                                confirmBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Card newDefaultCard = mCardAdapter.getCard(position);
                                        //SERVER STUFF HERE
/*

                                        // String s = mSharedPreferences.getString("carsString","");
                                        Log.d("DEFAULT", "id: " + newDefaultCard.getId());
                                        newDefaultCard.setActive(true);
                                        defaultCard = newDefaultCard.getId();
                                        mSharedPreferences.edit().putString("default_source",defaultCard).apply();
                                        mCardAdapter.remove(position);
                                        mCardAdapter.add(newDefaultCard);


                                        for (int i = 0; i < mCardAdapter.getItemCount(); i++) {
                                            if (mCardAdapter.getCard(i).isActive) {
                                                Card temp = mCardAdapter.getCard(i);
                                                temp.setActive(false);

                                                mCardAdapter.remove(i);
                                                mCardAdapter.add(temp);
                                            }
                                        }
                                        */
                                        //now remove from server
                                        mWashMyWhipEngine.changeDefaultStripeCard(userID, newDefaultCard.getId(), new Callback<Object>() {
                                            @Override
                                            public void success(Object s, Response response) {
                                                // String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                                                Log.d("DEFAULTcard", "success: " + s.toString());
                                                while(mCardAdapter.getItemCount()>0){
                                                    mCardAdapter.remove(0);
                                                }
                                                getCards();
                                            }

                                            @Override
                                            public void failure(RetrofitError error) {
                                                Log.d("DEFAULTcard", "failz: " + error.toString());
                                            }
                                        });

                                        dialog.cancel();
                                    }
                                });
                                confirmBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                confirmBuilder.show();
                                dialog.cancel();
                            }
                        });
                        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                })
        );
      //  mView.addOnItemTouchListener(null);
        getCards();
       // paymentList.setOnItemClickListener(this);
        addPaymentButton.setOnClickListener(this);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //set active card
        if (position == 0) {

        } else if(position ==1 ){

        }
        Log.d("PAYMENT", "PAYMENT OPTION SELECTED: "+position );
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == addPaymentButton.getId()){

            Fragment addPaymentFragment = AddPaymentFragment.newInstance();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.contentFrame, addPaymentFragment).commit();

            /*

            Log.d("addPayment","requested to add a card");
            final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            final View view = getActivity().getLayoutInflater().inflate(R.layout.add_payment_form, null);
            alertDialog.setView(view);
            final Button save = (Button)view.findViewById(R.id.addPayment);
            final Button cancel =  (Button)view.findViewById(R.id.cancelPayment);
            form = new CreditCardForm(getActivity());
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                        Log.d("addPayment","invalid card");
                    }
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                }
            });
            //input.setHint("**** **** **** ****");
           // input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(16)});
           // alertDialog.setContentView(R.layout.add_payment_layout);
            alertDialog.setTitle("Please enter your payment information");
            alertDialog.show();

          //  Log.d("addPayment",input.getText().toString());
        */
        }
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
