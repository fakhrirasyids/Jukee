package com.project.jukee.ui.auth.choice;

import static com.project.jukee.utils.Constants.KEY_LOGGED_HOST;
import static com.project.jukee.utils.Constants.KEY_LOGGED_VISITOR;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.jukee.R;
import com.project.jukee.databinding.FragmentChoiceBinding;
import com.project.jukee.databinding.FragmentLoginBinding;
import com.project.jukee.ui.auth.AuthActivity;
import com.project.jukee.ui.main.host.HostMainActivity;
import com.project.jukee.ui.main.visitor.VisitorMainActivity;
import com.project.jukee.utils.PreferenceManager;

public class ChoiceFragment extends Fragment {
    private PreferenceManager preferenceManager;

    private FragmentChoiceBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChoiceBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(requireContext());

        binding.tvUsername.setText(new StringBuilder("Welcome, " + preferenceManager.getUsername() + "!"));

        setupPlayAnimation();
        setListeners();

        return binding.getRoot();
    }

    private void setListeners() {
        binding.btnHost.setOnClickListener(v -> {
            preferenceManager.setLoggedIn(KEY_LOGGED_HOST, true);

            Intent iHost = new Intent(requireContext(), HostMainActivity.class);
            requireActivity().finishAffinity();
            startActivity(iHost);
        });

        binding.btnVisitor.setOnClickListener(v -> {
            preferenceManager.setLoggedIn(KEY_LOGGED_VISITOR, true);

            Intent iVisitor = new Intent(requireContext(), VisitorMainActivity.class);
            requireActivity().finishAffinity();
            startActivity(iVisitor);
        });

        binding.btnLogout.setOnClickListener(v -> {
            preferenceManager.clearPreferences();

            Intent iAuth = new Intent(requireContext(), AuthActivity.class);
            requireActivity().finishAffinity();
            startActivity(iAuth);
        });
    }

    @SuppressLint("Recycle")
    private void setupPlayAnimation() {
        Animator layoutWelcoming = ObjectAnimator.ofFloat(binding.layoutWelcoming, View.ALPHA, 1f).setDuration(150);

        Animator title = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 1f).setDuration(150);
        Animator buttonHost =
                ObjectAnimator.ofFloat(binding.btnHost, View.ALPHA, 1f).setDuration(150);
        Animator buttonVisitor =
                ObjectAnimator.ofFloat(binding.btnVisitor, View.ALPHA, 1f).setDuration(150);
        Animator buttonLogout =
                ObjectAnimator.ofFloat(binding.btnLogout, View.ALPHA, 1f).setDuration(150);

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.playSequentially(layoutWelcoming, title, buttonHost, buttonVisitor, buttonLogout);
        animatorSet.setStartDelay(150);
        animatorSet.start();
    }
}