package com.washmywhip.washmywhip;

import com.squareup.okhttp.OkHttpClient;
import com.stripe.android.model.Token;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by Ross on 2/8/2016.
 */
public class WashMyWhipEngine {
    private WashMyWhipService mService;

    public WashMyWhipEngine(){
        OkHttpClient okhttpclient = new OkHttpClient();

        OkClient okclient = new OkClient(okhttpclient);

        RestAdapter.Builder builder =  new RestAdapter.Builder()
                .setEndpoint("http://www.WashMyWhip.us/wmwapp")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(okclient);

        RestAdapter adapter = builder.build();
        mService = adapter.create(WashMyWhipService.class);
    }

    public void requestUserLogin(String username, String password, Callback<JSONObject> callback) {
        mService.requestUserLogin(username,password, callback);
    }

    public void requestTemporaryPassword(String email,Callback<JSONObject> callback) {
        mService.requestTemporaryPassword(email, callback);
    }

    public void getUserWithID(int userID, Callback<Object> callback) {
        mService.getUserWithID(userID, callback);
    }

    public void createUser(String username, String password, String email,String phoneNumber, String firstName, String lastName,Callback<Integer> callback) {
        mService.createUser(username, password, email, phoneNumber, firstName, lastName, callback);
    }

    public void updateUserInfo(int userId, String email, String firstName, String lastName, String phoneNumber, Callback<Object> callback) {
        mService.updateUserInfo(userId, email, firstName, lastName, phoneNumber, callback);
    }

    public void updateUserPassword(String username, String password,Callback<JSONObject> callback) {
        mService.updateUserPassword(username, password, callback);
    }

    public void createCar(int userID, String color, String make,String model, String plate, boolean hasImage, Callback<String> callback) {
        mService.createCar(userID, color, make, model, plate, hasImage, callback);
    }
    public void updateCar(int userID, String color, String make,String model, String plate, boolean hasImage, Callback<JSONObject> callback) {
        mService.updateCar(userID, color, make, model, plate, hasImage, callback);
    }
    public void getCars(int userID, Callback<List<JSONObject>> callback) {
        mService.getCars(userID, callback);
    }
    public void deleteCar(int carID, Callback<Object> callback) {
        mService.deleteCar(carID, callback);
    }
    public void getStripeCustomer(int userID, Callback<JSONObject> callback) {
        mService.getStripeCustomer(userID, callback);
    }
    public void addPaymentSource(int userID,String tokenID, Callback<Object> callback) {
        mService.addPaymentSource(userID, tokenID, callback);
    }
    public void changeDefaultStripeCard(int userID, String defaultID, Callback<Object> callback) {
        mService.changeDefaultStripeCard(userID, defaultID, callback);
    }
    public void deleteStripeCard(int userID, String cardID,Callback<Object> callback) {
        mService.deleteStripeCard(userID, cardID, callback);
    }
    public void rateVendor(int transactionID,int rating,String comments,Callback<String> callback) {
        mService.rateVendor(transactionID, rating, comments, callback);
    }
    public void getVendorWithID(int vendorID, Callback<JSONObject> callback) {
        mService.getVendorWithID(vendorID, callback);
    }

}
