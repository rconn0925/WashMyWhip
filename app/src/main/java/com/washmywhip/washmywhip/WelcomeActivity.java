package com.washmywhip.washmywhip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    @InjectView(R.id.loginButton)
    Button login;
    @InjectView(R.id.signUpButton)
    Button signup;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        ButterKnife.inject(this);
        login.setOnClickListener(this);
        signup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.loginButton){
            Intent i = new Intent(this, LoginActivity.class);
            Log.d("muffins","i lyke muffins");
            startActivity(i);
            finish();
        }
        if(v.getId() == R.id.signUpButton){
            Intent i = new Intent(this, SignUpActivity.class);
            Log.d("muffins", "i dont lyke muffins");
            startActivity(i);
            finish();
        }
    }
}
