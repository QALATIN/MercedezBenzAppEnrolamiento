package com.latinid.mercedes.ui.home;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentModoCapturaBinding;
import com.latinid.mercedes.databinding.FragmentPasswordBinding;
import com.latinid.mercedes.util.Conexiones;
import com.latinid.mercedes.util.GetPost;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RecoveryPassFragment extends DialogFragment {

    private FragmentPasswordBinding binding;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPasswordBinding.inflate(inflater,container,false);
        controlBotones();
        binding.imgGif.setBackgroundResource(R.drawable.loadinggeneral);
        AnimationDrawable frameAnimation = (AnimationDrawable) binding.imgGif.getBackground();
        frameAnimation.start();
        return binding.getRoot();
    }

    private void controlBotones(){

        binding.enviarCorreo.setOnClickListener(view -> {
            executorService.execute(() -> {
                try {
                    requireActivity().runOnUiThread(() -> {
                        view.setEnabled(false);
                        view.setVisibility(View.GONE);
                        binding.imgGif.setVisibility(View.VISIBLE);
                    });

                    String correo = binding.correo.getEditText().getText().toString();
                    //correo = "ahuerta@latinid.com.mx";
                    Pattern pattern = Patterns.EMAIL_ADDRESS;
                    Matcher mat = pattern.matcher(correo);
                    if (!mat.find()) {
                        requireActivity().runOnUiThread(() -> {
                            binding.correo.setError("Correo inválido");
                        });
                        return;
                    }
                    //http://10.10.8.240:8080/api/usuarios/sendMail
                    //http://10.10.8.240:8080/api/usuarios/resetPassword
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("CorreoElectronico", correo);
                    JSONObject responseCorreo = GetPost.crearPost(Conexiones.webServiceGeneral+"/Gateway/api/recuperacion/sendMail",jsonObject, requireContext());
                    System.out.println(responseCorreo);
                    requireActivity().runOnUiThread(() -> {
                        binding.imgGif.setVisibility(View.GONE);
                        binding.subtitulo.setText("Coloca tu correo electrónico para restablecer tu contraseña");
                    });
                }catch (Throwable e){
                    e.printStackTrace();
                }
            });
        });
        binding.cerrar.setOnClickListener(view -> {
            this.dismiss();
        });
    }

    private void enviarAccion(String accion){
        Bundle result = new Bundle();
        result.putString("accion", accion);
        getParentFragmentManager().setFragmentResult("correo", result);
        this.dismiss();
    }

}