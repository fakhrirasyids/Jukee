package com.project.jukee.ui.main.visitor;

import static com.project.jukee.utils.Constants.IS_LOGGED_IN;
import static com.project.jukee.utils.Constants.KEY_LOGGED_HOST;
import static com.project.jukee.utils.Constants.KEY_LOGGED_VISITOR;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.project.jukee.R;
import com.project.jukee.databinding.ActivityVisitorMainBinding;
import com.project.jukee.ui.auth.AuthActivity;
import com.project.jukee.utils.PreferenceManager;

public class VisitorMainActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;

    private ActivityVisitorMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisitorMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_visitor_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        binding.btnLogout.setOnClickListener(v -> {
            preferenceManager.setLoggedIn(KEY_LOGGED_VISITOR, false);

            Intent intent = new Intent(this, AuthActivity.class);
            intent.putExtra(IS_LOGGED_IN, true);
            startActivity(intent);
            finish();
        });
    }

}