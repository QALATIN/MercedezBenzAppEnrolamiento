package com.latinid.mercedes.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.latinid.mercedes.DatosRecolectados;


import com.latinid.mercedes.databinding.FragmentHomeBinding;
import com.latinid.mercedes.ui.applicants.SubMenuFragment;
import com.latinid.mercedes.Main3Activity;
import com.latinid.mercedes.ui.nuevosolicitante.privacypolicy.AvisoFragment;
import com.latinid.mercedes.util.BinnacleCongif;
import com.latinid.mercedes.util.Conexiones;
import com.latinid.mercedes.util.InputStreamVolleyRequest;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final String TAG = "HomeFragment";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.botonConsulta.setOnClickListener(view -> {
            ((Main3Activity) getActivity()).replaceFragments(SubMenuFragment.class);
        });
        binding.botonNuevo.setOnClickListener(view -> {
            ((Main3Activity) getActivity()).replaceFragments(AvisoFragment.class);
        });
        if(!DatosRecolectados.inSesion){
            ((Main3Activity) getActivity()).replaceFragments(LoginFragment.class);
        }
        insertName();
        //BinnacleCongif.writeLog("Main",1, "Iniciando log . . .", "Log init", requireContext());
        return root;
    }

    public void insertName(){
        executorService.execute(() -> {
            do{
                if(!DatosRecolectados.nameCompleteUSER.equals("")){
                        getActivity().runOnUiThread(() -> {
                            binding.nameUser.setText("¡Bienvenido!");
                        });
                    break;
                }
            }while (true);
        });
    }

    @Override
    public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs, @Nullable Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}