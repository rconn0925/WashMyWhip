package com.washmywhip.washmywhip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{


    @InjectView(R.id.signupSignup)
    Button singup;
    @InjectView(R.id.cancelSignup)
    Button cancel;
    @InjectView(R.id.usernameSignup)
    EditText username;
    @InjectView(R.id.passwordSignup)
    EditText password;
    @InjectView(R.id.reenterPasswordSignup)
    EditText reenterPassword;
    @InjectView(R.id.emailSignup)
    EditText email;
    @InjectView(R.id.phoneSignup)
    EditText phone;
    @InjectView(R.id.firstNameSignup)
    EditText firstName;
    @InjectView(R.id.lastNameSignup)
    EditText lastName;

    EditText[] fields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.inject(this);
        singup.setOnClickListener(this);
        cancel.setOnClickListener(this);
        fields = new EditText[]{username,password,reenterPassword,email,phone,firstName,lastName};
    }

    public boolean validateInput(){
        boolean emptyField = false;
        for (EditText field:fields){
            if(field.getText().toString().equals("")){
                emptyField = true;
            }
        }

        if(emptyField){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error creating account");
            builder.setMessage("Please fill out all the information.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
            return false;
        }
        //check if passwords match
        if(!password.getText().toString().equals(reenterPassword.getText().toString())){

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error creating account");
            builder.setMessage("Passwords do not match");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
            return false;
        }
        return true;
    }

    public void createUser(){
        LoginEngine mLogineEngine = new LoginEngine();
        final Intent intent = new Intent(this, MainActivity.class);
        mLogineEngine.createUser(username.getText().toString(), password.getText().toString(),
                email.getText().toString(), firstName.getText().toString(),
                lastName.getText().toString(), phone.getText().toString(),
                new Callback<Integer>() {
                    @Override
                    public void success(Integer i, Response response) {
                        String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                        Log.d("slim thug", responseString);
                        Log.d("createUSER", "CREATE user SUCCESS");
                        //update user that account creation was successful then return to login screen.
                        //Toast pop up registration successful
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("createUSER", "CREATE user FAIL", error);
                    }
                });
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == singup.getId()) {
            if(validateInput()){
                createUser();
            }

        }
        if(v.getId() == cancel.getId()){
            Intent i = new Intent(this,WelcomeActivity.class);
            startActivity(i);
            finish();
        }

    }
}
