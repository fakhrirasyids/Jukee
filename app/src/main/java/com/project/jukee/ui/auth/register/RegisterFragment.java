package com.project.jukee.ui.auth.register;

import static com.project.jukee.utils.Constants.COL_EMAIL;
import static com.project.jukee.utils.Constants.COL_PASSWORD;
import static com.project.jukee.utils.Constants.COL_USERNAME;
import static com.project.jukee.utils.Constants.KEY_LOGGED_NORMAL_USER;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.jukee.R;
import com.project.jukee.data.remote.FirebaseConfig;
import com.project.jukee.databinding.FragmentRegisterBinding;
import com.project.jukee.ui.auth.AuthActivity;
import com.project.jukee.ui.auth.choice.ChoiceFragment;
import com.project.jukee.ui.auth.login.LoginFragment;

import java.util.HashMap;
import java.util.Objects;

public class RegisterFragment extends Fragment {

    private final DatabaseReference firebaseReference = new FirebaseConfig().getUserListDatabaseReference();
    private FragmentRegisterBinding binding;
    private AuthActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        activity = (AuthActivity) requireActivity();

        setupPlayAnimation();
        setListeners();

        return binding.getRoot();
    }

    private void setListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            if (isValid()) {
                showLoading(true);

                HashMap<String, String> userAccount = new HashMap<>();

                userAccount.put(COL_USERNAME, binding.edName.getText().toString());
                userAccount.put(COL_EMAIL, binding.edEmail.getText().toString());
                userAccount.put(COL_PASSWORD, binding.edPassword.getText().toString());
                firebaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean flag = false;

                        if (snapshot.exists()) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                if (Objects.requireNonNull(data.child(COL_EMAIL).getValue()).toString().equals(binding.edEmail.getText().toString())) {
                                    flag = true;
                                }
                            }
                        }

                        if (flag) {
                            showLoading(false);
                            activity.showToast("Email already taken!");
                        } else {
                            firebaseReference.push().setValue(userAccount)
                                    .addOnFailureListener(e -> {
                                        showLoading(false);
                                        activity.showToast("Failed to Register : " + e);
                                    })
                                    .addOnSuccessListener(unused -> {
                                        showLoading(false);
                                        activity.showToast("User successfully registered, please Log In!");

                                        getParentFragmentManager().beginTransaction()
                                                .replace(
                                                        R.id.auth_container,
                                                        new LoginFragment(),
                                                        LoginFragment.class.getSimpleName()
                                                )
                                                .commit();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        activity.showToast(error.getMessage());
                        showLoading(false);
                    }
                });
            }
        });

        binding.btnLogin.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(
                        R.id.auth_container,
                        new LoginFragment(),
                        LoginFragment.class.getSimpleName()
                )
                .commit());
    }


    @SuppressLint("Recycle")
    private void setupPlayAnimation() {
        ObjectAnimator.ofFloat(binding.ivLogo, View.TRANSLATION_X, -30f, 30f).setDuration(2000);
        ObjectAnimator.ofFloat(binding.ivLogo, View.TRANSLATION_X, -30f, 30f).setRepeatCount(ObjectAnimator.INFINITE);
        ObjectAnimator.ofFloat(binding.ivLogo, View.TRANSLATION_X, -30f, 30f).setRepeatMode(ObjectAnimator.REVERSE);

        Animator title = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 1f).setDuration(150);
        Animator name = ObjectAnimator.ofFloat(binding.edName, View.ALPHA, 1f).setDuration(150);
        Animator email = ObjectAnimator.ofFloat(binding.edEmail, View.ALPHA, 1f).setDuration(150);
        Animator password =
                ObjectAnimator.ofFloat(binding.edPassword, View.ALPHA, 1f).setDuration(150);
        Animator button =
                ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(150);
        Animator layoutText = ObjectAnimator.ofFloat(binding.layoutLogin, View.ALPHA, 1f).setDuration(150);

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.playSequentially(title, name, email, password, button, layoutText);
        animatorSet.setStartDelay(150);
        animatorSet.start();
    }

    private Boolean isValid() {
        if (binding.edName.getText().toString().isEmpty()) {
            activity.showToast("Name field must be filled!");
            return false;
        } else if (binding.edEmail.getText().toString().isEmpty()) {
            activity.showToast("Email field must be filled!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edEmail.getText().toString()).matches()) {
            activity.showToast("Fill email with a valid email pattern!");
            return false;
        } else if (binding.edPassword.getText().toString().isEmpty()) {
            activity.showToast("Password field must be filled!");
            return false;
        } else {
            return true;
        }
    }

    private void showLoading(Boolean isLoading) {
        if (isLoading) {
            binding.progressbar.setVisibility(View.VISIBLE);
            binding.btnRegister.setVisibility(View.GONE);
        } else {
            binding.progressbar.setVisibility(View.GONE);
            binding.btnRegister.setVisibility(View.VISIBLE);
        }
    }
}