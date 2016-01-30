package com.washmywhip.washmywhip;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
/**
 * Created by MICRO PENIS on 1/25/2016.
 */
public class LoginEngine {

    private LoginService mLoginService;
    private String user,pass;

    public LoginEngine(){
        OkHttpClient okhttpclient = new OkHttpClient();

        OkClient okclient = new OkClient(okhttpclient);

        RestAdapter.Builder builder =  new RestAdapter.Builder()
                .setEndpoint("http://www.ryanserkes.com/WashMyWhip/")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(okclient);

        RestAdapter adapter = builder.build();
        mLoginService = adapter.create(LoginService.class);
    }

    public JSONObject userLoginJSON(String userid, String password) {
        JSONObject json = new JSONObject();
        try {
            json.put("username", userid);
            json.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void requestUserLogin(String username, String password, Callback<JSONObject> callback) {
        mLoginService.requestUserLogin(username,password, callback);
    }

    public void requestTemporaryPassword(String email,Callback<JSONObject> callback) {
        mLoginService.requestTemporaryPassword(email, callback);
    }

    public void createUser(String username, String password, String email,String phoneNumber, String firstName, String lastName,Callback<Integer> callback) {
        mLoginService.createUser(username, password, email,phoneNumber, firstName, lastName, callback);
    }

    public void updateUserInfo(int userId, String email, String firstName, String lastName, String phoneNumber) {
        mLoginService.updateUserInfo(userId, email, firstName, lastName, phoneNumber);
    }

    public void updateUserPassword(String username, String password,Callback<JSONObject> callback) {
        mLoginService.updateUserPassword(username, password, callback);
    }

}


