package fr.ethanduault.younglimit;

import android.content.pm.PackageManager;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.Manifest;
import android.location.Location;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
    private double speed = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getDeviceLocation();
        setSpeedLimit(getSpeedLimit());

        startLocationLoop();
    }

    // loop to get the location every 5 seconds
    private void startLocationLoop() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getDeviceLocation();
                setSpeedLimit(getSpeedLimit());
                handler.postDelayed(this, 5000);
            }
        }, 5000);
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
                        location.getSpeed();
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

    private int getSpeedLimit() {
        Future<String> request = HttpRequest.execute("https://overpass-api.de/api/interpreter", latitude, longitude);
        String response;

        try {
            TextView roadType = findViewById(R.id.road_type);
            response = request.get();

            if (new JSONObject(response).getJSONArray("elements").length() == 0) {
                roadType.setText("Route inconnue");
                return 0;
            }
            JSONObject tags = new JSONObject(response).getJSONArray("elements").getJSONObject(0).getJSONObject("tags");

            if (tags.has("name") && tags.has("ref")) {
                roadType.setText(MessageFormat.format("{0} - {1}", tags.getString("name"), tags.getString("ref")));
            } else if (tags.has("name")) {
                roadType.setText(tags.getString("name"));
            } else if (tags.has("ref")) {
                roadType.setText(tags.getString("ref"));
            }

            if (tags.getString("highway").equals("motorway") && Integer.parseInt(tags.getString("maxspeed")) == 130) {
                return 110;
            } else if(tags.getString("highway").equals("trunk") && Integer.parseInt(tags.getString("lanes")) >= 2 && tags.getString("oneway").equals("yes") && Integer.parseInt(tags.getString("maxspeed")) == 110) {
                return 100;
            } else if (tags.getString("highway").equals("residential") && !tags.has("maxspeed")) {
                return 50;
            } else {
                return Integer.parseInt(tags.getString("maxspeed"));
            }

        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return 0;
    }

}