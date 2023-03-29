package com.latinid.mercedes.util;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentMessageBinding;
import com.latinid.mercedes.databinding.FragmentUpdateBinding;
import com.latinid.mercedes.ui.nuevosolicitante.archivos.ModoCapturaFragment;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GenericMessage extends DialogFragment {

    private FragmentMessageBinding binding;

    private static final String ARG_TITULO = "titulo";
    private static final String ARG_SUBTITULO = "subtitulo";

    private String titulo;
    private String subtitulo;


    public static GenericMessage newInstance(String param1, String param2) {
        GenericMessage fragment = new GenericMessage();
        Bundle args = new Bundle();
        args.putString(ARG_TITULO, param1);
        args.putString(ARG_SUBTITULO, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public GenericMessage() {
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
        binding = FragmentMessageBinding.inflate(inflater, container, false);

        controlBotones();
        binding.titulo.setText(titulo);
        binding.subtitulo.setText(subtitulo);
        return binding.getRoot();

    }


    private void controlBotones() {

        binding.accept.setOnClickListener(view -> {
            dismiss();
        });
        binding.cerrar.setOnClickListener(view -> {
           dismiss();
        });
    }


}