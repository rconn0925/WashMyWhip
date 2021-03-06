package com.washmywhip.washmywhip;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
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
    private Context mContext;
    public ConnectionManager(Context context){

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        // SocketIO socket =new SocketIO();
        deviceID = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
        userID = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
        Log.d("server connection","response?: " + deviceID);
        mContext = context;

        try {
           // mAddress = "http://192.168.0.18:3000";
            mAddress = "http://54.191.214.16:3000";
          //  IO.Options opts = new IO.Options();
          //  opts.forceNew = true;
          //  opts.reconnection = false;
            if (mSocket!=null&&mSocket.connected())
            {
                Log.d("serverConnection","socket is closing");
                mSocket.close();
            }
            mSocket = IO.socket(mAddress);
            mSocket.on("addUserConfirmation", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onAddUser");
                    if (args != null && args.length > 0) {
                        Log.d("server connection", "onAddUser staus: " + args[0].toString());
                        Intent intent = new Intent();
                        intent.putExtra("state", args[0].toString());
                        intent.setAction("com.android.activity.SEND_DATA");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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
                   // String response = (String) args[0];
                    Log.d("server connection", "onError: ");
                }
            }).on("requestWash", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onRequestWash (i dont think this is called)");
                    if(args!=null&&args.length>0){
                        Log.d("server connection", "onRequestWash staus: "+ args[0].toString());
                    }
                }
            }).on("updateVendorETA", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "on updateETA getting location");
                    if (args != null && args.length > 0) {
                        /*
                        Log.d("server connection", "updateETA: "+ args[0].toString());
                        Intent intent = new Intent();
                        intent.putExtra("vendorLocation", args[0].toString());
                        intent.setAction("com.android.activity.SEND_DATA");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        */
                    }
                    //  updateVendorInfo();

                }
            }).on("transactionID", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onTransactionID getting transactionID");
                    if(args!=null&&args.length>0){
                        Log.d("server connection", "onTransactionID: " + args[0].toString());
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
            }).on("requestAccepted", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "requestAccepted getting VendorID and Location");
                    if(args!=null&&args.length>1){
                        Log.d("server connection", "requestAccepted: "+ args[0].toString()+" "+ args[1].toString());
                        //put info into shared preferences?
                        //this should initialize the waiting layout
                        Intent intent = new Intent();
                        intent.putExtra("vendorInfo", args[0].toString()+", "+ args[1].toString());
                        intent.setAction("com.android.activity.SEND_DATA");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    }
                }
            }).on("vendorArrived", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "on VENDOR has arrived ");
                    Intent intent = new Intent();
                    intent.putExtra("vendorHasArrived", "true");
                    intent.setAction("com.android.activity.SEND_DATA");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            }).on("washStarted", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "on VENDOR has started wash ");
                    if(args!=null&&args.length>0) {
                        Intent intent = new Intent();
                        intent.putExtra("vendorHasStartedWash", args[0].toString());
                        intent.setAction("com.android.activity.SEND_DATA");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        mSharedPreferences.edit().putString("transactionID",args[0].toString()).apply();
                    }

                }
            }).on("washCompleted", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "on VENDOR has completed wash ");
                    if(args!=null&&args.length>0) {
                        Intent intent = new Intent();
                        intent.putExtra("vendorHasCompletedWash",  args[0].toString());
                        intent.setAction("com.android.activity.SEND_DATA");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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
        String locationString = location.latitude+", "+location.longitude;
        Log.d("server connection", "requestWash server: "+ locationString);
        Object[] data = {locationString,carID,washType};
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
    public void cancelActiveRequest() {
        Log.d("server connection", "cancelActiveRequest server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("cancelActiveRequest", "");
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
        mSocket.off("updateETA");
        mSocket.off("userHasFinalized");
        mSocket.off("transactionID");
        mSocket.off("vendorInfo");
        mSocket.off("washCompleted");
        mSocket.off("washStarted");

    }

    public boolean isConnected(){
        return mSocket.connected();
    }

}
