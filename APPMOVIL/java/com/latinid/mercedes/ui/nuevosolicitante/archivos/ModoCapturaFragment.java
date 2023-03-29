package com.latinid.mercedes.ui.nuevosolicitante.archivos;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentModoCapturaBinding;
import com.latinid.mercedes.ui.nuevosolicitante.selfie.fragments.SelfieAwareFragment;


public class ModoCapturaFragment extends DialogFragment {

    private FragmentModoCapturaBinding binding;

    private static final String ARG_TITULO = "titulo";
    private static final String ARG_SUBTITULO = "subtitulo";

    private String titulo;
    private String subtitulo;


    public static ModoCapturaFragment newInstance(String param1, String param2) {
        ModoCapturaFragment fragment = new ModoCapturaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITULO, param1);
        args.putString(ARG_SUBTITULO, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ModoCapturaFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            titulo = getArguments().getString(ARG_TITULO);
            subtitulo = getArguments().getString(ARG_SUBTITULO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentModoCapturaBinding.inflate(inflater,container,false);
        binding.titulo.setText(titulo);
        binding.subtitulo.setText(subtitulo);
        controlBotones();
        return binding.getRoot();
    }

    private void controlBotones(){

        binding.adjuntar.setOnClickListener(view -> {
            enviarAccion("adjuntar");
        });
        binding.camara.setOnClickListener(view -> {
            enviarAccion("camara");
        });
        binding.cerrar.setOnClickListener(view -> {
            enviarAccion("cerrar");
        });
    }

    private void enviarAccion(String accion){
        Bundle result = new Bundle();
        result.putString("accion", accion);
        getParentFragmentManager().setFragmentResult("modoCaptura", result);
        this.dismiss();
    }

}