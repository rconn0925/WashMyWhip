package com.washmywhip.washmywhip;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Ross on 4/5/2016.
 */
public class WashMyWhip extends Application {
    @Override
    public void onCreate() {

        // TODO Auto-generated method stub
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("isLoggedIn", false)){
            //Login activity
            Log.d("WASHMYWHIP","login from past session");
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        } else {
            //Main Activity
            Log.d("WASHMYWHIP","new login session");
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }
    }

}


