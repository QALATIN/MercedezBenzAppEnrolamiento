package com.latinid.mercedes.ui.nuevosolicitante.fingerprint;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentMenuSkipBinding;
import com.latinid.mercedes.ui.nuevosolicitante.archivos.ModoCapturaFragment;

public class MenuSkipFragment extends DialogFragment {

    private FragmentMenuSkipBinding binding;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMenuSkipBinding.inflate(inflater,container,false);
        controlButtons();
        return binding.getRoot();
    }

    private void controlButtons(){
        binding.buttonAmp.setOnClickListener(view -> {
            sendAction("amputado");
        });
        binding.buttonBand.setOnClickListener(view -> {
            sendAction("vendado");
        });
        binding.buttonNa.setOnClickListener(view -> {
            sendAction("na");
        });
    }

    private void sendAction(String action){
        Bundle result = new Bundle();
        result.putString("action", action);
        getParentFragmentManager().setFragmentResult("omitido", result);
        this.dismiss();
    }

}