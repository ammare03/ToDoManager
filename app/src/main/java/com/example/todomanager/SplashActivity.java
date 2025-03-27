package com.example.todomanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private TextView splashText;
    private ProgressBar splashProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashText = findViewById(R.id.splashText);
        splashProgressBar = findViewById(R.id.splashProgressBar);

        splashText.animate()
                .alpha(0f)
                .setDuration(1000)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        splashText.setVisibility(View.GONE);

                        splashProgressBar.setAlpha(0f);
                        splashProgressBar.setVisibility(View.VISIBLE);
                        splashProgressBar.animate()
                                .alpha(1f)
                                .setDuration(1000)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                                                } else {
                                                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                                }
                                                finish();
                                            }
                                        }, 1500);
                                    }
                                }).start();
                    }
                }).start();
    }
}