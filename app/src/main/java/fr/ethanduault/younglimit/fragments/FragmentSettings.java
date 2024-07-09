package fr.ethanduault.younglimit.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
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

        SwitchMaterial darkmode = view.findViewById(R.id.darkmode);

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

        // listen for the click on the next button
        buttonNext.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("darkmode", darkmode.isChecked());
            editor.putBoolean("firstStart", false);
            editor.putInt("refreshDelay", (int) slider.getValue());
            editor.apply();

//            FragmentWelcome fragmentFinish = new FragmentFinish();
//            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
//            FragmentTransaction transaction = fragmentManager.beginTransaction();
//            transaction.replace(R.id.fragment_container, fragmentFinish);
//            transaction.addToBackStack(null);
//            transaction.commit();
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
}