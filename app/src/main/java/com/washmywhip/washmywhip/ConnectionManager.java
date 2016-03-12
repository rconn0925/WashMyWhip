package com.washmywhip.washmywhip;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import android.provider.Settings.Secure;

import com.google.android.gms.maps.model.LatLng;

import java.net.URL;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * Created by Ross on 3/4/2016.
 */
public class ConnectionManager {

    private String mAddress;
    private Socket mSocket;
    private int userID;
    private String deviceID;
    SharedPreferences mSharedPreferences;
    public ConnectionManager(Context context){

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        // SocketIO socket =new SocketIO();
        deviceID = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
        userID = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
        Log.d("server connection","response?: " + deviceID);

        try {
           // mAddress = "http://192.168.0.18:3000";
            mAddress = "http://54.191.214.16:3000";
          //  IO.Options opts = new IO.Options();
          //  opts.forceNew = true;
          //  opts.reconnection = false;
            if (mSocket!=null&&mSocket.connected())
            {
                mSocket.close();
            }
            mSocket = IO.socket(mAddress);
            mSocket.on("addUserConfirmation", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onAddUser");
                    if(args!=null&&args.length>0){
                        Log.d("server connection", "onAddUser staus: "+ args[0].toString());
                    }
                }
            }).on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onConnect");
                    if (args != null) {
                        Log.d("server connection", "onConnect: " + args.length);
                        addUser();
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String response = (String)args[0];
                    Log.d("server connection", "onDisconnect: "+ response);
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    Log.d("server connection", "onConnectionError: ");
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String response = (String) args[0];
                    Log.d("server connection", "onError: " + response);
                }
            }).on("requestWash", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onRequestWash (i dont think this is called)");
                    if(args!=null&&args.length>0){
                        Log.d("server connection", "onRequestWash staus: "+ args[0].toString());
                    }
                }
            }).on("cancelWash", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onCancelWash (i dont think this is called)");
                    if(args!=null&&args.length>0){
                        Log.d("server connection", "onCancelWash staus: "+ args[0].toString());
                    }
                }
            }).on("updateVendorInfo", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onUpdateVendorInfo getting VendorID and Location");
                    if(args!=null&&args.length>0){
                        Log.d("server connection", "onUpdateVendorInfo: "+ args[0].toString()+ " "+ args[1].toString());
                    }
                }
            }).on("updateTransactionID", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onUpdateTransactionID getting transactionID");
                    if(args!=null&&args.length>0){
                        Log.d("server connection", "onUpdateTransactionID: "+ args[0].toString());
                    }
                }
            }).on("transactionID", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onTransactionID getting transactionID");
                    if(args!=null&&args.length>0){
                        Log.d("server connection", "onTransactionID: "+ args[0].toString());
                        //put info into shared preferences?
                    }
                }
            }).on("vendorInfo", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onVendorInfo getting VendorID and Location");
                    if(args!=null&&args.length>1){
                        Log.d("server connection", "onVendorInfo: "+ args[0].toString()+" "+ args[1].toString());
                        //put info into shared preferences?
                    }
                }
            });
            Log.d("server connection", "attempting to connect...");
            mSocket.connect();

        }  catch (URISyntaxException e) {
            e.printStackTrace();
            Log.d("server connection", "failzz?");
        }
    }

    public void addUser(){
        Log.d("server connection", "addUSer server: "+ mSocket.connected());
        String userIDstring =Integer.toString(userID);
        //empty string place holder for device
        String[] data = {userIDstring,""};
        if(mSocket.connected()){
            mSocket.emit("addUser", data);
        }
    }
    public void requestWash(LatLng location, int carID, int washType){
        Log.d("server connection", "requestWash server: "+ mSocket.connected());

        Object[] data = {location,carID,washType};
        if(mSocket.connected()){
            mSocket.emit("requestWash", data);
        }
    }

    public void cancelRequest(){
        Log.d("server connection", "cancelRequest server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("cancelRequest", "");
        }
    }

    public void updateVendorInfo(){
        Log.d("server connection", "updateVendorInfo server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("updateVendorInfo", "");
        }
    }

    public void updateTransactionID(){
        Log.d("server connection", "updateTransactionID server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("updateTransactionID", "");
        }
    }

    public void userHasFinalized(){
        Log.d("server connection", "userHasFinalized server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("userHasFinalized", "");
        }
    }



    public void disconnect(){
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT);
        mSocket.off(Socket.EVENT_DISCONNECT);
        mSocket.off(Socket.EVENT_ERROR);
        mSocket.off(Socket.EVENT_CONNECT_ERROR);
        mSocket.off("addUser");
        mSocket.off("requestWash");
        mSocket.off("cancelRequest");
        mSocket.off("updateVendorInfo");
        mSocket.off("updateTransactionID");
        mSocket.off("userHasFinalized");
        mSocket.off("transactionID");
        mSocket.off("vendorInfo");

    }

    public boolean isConnected(){
        return mSocket.connected();
    }
}
