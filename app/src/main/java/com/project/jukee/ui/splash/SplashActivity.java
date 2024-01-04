package com.project.jukee.ui.splash;

import static com.project.jukee.utils.Constants.IS_LOGGED_IN;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.project.jukee.R;
import com.project.jukee.databinding.ActivitySplashBinding;
import com.project.jukee.ui.auth.AuthActivity;
import com.project.jukee.ui.auth.choice.ChoiceFragment;
import com.project.jukee.ui.main.host.HostMainActivity;
import com.project.jukee.ui.main.visitor.VisitorMainActivity;
import com.project.jukee.utils.PreferenceManager;

public class SplashActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);

        new Handler().postDelayed(() -> {
            if (preferenceManager.isLoggedAsNormalUser()) {
                if (preferenceManager.isLoggedAsHost()) {
                    Intent iHost = new Intent(this, HostMainActivity.class);
                    finishAffinity();
                    startActivity(iHost);
                } else if (preferenceManager.isLoggedAsVisitor()) {
                    Intent iVisitor = new Intent(this, VisitorMainActivity.class);
                    finishAffinity();
                    startActivity(iVisitor);
                } else {
                    Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
                    intent.putExtra(IS_LOGGED_IN, true);
                    startActivity(intent);
                    finish();
                }
            } else if (!preferenceManager.isLoggedAsNormalUser()) {
                Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
                intent.putExtra(IS_LOGGED_IN, false);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}