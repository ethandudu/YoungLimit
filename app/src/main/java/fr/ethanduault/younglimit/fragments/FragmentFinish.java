package fr.ethanduault.younglimit.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import fr.ethanduault.younglimit.MainActivity;
import fr.ethanduault.younglimit.R;

public class FragmentFinish extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finish, container, false);

        Button buttonNext = view.findViewById(R.id.next);
        Button buttonBack = view.findViewById(R.id.previous);

        buttonNext.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        buttonBack.setOnClickListener(v -> {
            FragmentSettings fragmentSettings = new FragmentSettings();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragmentSettings);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        return view;
    }
}
