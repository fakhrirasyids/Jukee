package com.project.jukee.ui.auth;

import static com.project.jukee.utils.Constants.IS_LOGGED_IN;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.project.jukee.R;
import com.project.jukee.databinding.ActivityAuthBinding;
import com.project.jukee.ui.auth.choice.ChoiceFragment;
import com.project.jukee.ui.auth.login.LoginFragment;
import com.project.jukee.ui.main.host.HostMainActivity;
import com.project.jukee.ui.main.visitor.VisitorMainActivity;
import com.project.jukee.utils.PreferenceManager;

public class AuthActivity extends AppCompatActivity {

    private Boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAuthBinding binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            isLoggedIn = getIntent().getBooleanExtra(IS_LOGGED_IN, false);

            if (isLoggedIn) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.auth_container, new ChoiceFragment())
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.auth_container, new LoginFragment())
                        .commit();
            }
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}