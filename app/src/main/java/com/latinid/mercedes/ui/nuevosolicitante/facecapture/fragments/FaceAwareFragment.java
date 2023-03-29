package com.latinid.mercedes.ui.nuevosolicitante.facecapture.fragments;

import static com.latinid.mercedes.util.OperacionesUtiles.readFile;
import static com.latinid.mercedes.util.OperacionesUtiles.writeToFile;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.latinid.mercedes.DatosRecolectados;

import com.latinid.mercedes.Main3Activity;
import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentFaceAwareBinding;
import com.latinid.mercedes.databinding.FragmentSelfieAwareBinding;
import com.latinid.mercedes.db.DataBase;
import com.latinid.mercedes.db.model.ActiveEnrollment;
import com.latinid.mercedes.model.local.IdentificacionModel;
import com.latinid.mercedes.model.local.PersonaModel;
import com.latinid.mercedes.ui.nuevosolicitante.fingerprint.HuellasFragment;

import com.latinid.mercedes.ui.nuevosolicitante.selfie.rest.GetOverrides;
import com.latinid.mercedes.util.BinnacleCongif;
import com.latinid.mercedes.util.Conexiones;
import com.latinid.mercedes.util.GetPost;
import com.latinid.mercedes.util.OperacionesUtiles;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class FaceAwareFragment extends Fragment {

    private static final String TAG = "FaceAwareFragment.class";

    private FragmentFaceAwareBinding binding;
    private boolean mInitComplete = false;

    private String mOverrideJson;
    private ConditionVariable mNetworkTaskComplete = new ConditionVariable();
    private FaceListener mActionButtonListener;
    public static int esconderFragment = 0;
    private AnimationDrawable frameAnimation;

    public interface FaceListener {
        // workflowName can be either
        // "Alpha Workflow" or "Bravo Workflow"
        void onWorkflowSelected(String workflowName, String id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFaceAwareBinding.inflate(inflater, container, false);
        binding.gifMercedes.setBackgroundResource(R.drawable.loadinggeneral);
        frameAnimation = (AnimationDrawable) binding.gifMercedes.getBackground();
        frameAnimation.start();
        binding.iniciarCaptura.setVisibility(View.VISIBLE);
        binding.gifMercedes.setVisibility(View.INVISIBLE);

       /* new Thread(() -> {
            String model = Build.MODEL;
            String strNewModel = model.replaceAll("[^A-Za-z0-9\\_\\-]", "");
            new GetOverrides().getOverrides(FaAwareFragment.this, requireContext(), "https://mobileauth.aware-demos.com/faceliveness" + "/" + "deviceConfig", mNetworkTaskComplete, strNewModel);
        }).start();*/

        binding.iniciarCaptura.setOnClickListener(view -> {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            binding.fragmentContainerr.setVisibility(View.VISIBLE);
            binding.iniciarCaptura.setVisibility(View.INVISIBLE);
            binding.recap.setVisibility(View.INVISIBLE);
            binding.continuee.setVisibility(View.INVISIBLE);
            binding.gifMercedes.setVisibility(View.VISIBLE);
           esconderFragment = 0;
            DatosRecolectados.selfieFinish = false;
            mActionButtonListener.onWorkflowSelected(getWorkflowName(), "Latin");
            esperarCaptura();

        });
        buttonContinue();
        recapFace();
        return binding.getRoot();
    }

    private void buttonContinue(){
        binding.continuee.setOnClickListener(view -> {
            new Thread(() -> {
                requireActivity().runOnUiThread(() -> {
                    binding.recap.setVisibility(View.INVISIBLE);
                    binding.continuee.setVisibility(View.INVISIBLE);
                    binding.gifMercedes.setVisibility(View.VISIBLE);
                });
                if (!DatosRecolectados.proofLifeSelfie) {
                    getActivity().runOnUiThread(() -> {
                        binding.textoMensaje.setText("No se pudo procesar correctamente la foto, por favor inténtelo de nuevo");
                        binding.iniciarCaptura.setVisibility(View.VISIBLE);
                        binding.gifMercedes.setVisibility(View.INVISIBLE);
                    });
                    return;
                }

                if (!DatosRecolectados.selfieB64.equals("")) {
                    Bitmap selfieBitmap = OperacionesUtiles.generarBitmapFromBase64(DatosRecolectados.selfieB64);
                    getActivity().runOnUiThread(() -> {
                        binding.image.setVisibility(View.VISIBLE);
                        binding.image.setImageBitmap(selfieBitmap);
                    });
                    try {
                        IdentificacionModel identificacion = new Gson().fromJson(readFile(new File(DatosRecolectados.activeEnrollment.getJson_ident())), IdentificacionModel.class);
                        PersonaModel persona = new Gson().fromJson(readFile(new File(DatosRecolectados.activeEnrollment.getJson_pers())), PersonaModel.class);
                        JSONObject jsonObject = generarJsonFaceMatch(DatosRecolectados.selfieB64, identificacion.getFotoRecorteB64().replaceAll("\n", "").replace("\\", ""), requireContext());
                        //JSONObject jsonResponse = GetPost.crearPost("http://10.10.8.219:8081/nexaface/compare", jsonObject, requireContext());
                        //JSONObject jsonResponse = GetPost.crearPost("https://videollamada-api.latinid.com.mx/VideoCall/services/facialmer/procesar", jsonObject, requireContext());
                        HashMap<String, String> params = new HashMap<>();
                        params.put("Authorization", "Bearer " + DatosRecolectados.token);
                        JSONObject jsonResponse = GetPost.crearPostHeaders(Conexiones.webServiceGeneral+"/Gateway/api/paquetes/ComparacionFacial", jsonObject,params, requireContext());
                        //JSONObject jsonResponse = GetPost.crearPost("http://10.10.8.213:8080/VideoCall/services/facialmer/procesar", jsonObject, requireContext());
                        float responseComparacion = (float) jsonResponse.getDouble("score");
                        float responseComparacion2 = (float) jsonResponse.getDouble("score_percent");
                        if (responseComparacion2 > 50) {
                            persona.setFotoSelfieB64(DatosRecolectados.selfieB64);
                            persona.setComparacionFacial(String.valueOf(responseComparacion2));
                            persona.setPruebaDeVida("true");
                            String jsonPers = new Gson().toJson(persona);
                            String persPath = writeToFile(requireActivity().getFilesDir(), jsonPers);
                            ActiveEnrollment activeEnrollment = DatosRecolectados.activeEnrollment;
                            File filePersonaAnterior = new File(activeEnrollment.getJson_pers());
                            filePersonaAnterior.delete();
                            activeEnrollment.setJson_pers(persPath);
                            activeEnrollment.setIdent_id("2");
                            activeEnrollment.setState_id("3");
                            DataBase dataBase = new DataBase(requireContext());
                            dataBase.open();
                            dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
                            dataBase.close();
                            DatosRecolectados.activeEnrollment = activeEnrollment;
                            getActivity().runOnUiThread(() -> {
                                binding.gifMercedes.setVisibility(View.INVISIBLE);
                                ((Main3Activity) getActivity()).replaceFragments(HuellasFragment.class);
                            });
                        } else {
                            getActivity().runOnUiThread(() -> {
                                binding.textoMensaje.setText("Parece que esta persona no es la misma que la identificación, vuelva a intentarlo");
                                binding.iniciarCaptura.setVisibility(View.VISIBLE);
                                binding.gifMercedes.setVisibility(View.INVISIBLE);
                            });
                        }
                    } catch (Throwable e) {
                        BinnacleCongif.writeLog(TAG,1, "Error guardando foto de cara -> function: buttonContinue()", "Error: "+e.getLocalizedMessage(), requireContext());
                        e.printStackTrace();
                    }
                } else {
                    getActivity().runOnUiThread(() -> {
                        binding.textoMensaje.setText("No se pudo procesar correctamente la foto, por favor inténtelo de nuevo");
                        binding.iniciarCaptura.setVisibility(View.VISIBLE);
                        binding.gifMercedes.setVisibility(View.INVISIBLE);
                    });
                }
            }).start();
        });
    }

    private void recapFace(){
        binding.recap.setOnClickListener(view -> {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            binding.fragmentContainerr.setVisibility(View.VISIBLE);
            binding.iniciarCaptura.setVisibility(View.INVISIBLE);
            binding.gifMercedes.setVisibility(View.VISIBLE);
            esconderFragment = 0;
            DatosRecolectados.selfieFinish = false;
            mActionButtonListener.onWorkflowSelected(getWorkflowName(), "Latin");
            esperarCaptura();
        });

    }

    private void esperarResultados() {
        new Thread(() -> {
            do {
                try {
                    Thread.sleep(500);
                    if (DatosRecolectados.selfieFinish) {
                        break;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } while (true);

            if (!DatosRecolectados.selfieB64.equals("")) {
                Bitmap selfieBitmap = OperacionesUtiles.generarBitmapFromBase64(DatosRecolectados.selfieB64);
                requireActivity().runOnUiThread(() -> {
                    binding.textoMensaje.setText("¿Se ve bien la fotografía, la iluminación es buena y se ve el rostro completo?");
                    binding.image.setVisibility(View.VISIBLE);
                    binding.image.setImageBitmap(selfieBitmap);
                    binding.recap.setVisibility(View.VISIBLE);
                    binding.continuee.setVisibility(View.VISIBLE);
                    binding.gifMercedes.setVisibility(View.INVISIBLE);
                });
            }


        }).start();
    }

    public static JSONObject generarJsonFaceMatch(String foto1, String foto2, Context context) {
        try {
            JSONObject jsonObject = new JSONObject();
           /*  jsonObject.put("fotoSelfie",foto1);
            jsonObject.put("voz",foto2);
            jsonObject.put("identificadorId","1");*/
            jsonObject.put("imagenCredencial",foto2);
            jsonObject.put("imagenCamara",foto1);
            return jsonObject;
        } catch (JSONException e) {
            BinnacleCongif.writeLog(TAG,1, "Error generando JSON -> function: generarJsonFaceMatch()", "Error: "+e.getLocalizedMessage(), context);
            e.printStackTrace();
            return null;
        }
    }

    private void esperarCaptura() {
        new Thread(() -> {
            do {
                try {
                    Thread.sleep(500);
                    if (esconderFragment == 1) {
                        getActivity().runOnUiThread(() -> {
                            binding.fragmentContainerr.setVisibility(View.INVISIBLE);
                            esperarResultados();
                        });
                        break;
                    }
                    if (esconderFragment == 2) {
                        getActivity().runOnUiThread(() -> {
                            binding.fragmentContainerr.setVisibility(View.INVISIBLE);
                            binding.iniciarCaptura.setVisibility(View.VISIBLE);
                            binding.gifMercedes.setVisibility(View.INVISIBLE);
                        });
                        break;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } while (true);
        }).start();

    }


    private String getWorkflowName() {
        String mWorkflow = "Foxtrot";
        //String mWorkflow = "Charlie2";
        return mWorkflow;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        frameAnimation.stop();
        Log.d("SelfieAware", "Si destrui el fragment");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mActionButtonListener = (FaceListener) context;
        } catch (ClassCastException e) {
            Log.e("onAttach", "ActionButtonListener not implemented");
            throw new ClassCastException(e.toString() + " implement ActionButtonListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActionButtonListener = null;
    }
}
