package com.latinid.mercedes.util;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import java.io.OutputStream;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import top.ss007.library.DownloadListener;
import top.ss007.library.DownloadUtil;
import top.ss007.library.InputParameter;


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
    ExecutorService executorResponse = Executors.newSingleThreadExecutor();
    int count;

    private void controlBotones() {

        binding.adjuntar.setOnClickListener(view -> {

            executorService.execute(() -> {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);//getFilesDir();
                File futureStudioIconFile = new File(path, "App_mercedes.apk");
                DownloadUtil.getInstance()
                        .downloadFile(new InputParameter.Builder("https://mbfs.latinid.com.mx:9582/Gateway/api/recuperacion/", "getDownloadApk", futureStudioIconFile.getPath())
                                .setCallbackOnUiThread(true)
                                .build(), new DownloadListener() {

                            @Override
                            public void onFinish(final File file) {
                                //you can let this callback run on UI thread by setCallbackOnUiThread(true) in inputParameter
                                binding.titulo.setText("Terminado");
                                installAPK(file,getActivity());
                            }

                            @Override
                            public void onProgress(int progress, long downloadedLengthKb, long totalLengthKb) {
                                binding.titulo.setText("Descargando: " + progress + "%");
                            }

                            @Override
                            public void onFailed(String errMsg) {
                                //you can let this callback run on UI thread by setCallbackOnUiThread(true) in inputParameter
                            }
                        });


                requireActivity().runOnUiThread(() -> {
                    view.setVisibility(View.GONE);
                    binding.imgGif.setVisibility(View.VISIBLE);
                });
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

 /*   private void downloadFile() {
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);

        Call<ResponseBody> call = apiInterface.downloadFileWithFixedUrl();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    if (response.isSuccessful()) {
                        executorResponse.execute(() -> {
                        boolean writtenToDisk = writeResponseBodyToDisk(response.body());

                        Log.d("File download was a success? ", String.valueOf(writtenToDisk));
                        getActivity().runOnUiThread(() -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);//Environment.getExternalStoragePublicDirectory
                            Uri photoURI = FileProvider.getUriForFile(requireContext(), getContext().getPackageName() + ".fileprovider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/meche.apk"));
                            intent.setDataAndType(photoURI, "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        });
                        });
                    }


            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }*/

    /*private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);//getFilesDir();
            File futureStudioIconFile = new File(path, "meche.apk");
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    //Log.d("File Download: " , fileSizeDownloaded + " of " + fileSize);
                }


                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }*/

    public void installAPK(File file, Activity mAct) {
        if (file == null) return;
        String authority = getContext().getPackageName() + ".fileprovider";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data;
        String type = "application/vnd.android.package-archive";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            data = Uri.fromFile(file);
        } else {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            data = FileProvider.getUriForFile(getContext(), authority, file);
        }
        intent.setDataAndType(data, type);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mAct.startActivity(intent);
    }

    private void enviarAccion(String accion) {
        Bundle result = new Bundle();
        result.putString("accion", accion);
        getParentFragmentManager().setFragmentResult("modoCaptura", result);
        this.dismiss();
    }

}