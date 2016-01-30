package com.washmywhip.washmywhip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    @InjectView(R.id.forgotPasswordEmail)
    EditText email;
    @InjectView(R.id.submitForgotPassswordButton)
    Button submit;
    @InjectView(R.id.cancelForgotPassswordButton)
    Button cancel;

    private LoginEngine mLogineEngine;
    private SharedPreferences mSharedPreferences;
    private String user,pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_layout);
        ButterKnife.inject(this);
        cancel.setOnClickListener(this);
        submit.setOnClickListener(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        user = mSharedPreferences.getString("username", "null");
        pass = mSharedPreferences.getString("password","null");
        mLogineEngine = new LoginEngine();
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == submit.getId()){
            //send temp pass to email
            //do some server shit
            mLogineEngine.requestTemporaryPassword(email.getText().toString(), new Callback<JSONObject>() {
                @Override
                public void success(JSONObject jsonObject, Response response) {
                    String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                    Log.d("small PIMPING", responseString);
                    Log.d("monkeys", "update user Password SUCCESS");
                    //update user that password reset has been successful then return to login screen.
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("monkeys", "update user Password FAIL");
                }
            });
            Intent i = new Intent(this,LoginActivity.class);
            startActivity(i);
            finish();
        }
        if(v.getId() == cancel.getId()){
            Intent i = new Intent(this,LoginActivity.class);
            startActivity(i);
            finish();
        }
    }
}
