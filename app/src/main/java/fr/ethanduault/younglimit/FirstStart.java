package fr.ethanduault.younglimit;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import fr.ethanduault.younglimit.fragments.FragmentWelcome;

public class FirstStart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);

        if (savedInstanceState == null){
            FragmentWelcome fragmentWelcome = new FragmentWelcome();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragmentWelcome)
                    .commit();
        }
    }
}
