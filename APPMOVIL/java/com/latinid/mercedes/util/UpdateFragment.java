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
import com.latinid.mercedes.databinding.FragmentModoCapturaBinding;
import com.latinid.mercedes.databinding.FragmentUpdateBinding;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UpdateFragment extends DialogFragment {

    private FragmentUpdateBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUpdateBinding.inflate(inflater, container, false);

        controlBotones();
        binding.imgGif.setBackgroundResource(R.drawable.loadinggeneral);
        binding.imgGif.setImageBitmap(null);
        AnimationDrawable frameAnimation = (AnimationDrawable) binding.imgGif.getBackground();
        frameAnimation.start();
        return binding.getRoot();

    }

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    int count;

    private void controlBotones() {

        binding.adjuntar.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Conexiones.webServiceGeneral+"/Gateway/api/recuperacion/getDownloadApk"));
            startActivity(browserIntent);
            /*executorService.execute(() -> {
                requireActivity().runOnUiThread(() -> {
                    view.setVisibility(View.GONE);
                    binding.imgGif.setVisibility(View.VISIBLE);
                });
                System.out.println("Entre");
                String mUrl = Conexiones.webServiceGeneral + "/Gateway/api/recuperacion/getDownloadApk";
                InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                        response -> {
                            System.out.println("sali");
                            // TODO handle the response
                            HashMap<String, Object> map = new HashMap<String, Object>();
                            try {
                                if (response != null) {
                                    try {
                                        long lenghtOfFile = response.length;
                                        //covert reponse to input stream
                                        InputStream input = new ByteArrayInputStream(response);
                                        File path = requireContext().getDataDir();//getFilesDir();
                                        File file = new File(path, "meche.apk");
                                        map.put("resume_path", file.toString());
                                        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
                                        byte data[] = new byte[1024];

                                        long total = 0;

                                        while ((count = input.read(data)) != -1) {
                                            total += count;
                                            output.write(data, 0, count);
                                        }

                                        output.flush();
                                        output.close();
                                        input.close();

                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        Uri photoURI = FileProvider.getUriForFile(requireContext(), getContext().getPackageName() + ".fileprovider", new File(requireContext().getFilesDir() + "/meche.apk"));
                                        intent.setDataAndType(photoURI, "application/vnd.android.package-archive");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                        requireActivity().runOnUiThread(() -> {
                                            view.setVisibility(View.GONE);
                                            binding.imgGif.setVisibility(View.GONE);
                                        });

                                        System.out.println("Guardado");

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        System.out.println("sali null");
                                    }
                                } else {
                                    System.out.println("sali null");
                                }
                            } catch (Throwable e) {
                                System.out.println("sali error");
                                e.printStackTrace();
                                // TODO Auto-generated catch block
                                // Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");


                            }
                        }, error -> {

                    System.out.println(new Gson().toJson(error));
                    System.out.println("sali error");

                }, null);

                RequestQueue mRequestQueue = Volley.newRequestQueue(requireContext(), new HurlStack());
                mRequestQueue.add(request);
                System.out.println("procesando");
            });*/
        });
        binding.cerrar.setOnClickListener(view -> {
            enviarAccion("cerrar");
        });
    }

    private void enviarAccion(String accion) {
        Bundle result = new Bundle();
        result.putString("accion", accion);
        getParentFragmentManager().setFragmentResult("modoCaptura", result);
        this.dismiss();
    }

}