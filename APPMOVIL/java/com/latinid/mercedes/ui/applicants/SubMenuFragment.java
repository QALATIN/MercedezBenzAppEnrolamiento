package com.latinid.mercedes.ui.applicants;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.latinid.mercedes.databinding.FragmentSubMenuBinding;
import com.latinid.mercedes.Main2Activity;


public class SubMenuFragment extends Fragment {

    private FragmentSubMenuBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSubMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onResume() {
        super.onResume();
        binding.botonEnproceso.setOnClickListener(view -> {
            ((Main2Activity) getActivity()).replaceFragments(InProcessFragment.class);
        });
        binding.botonFinalizadas.setOnClickListener(view -> {
            ((Main2Activity) getActivity()).replaceFragments(CompleteProcessFragment.class);
        });
    }
}