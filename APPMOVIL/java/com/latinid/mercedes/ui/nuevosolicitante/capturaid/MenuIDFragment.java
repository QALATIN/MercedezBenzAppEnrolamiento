package com.latinid.mercedes.ui.nuevosolicitante.capturaid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

import com.latinid.mercedes.databinding.FragmentMenuIdBinding;
import com.latinid.mercedes.databinding.FragmentMenuSkipBinding;

public class MenuIDFragment extends DialogFragment {


    private FragmentMenuIdBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMenuIdBinding.inflate(inflater,container,false);
        controlButtons();
        return binding.getRoot();
    }

    private void controlButtons(){
        binding.buttonChip.setOnClickListener(view -> {
            sendAction("chip");
        });
        binding.buttonNormal.setOnClickListener(view -> {
            sendAction("normal");
        });
    }

    private void sendAction(String action){
        Bundle result = new Bundle();
        result.putString("action", action);
        getParentFragmentManager().setFragmentResult("captura", result);
        this.dismiss();
    }

}