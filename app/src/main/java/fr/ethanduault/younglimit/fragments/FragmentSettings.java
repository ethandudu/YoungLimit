package fr.ethanduault.younglimit.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import fr.ethanduault.younglimit.R;

public class FragmentSettings extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button buttonNext = view.findViewById(R.id.next);
        Button buttonBack = view.findViewById(R.id.previous);
        SwitchMaterial switchPermission= view.findViewById(R.id.permissions);

        SwitchMaterial darkmode = view.findViewById(R.id.darkmode);
        SwitchMaterial speedAlert = view.findViewById(R.id.speedalert);

        Slider slider = view.findViewById(R.id.slider);

        // check if the OS is in dark mode
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            darkmode.setChecked(true);
        }

        // listen the changes on the dark mode switch
        darkmode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // listen the changes on the permissions switch
        switchPermission.setOnCheckedChangeListener((buttonView, isChecked)-> {
            if (isChecked) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            }
        });

        // listen for the click on the next button
        buttonNext.setOnClickListener(v -> {
            if (!switchPermission.isChecked()){
                Toast.makeText(requireActivity(), R.string.permission_needed, Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("darkmode", darkmode.isChecked());
            editor.putBoolean("speedAlert", speedAlert.isChecked());
            editor.putBoolean("isFirstLaunch", false);
            editor.putInt("refreshDelay", (int) slider.getValue());
            editor.apply();

            FragmentFinish fragmentFinish = new FragmentFinish();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragmentFinish);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // listen for the click on the back button
        buttonBack.setOnClickListener(v -> {
            FragmentWelcome fragmentWelcome = new FragmentWelcome();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragmentWelcome);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SwitchMaterial switchPermission = requireView().findViewById(R.id.permissions);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switchPermission.setEnabled(false);
            } else {
                Toast.makeText(requireActivity(), R.string.permission_needed, Toast.LENGTH_SHORT).show();
                switchPermission.setChecked(false);
            }
        }
    }
}