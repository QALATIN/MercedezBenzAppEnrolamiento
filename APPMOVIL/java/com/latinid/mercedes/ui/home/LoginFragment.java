package com.latinid.mercedes.ui.home;

import static com.latinid.mercedes.util.OperacionesUtiles.stringCustomDateToday;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.latinid.mercedes.BuildConfig;
import com.latinid.mercedes.DatosRecolectados;
import com.latinid.mercedes.R;

import com.latinid.mercedes.databinding.FragmentLoginBinding;
import com.latinid.mercedes.db.DataBase;
import com.latinid.mercedes.db.model.User;
import com.latinid.mercedes.Main2Activity;
import com.latinid.mercedes.ui.nuevosolicitante.fingerprint.MenuSkipFragment;
import com.latinid.mercedes.util.Conexiones;
import com.latinid.mercedes.util.FingerCapture;
import com.latinid.mercedes.util.GetPost;
import com.latinid.mercedes.util.InputStreamVolleyRequest;
import com.latinid.mercedes.util.UpdateFragment;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintImage;
import cn.com.aratek.fp.FingerprintScanner;
import cn.com.aratek.util.Result;

public class LoginFragment extends FingerCapture {

    private FragmentLoginBinding binding;
    private static final String TAG = "LoginFragment";
    private ExecutorService executor
            = Executors.newFixedThreadPool(3);


    private String userName;
    private String pass;
    private boolean isRunningCapture = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        enterNormal();
        enterWithFinger();
        loginNormalButton();
        binding.resetSensor.setPaintFlags(binding.resetSensor.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        return root;
    }

    private void loginNormalButton() {
        binding.imgGif.setBackgroundResource(R.drawable.loadinggeneral);
        AnimationDrawable frameAnimation = (AnimationDrawable) binding.imgGif.getBackground();
        frameAnimation.start();
        binding.buttonLoginNormal.setOnClickListener(view -> {
            cancelCap = true;
            binding.loginNormal.setVisibility(View.VISIBLE);
            binding.loginFinger.setVisibility(View.INVISIBLE);
        });

        binding.buttonForgot.setOnClickListener(view -> {
            RecoveryPassFragment recoveryPassFragment = new RecoveryPassFragment();
            recoveryPassFragment.setCancelable(true);
            recoveryPassFragment.show(getParentFragmentManager(), "correo");

        });
        binding.resetSensor.setOnClickListener(view -> {

            new Thread(()->{
                try {
                    //onDestroy();
                    onResume();
                    Thread.sleep(1000);
                    enterWithFinger();
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }).start();



        });
    }

    private void enterWithFinger() {
        binding.buttonFinger.setOnClickListener(view -> {
            binding.loginNormal.setVisibility(View.INVISIBLE);
            binding.loginFinger.setVisibility(View.VISIBLE);
            executor.execute(() -> {
                FingerprintImage fi;
                do {
                    int idR2 = Bione.getFreeID();
                    Log.d(TAG, "free id" + idR2);
                    fi = waitFinger();

                    requireActivity().runOnUiThread(() -> {
                        binding.textView6.setText("procesando . . .");
                        binding.buttonLoginNormal.setVisibility(View.INVISIBLE);
                        binding.imgHuella.getLayoutParams().width = 50;
                        binding.imgHuella.getLayoutParams().height = 50;
                        binding.imgHuella.setBackgroundResource(R.drawable.loadinggeneral);
                        binding.imgHuella.setImageBitmap(null);
                        AnimationDrawable frameAnimation = (AnimationDrawable) binding.imgHuella.getBackground();
                        frameAnimation.start();
                    });
                    if (fi == null) {
                        Log.e(TAG, "Se cancelo o fallo la captura");
                        requireActivity().runOnUiThread(() -> {
                            binding.textView6.setText("Coloca tu dedo en el sensor");
                            binding.buttonLoginNormal.setVisibility(View.VISIBLE);
                            binding.imgHuella.setImageResource(R.mipmap.huella);
                            binding.imgHuella.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                            binding.imgHuella.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            binding.imgHuella.setBackgroundResource(0);
                        });
                        break;
                    }
                    byte[] fpFeat = null, fpTemp;
                    Result res;
                    res = Bione.extractFeature(fi);
                    if (res.error != Bione.RESULT_OK) {
                        Log.e(TAG, "Error() Bione.extractFeature(fi)");
                    }
                    fpFeat = (byte[]) res.data;
                    int id = Bione.identify(fpFeat);
                    if (id < 0) {
                        requireActivity().runOnUiThread(() -> {
                            binding.textView6.setText("Sin match, volverlo a intentar");
                            binding.buttonLoginNormal.setVisibility(View.VISIBLE);
                            binding.imgHuella.setImageResource(R.mipmap.huella);
                            binding.imgHuella.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                            binding.imgHuella.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            binding.imgHuella.setBackgroundResource(0);
                        });
                    } else {
                        DataBase dataBase = new DataBase(requireContext());
                        dataBase.open();
                        List<User> users = dataBase.getUsers();
                        dataBase.close();
                        for (User user : users) {
                            if (user.getFinger_id().equals(String.valueOf(id))) {
                                try {
                                    byte[] data = user.getPass().getBytes("UTF-8");
                                    String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                                    base64.replaceAll("\n", "");
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("NombreUsuario", user.getName());
                                    jsonObject.put("Password", base64.replaceAll("\n", ""));
                                    System.out.println(jsonObject);
                                    JSONObject responseLogin = GetPost.crearPost(Conexiones.webServiceGeneral + "/Autenticacion/api/Autenticacion", jsonObject, requireContext());
                                    String token = null;
                                    try {
                                        token = responseLogin.getString("token");
                                        Log.i(TAG, "TOKEN " + responseLogin.getString("token"));
                                    } catch (JSONException ignored) {
                                    }
                                    if (token != null) {
                                        DatosRecolectados.token = token;
                                    }
                                } catch (Throwable e) {
                                    Log.e(TAG, "ERROR ", e);
                                }
                            }
                        }
                        loginSuccess();
                        break;
                    }
                } while (true);
            });
        });
    }

    private ExecutorService executorService2 = Executors.newSingleThreadExecutor();
    int count;

    private void enterNormal() {
        //binding.inputLayoutUser.getEditText().setText("JANTONIO");
        //binding.inputLayoutPass.getEditText().setText("Huerta");
        binding.buttonEnter.setOnClickListener(view -> {
            executor.execute(() -> {
                do{
                    requireActivity().runOnUiThread(() -> {
                        view.setEnabled(false);
                        view.setVisibility(View.INVISIBLE);
                        binding.imgGif.setVisibility(View.VISIBLE);
                    });
                    userName = binding.inputLayoutUser.getEditText().getText().toString();
                    pass = binding.inputLayoutPass.getEditText().getText().toString();
                    if (userName.isEmpty()) {
                        requireActivity().runOnUiThread(() -> {
                            binding.inputLayoutUser.findFocus();
                            binding.inputLayoutUser.requestFocus();
                            view.setEnabled(true);
                            view.setVisibility(View.VISIBLE);
                            binding.imgGif.setVisibility(View.INVISIBLE);
                        });
                        break;
                    }
                    if (pass.isEmpty()) {
                        requireActivity().runOnUiThread(() -> {
                            binding.inputLayoutPass.findFocus();
                            binding.inputLayoutPass.requestFocus();
                            view.setEnabled(true);
                            view.setVisibility(View.VISIBLE);
                            binding.imgGif.setVisibility(View.INVISIBLE);
                        });
                        break;


                    }
                    try {
                        byte[] data = pass.getBytes("UTF-8");
                        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                        base64.replaceAll("\n", "");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("NombreUsuario", userName);
                        jsonObject.put("Password", base64.replaceAll("\n", ""));
                        System.out.println(jsonObject);
                        JSONObject responseLogin = GetPost.crearPost(Conexiones.webServiceGeneral + "/Autenticacion/api/Autenticacion", jsonObject, requireContext());
                        String token = null;
                        try {
                            token = responseLogin.getString("token");
                            Log.i(TAG, "TOKEN " + responseLogin.getString("token"));
                        } catch (JSONException ignored) {
                        }
                        if (token != null) {
                            DatosRecolectados.token = token;
                            User user = verifyUserDB();
                            if (user == null) {
                                registerUser();
                            }
                            closeScanner();
                            loginSuccess();
                            break;
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                view.setEnabled(true);
                                binding.inputLayoutUser.findFocus();
                                binding.inputLayoutUser.requestFocus();
                                binding.inputLayoutUser.setHelperText("Usuario y/o contraseña erroneos");
                                view.setVisibility(View.VISIBLE);
                                binding.imgGif.setVisibility(View.INVISIBLE);
                            });
                            break;
                        }
                    } catch (Throwable e) {
                        requireActivity().runOnUiThread(() -> {
                            view.setEnabled(true);
                            binding.inputLayoutUser.findFocus();
                            binding.inputLayoutUser.requestFocus();
                            binding.inputLayoutUser.setHelperText("Usuario y/o contraseña erroneos");
                            view.setVisibility(View.VISIBLE);
                            binding.imgGif.setVisibility(View.INVISIBLE);
                        });
                        Log.e(TAG, "Throwable() ", e);
                        break;
                    }
                }while (true);


            });

        });
    }

    private void registerUser() {
        requireActivity().runOnUiThread(() -> {
            binding.loginNormal.setVisibility(View.INVISIBLE);
            binding.loginFinger.setVisibility(View.VISIBLE);
            binding.textSubFinger.setText("Capturaremos tu huella, posteriormente podrás iniciar sesión con está");
            binding.buttonLoginNormal.setVisibility(View.INVISIBLE);
        });
        FingerprintImage fi;
        fi = waitFinger();
        requireActivity().runOnUiThread(() -> {
            binding.textView6.setText("procesando . . .");
            binding.buttonLoginNormal.setVisibility(View.INVISIBLE);
            binding.imgHuella.getLayoutParams().width = 50;
            binding.imgHuella.getLayoutParams().height = 50;
            binding.imgHuella.setBackgroundResource(R.drawable.loadinggeneral);
            binding.imgHuella.setImageBitmap(null);
            AnimationDrawable frameAnimation = (AnimationDrawable) binding.imgHuella.getBackground();
            frameAnimation.start();
        });
        if (fi == null) {
            Log.e(TAG, "Se cancelo o fallo la captura");
        }
        byte[] fpFeat = null, fpTemp;
        Result res;
        res = Bione.extractFeature(fi);
        if (res.error != Bione.RESULT_OK) {
            Log.e(TAG, "Error() Bione.extractFeature(fi)");
        }
        fpFeat = (byte[]) res.data;
        res = Bione.makeTemplate(fpFeat, fpFeat, fpFeat);
        if (res.error != Bione.RESULT_OK) {
            Log.e(TAG, "Error() Bione.makeTemplate()");
        }
        fpTemp = (byte[]) res.data;

        int id = Bione.getFreeID();
        Log.d(TAG, "free id" + id);
        if (id < 0) {
            Log.e(TAG, "Base de huellas llena");
        }
        int ret = Bione.enroll(id, fpTemp);
        if (ret != Bione.RESULT_OK) {
            Log.e(TAG, "Error() Bione.enroll()");
        }
        User userRegistration = new User();
        userRegistration.setName(userName);
        userRegistration.setPass(pass);
        userRegistration.setRegistration_date(stringCustomDateToday());
        userRegistration.setActive("true");
        userRegistration.setFinger_id(String.valueOf(id));
        DataBase dataBase = new DataBase(requireContext());
        dataBase.open();
        userRegistration.setUser_id(String.valueOf(dataBase.getUsers().size() + 1));
        dataBase.insertUser(userRegistration);
        dataBase.close();
        Log.i(TAG, "User insertado satisfactoriamente");
    }

    private User verifyUserDB() {
        User user = null;
        DataBase dataBase = new DataBase(requireContext());
        dataBase.open();
        List<User> users = dataBase.getUsers();

        dataBase.close();
        if (!users.isEmpty()) {
            for (User user1 : users) {
                Log.d(TAG, user1.toString());
                if (user1.getName().equals(userName)) {
                    user = user1;
                    break;
                }
            }
        } else {
            int error = Bione.clear();
            if (error == Bione.RESULT_OK) {
                Log.i(TAG, "DB cleaned");
            }
        }
        return user;
    }

    private void loginSuccess() {
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("Authorization", "Bearer " + DatosRecolectados.token);
            JSONObject responseCredentials = GetPost.crearGetHeaders(Conexiones.webServiceGeneral + "/Gateway/api/usuarios/credencial", params, requireContext());
            System.out.println(responseCredentials);
            String nombreCompleto = responseCredentials.getString("nombreCompleto");
            int usuarioId =  responseCredentials.getInt("usuarioId");
            DatosRecolectados.usuarioId = usuarioId;
            DatosRecolectados.nameCompleteUSER = nombreCompleto;
            DatosRecolectados.inSesion = true;

            requireActivity().runOnUiThread(() -> {
                ((Main2Activity) requireActivity()).insertNameComplete(nombreCompleto);
                ((Main2Activity) requireActivity()).activateBar();
                ((Main2Activity) requireActivity()).removerFragmets();
                //requireActivity().getSupportFragmentManager().popBackStack();
            });

        } catch (Throwable e) {
            Log.e(TAG, "Error", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((Main2Activity) getActivity()).desactivateBar();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        onDestroy();
        System.out.println("Si destruí en onDestroyView");
        new Thread(()->{
            do {
                executor.shutdown();
                if (executor.isShutdown()) {
                    break;
                }
            } while (true);
        }).start();
    }



}