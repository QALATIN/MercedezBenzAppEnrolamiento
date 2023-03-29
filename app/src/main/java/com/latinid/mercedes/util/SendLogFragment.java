package com.latinid.mercedes.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentSendlogBinding;
import com.latinid.mercedes.databinding.FragmentUpdateBinding;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import top.ss007.library.DownloadListener;
import top.ss007.library.DownloadUtil;
import top.ss007.library.InputParameter;


public class SendLogFragment extends DialogFragment {


    private FragmentSendlogBinding binding;
    private static final String TAG = "SendLogFragment.class";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSendlogBinding.inflate(inflater, container, false);
        controlBotones();
        binding.imgGif.setBackgroundResource(R.drawable.loadinggeneral);
        binding.imgGif.setImageBitmap(null);
        AnimationDrawable frameAnimation = (AnimationDrawable) binding.imgGif.getBackground();
        frameAnimation.start();
        return binding.getRoot();

    }

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ExecutorService executorResponse = Executors.newSingleThreadExecutor();
    int count;

    private void controlBotones() {

        binding.adjuntar.setOnClickListener(view -> {

            executorService.execute(() -> {
                try {
                    requireActivity().runOnUiThread(() -> {
                        view.setVisibility(View.GONE);
                        binding.imgGif.setVisibility(View.VISIBLE);
                    });
                    String url = Conexiones.webServiceGeneral+"/Gateway/api/bitacoraarchivo";
                    JSONObject jsonObject = new JSONObject();

                    String base64Log = getBase64FromPath(new File(requireContext().getFilesDir()+File.separator+"IDBIOMETRIC"+File.separator+"logInternal.log"));
                    jsonObject.put("Base64",base64Log.replaceAll("\n", "").replace("\\", ""));
                    JSONObject response = GetPost.crearPost(url,jsonObject,requireContext());
                    requireActivity().runOnUiThread(() -> {
                        view.setVisibility(View.GONE);
                        binding.imgGif.setVisibility(View.GONE);
                        binding.titulo.setText("Listo, ya puede cerrar esta ventana");
                    });
                }catch (Throwable e){
                    requireActivity().runOnUiThread(() -> {
                        view.setVisibility(View.GONE);
                        binding.imgGif.setVisibility(View.GONE);
                        binding.titulo.setText("Listo, ya puede cerrar esta ventana");
                    });
                    BinnacleCongif.writeLog(TAG,1, "Error trayendo base64 -> function: getBase64FromPath()", "Error: "+e.getLocalizedMessage(), requireContext());
                    e.printStackTrace();
                }




                //downloadFile();
               /* Intent intent = new Intent(Intent.ACTION_VIEW);//Environment.getExternalStoragePublicDirectory
                Uri photoURI = FileProvider.getUriForFile(requireContext(), getContext().getPackageName() + ".fileprovider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/meche.apk"));
                intent.setDataAndType(photoURI, "application/vnd.android.package-archive .apk");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);*/
            });



        });
        binding.cerrar.setOnClickListener(view -> {
            enviarAccion("cerrar");
        });
    }

    private String getBase64FromPath(File path) {
        String base64 = "";
        try {
            byte[] buffer = new byte[(int) path.length() + 100];
            int length = new FileInputStream(path).read(buffer);
            base64 = Base64.encodeToString(buffer, 0, length,
                    Base64.DEFAULT);
        } catch (IOException e) {
            BinnacleCongif.writeLog(TAG,1, "Error trayendo base64 -> function: getBase64FromPath()", "Error: "+e.getLocalizedMessage(), requireContext());
            e.printStackTrace();
        }
        return base64;
    }

    private void enviarAccion(String accion) {
        Bundle result = new Bundle();
        result.putString("accion", accion);
        getParentFragmentManager().setFragmentResult("modoCaptura", result);
        this.dismiss();
    }

}