package com.washmywhip.washmywhip;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.inject(this);
        singup.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == singup.getId()) {

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
        if(v.getId() == cancel.getId()){
            Intent i = new Intent(this,WelcomeActivity.class);
            startActivity(i);
            finish();
        }

    }
}
