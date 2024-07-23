package fr.ethanduault.younglimit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.Manifest;
import android.location.Location;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.caverock.androidsvg.SVG;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.Future;


public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private int speedLimit = 0;
    private double speed = 0.0;

    private int refreshDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (!loadSettings()) {
            super.finish();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.location_permission_title);
                builder.setMessage(R.string.location_permission_message);
                builder.setPositiveButton(R.string.ok, (dialog, which) -> grantLocationPermission()
                );
                builder.show();
            }

            if (isLocationPermissionGranted()) {
                run();
            }
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) { // Match the request code used in the permission request
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                run();
            } else {
                // Permission was denied or request was cancelled
                Toast.makeText(this, R.string.permission_needed, Toast.LENGTH_SHORT).show();
                super.finish();
            }
        }
    }

    private void run() {
        getDeviceLocation();
        getSpeedLimit();
        setSpeedLimit(speedLimit);
        startLocationLoop();
        speedLoop();
    }

    private void speedLoop() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setSpeed(speed, speedLimit);
                handler.postDelayed(this, 500);
            }
        }, 500);
    }
    private void grantLocationPermission() {
        // ask for location permission and set callback
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationLoop() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                getDeviceLocation();
                getSpeedLimit();
                setSpeedLimit(speedLimit);
                handler.postDelayed(this, refreshDelay);
            }
        }, refreshDelay);
    }

    private void setSpeed(double speed, int speedLimit) {
        System.out.println("speed: " + speed + " speedLimit: " + speedLimit);
        ProgressBar speedBar = findViewById(R.id.speed_progress);
        TextView speedText = findViewById(R.id.speed);
        speedText.setText(MessageFormat.format("{0} km/h", speed));
        if (speedLimit == 0) {
            speedBar.setProgress(0);
            return;
        }
        if (speed > speedLimit) {
            speedBar.setProgress(100);
        } else {
            speedBar.setProgress((int) ((speed / speedLimit) * 100));
        }
    }

    private void setSpeedLimit(int speedLimit) {
        try {
            String svgContent = readSvgFromFile(R.raw.speed_sign);
            if (svgContent != null) {
                svgContent = svgContent.replaceAll(">(\\d+)<", ">" + String.valueOf(speedLimit) + "<");

                SVG svg = SVG.getFromString(svgContent);
                PictureDrawable drawable = new PictureDrawable(svg.renderToPicture());
                ImageView imageView = findViewById(R.id.speed_sign);
                imageView.setImageDrawable(drawable);
            }
        } catch (Exception e) {

        }
    }

    private String readSvgFromFile(int resourceId) {
        try {
            InputStream inputStream = getResources().openRawResource(resourceId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder svgContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                svgContent.append(line).append("\n");
            }
            reader.close();
            return svgContent.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getDeviceLocation(){
        try {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        speed = location.getSpeed() * 3.6f;
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (SecurityException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void getSpeedLimit() {
        Future<String> request = HttpRequest.execute("https://overpass-api.de/api/interpreter", latitude, longitude);
        String response;

        try {
            TextView roadType = findViewById(R.id.road_type);
            response = request.get();
            JSONObject json = new JSONObject(response);

            if ((json.getJSONArray("elements").length() == 0)) {
                roadType.setText(R.string.unknown_road);
                speedLimit = 0;
                return;
            }
            JSONObject tags = new JSONObject(response).getJSONArray("elements").getJSONObject(0).getJSONObject("tags");

            if (!tags.has("maxspeed")) {
                System.out.println("No maxspeed");
                speedLimit = 0;
                return;
            }
            if (tags.has("name") && tags.has("ref")) {
                roadType.setText(MessageFormat.format("{0} - {1}", tags.getString("name"), tags.getString("ref")));
            } else if (tags.has("name")) {
                roadType.setText(tags.getString("name"));
            } else if (tags.has("ref")) {
                roadType.setText(tags.getString("ref"));
            }

            if (tags.getString("highway").equals("motorway") && Integer.parseInt(tags.getString("maxspeed")) == 130) {
                speedLimit = 110;
                return;
            } else if(tags.getString("highway").equals("trunk") && Integer.parseInt(tags.getString("lanes")) >= 2 && tags.getString("oneway").equals("yes") && Integer.parseInt(tags.getString("maxspeed")) == 110) {
                speedLimit = 100;
                return;
            } else if (tags.getString("highway").equals("residential") && !tags.has("maxspeed")) {
                speedLimit = 50;
                return;
            } else {
                speedLimit = Integer.parseInt(tags.getString("maxspeed"));
                return;
            }

        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        speedLimit = 0;
    }

    private boolean loadSettings() {
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);

        if (preferences.getBoolean("isFirstLaunch", true)) {
            Intent intent = new Intent(this, FirstStart.class);
            startActivity(intent);
            return false;
        }
        refreshDelay = preferences.getInt("refresh", 5) * 1000;
        setTheme(preferences.getBoolean("darkmode", false));
        return true;
    }
    
    private void setTheme(Boolean darkmode) {
        if (darkmode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (!darkmode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void openSettings(View v) {
        startActivity(new Intent(this, Settings.class));
        super.finish();
    }
}