package fr.ethanduault.younglimit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.res.Configuration;
import android.os.Bundle;
import fr.ethanduault.younglimit.fragments.FragmentWelcome;

public class FirstStart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);

        // check if the OS is in dark mode
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        }

        if (savedInstanceState == null){
            FragmentWelcome fragmentWelcome = new FragmentWelcome();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragmentWelcome)
                    .commit();
        }
    }
}
