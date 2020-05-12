package com.shubzz.hqm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.shubzz.hqm.ui.login.LoginFragment;
import com.shubzz.hqm.ui.signup.SignUpfragment;
import com.shubzz.hqm.utils.SessionHandler;

public class LSActivity extends AppCompatActivity {

    private TextView btnSignup, btnLogin;
    private SessionHandler sessionHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionHandler = new SessionHandler(this);
        if (sessionHandler.isLoggedIn()) {
            loadMainActivity();
        }

        setContentView(R.layout.activity_ls);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.frameLayout, new LoginFragment()).commit();
                btnSignup.setBackgroundResource(R.drawable.button_background);
                view.setBackgroundResource(R.drawable.button_background_selected);
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.frameLayout, new SignUpfragment()).commit();
                btnLogin.setBackgroundResource(R.drawable.button_background);
                view.setBackgroundResource(R.drawable.button_background_selected);
            }
        });

        btnLogin.callOnClick();
    }

    private void loadMainActivity() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}
