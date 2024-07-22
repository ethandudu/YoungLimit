package fr.ethanduault.younglimit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        loadSettings();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                saveSettings();
                startActivity(new Intent(Settings.this, MainActivity.class));
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    public void onAboutClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about);
        Context context = getApplicationContext();
        PackageManager pm = context.getPackageManager();
        String pn = context.getPackageName();
        String appVersion = null;
        try {
            appVersion = pm.getPackageInfo(pn, 0).versionName + " (" + pm.getPackageInfo(pn, 0).getLongVersionCode() + ")";
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        builder.setMessage(appVersion + "\n\nApplication développée par Ethan Duault et la communauté\n\nBasée sur OpenStreetMap et l'API Overpass\n\nInformations données à titre indicatif uniquement");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void loadSettings() {
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);

        if (preferences.getBoolean("darkmode", false)){
            SwitchMaterial darkmode = findViewById(R.id.darkmode);
            darkmode.setChecked(true);
        }

        if (preferences.getInt("refresh", 0) != 0){
            Slider slider = findViewById(R.id.slider);
            slider.setValue(preferences.getInt("refresh", 0));
        } else {
            Slider slider = findViewById(R.id.slider);
            slider.setValue(5);
        }
    }

    private void saveSettings() {
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        SwitchMaterial darkmode = findViewById(R.id.darkmode);
        editor.putBoolean("darkmode", darkmode.isChecked());

        Slider slider = findViewById(R.id.slider);
        editor.putInt("refresh", (int) slider.getValue());

        editor.apply();
    }
}