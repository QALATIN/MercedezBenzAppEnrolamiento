package com.latinid.mercedes.ui.nuevosolicitante.selfie.fragments;

import static com.latinid.mercedes.util.OperacionesUtiles.readFile;
import static com.latinid.mercedes.util.OperacionesUtiles.writeToFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.ConditionVariable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aware.face_liveness.api.FaceLiveness;
import com.google.gson.Gson;
import com.latinid.mercedes.DatosRecolectados;

import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentSelfieAwareBinding;
import com.latinid.mercedes.db.DataBase;
import com.latinid.mercedes.db.model.ActiveEnrollment;
import com.latinid.mercedes.model.local.IdentificacionModel;
import com.latinid.mercedes.model.local.PersonaModel;
import com.latinid.mercedes.Main2Activity;
import com.latinid.mercedes.ui.nuevosolicitante.fingerprint.HuellasFragment;
import com.latinid.mercedes.ui.nuevosolicitante.selfie.rest.GetOverrides;
import com.latinid.mercedes.util.GetPost;
import com.latinid.mercedes.util.OperacionesUtiles;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class SelfieAwareFragment extends Fragment implements GetOverrides.GetOverridesListener {

    private static final String TAG = "SelfieAwareFragment";
    private FragmentSelfieAwareBinding binding;
    private FaceLiveness mLivenessApi;
    private boolean mInitComplete = false;

    private String mOverrideJson;
    private ConditionVariable mNetworkTaskComplete = new ConditionVariable();
    private SelfieListener mActionButtonListener;
    public static int esconderFragment = 0;
    private AnimationDrawable frameAnimation;

    public interface SelfieListener {
        // workflowName can be either
        // "Alpha Workflow" or "Bravo Workflow"
        void onWorkflowSelected(String workflowName, String id, String overrideJson);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSelfieAwareBinding.inflate(inflater, container, false);
        binding.gifMercedes.setBackgroundResource(R.drawable.loadinggeneral);
        frameAnimation = (AnimationDrawable) binding.gifMercedes.getBackground();
        frameAnimation.start();
        binding.iniciarCaptura.setVisibility(View.INVISIBLE);
        binding.gifMercedes.setVisibility(View.VISIBLE);
        new Thread(() -> {
            String model = Build.MODEL;
            String strNewModel = model.replaceAll("[^A-Za-z0-9\\_\\-]", "");
            new GetOverrides().getOverrides(SelfieAwareFragment.this, requireContext(), "https://mobileauth.aware-demos.com/faceliveness" + "/" + "deviceConfig", mNetworkTaskComplete, strNewModel);
        }).start();
        binding.iniciarCaptura.setOnClickListener(view -> {
            //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            binding.fragmentContainerr.setVisibility(View.VISIBLE);
            binding.iniciarCaptura.setVisibility(View.INVISIBLE);
            binding.recap.setVisibility(View.INVISIBLE);
            binding.continuee.setVisibility(View.INVISIBLE);
            binding.gifMercedes.setVisibility(View.VISIBLE);
            SelfieAwareFragment.esconderFragment = 0;
            DatosRecolectados.selfieFinish = false;
            mActionButtonListener.onWorkflowSelected(getWorkflowName(), "Latin", mOverrideJson);
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
                        JSONObject jsonObject = generarJsonFaceMatch(DatosRecolectados.selfieB64, identificacion.getFotoRecorteB64().replaceAll("\n", "").replace("\\", ""));
                        //JSONObject jsonResponse = GetPost.crearPost("http://10.10.8.219:8081/nexaface/compare", jsonObject, requireContext());
                        JSONObject jsonResponse = GetPost.crearPost("https://videollamada-api.latinid.com.mx/VideoCall/services/facialmer/procesar", jsonObject, requireContext());
                        //JSONObject jsonResponse = GetPost.crearPost("http://10.10.8.213:8080/VideoCall/services/facialmer/procesar", jsonObject, requireContext());
                        System.out.println(jsonResponse);
                        float responseComparacion = (float) jsonResponse.getDouble("score");
                        if (responseComparacion > 50) {
                            persona.setFotoSelfieB64(DatosRecolectados.selfieB64);
                            persona.setComparacionFacial(String.valueOf(responseComparacion));
                            //persona.setComparacionFacial(String.valueOf("61"));
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
                                ((Main2Activity) getActivity()).replaceFragments(HuellasFragment.class);
                            });
                        } else {
                            getActivity().runOnUiThread(() -> {
                                binding.textoMensaje.setText("Parece que esta persona no es la misma que la identificación, vuelva a intentarlo");
                                binding.iniciarCaptura.setVisibility(View.VISIBLE);
                                binding.gifMercedes.setVisibility(View.INVISIBLE);
                            });
                        }
                    } catch (Throwable e) {
                        Log.e(TAG, "Error() ", e);
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
            binding.fragmentContainerr.setVisibility(View.VISIBLE);
            binding.iniciarCaptura.setVisibility(View.INVISIBLE);
            binding.gifMercedes.setVisibility(View.VISIBLE);
            SelfieAwareFragment.esconderFragment = 0;
            DatosRecolectados.selfieFinish = false;
            mActionButtonListener.onWorkflowSelected(getWorkflowName(), "Latin", mOverrideJson);
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
                    binding.image.setVisibility(View.VISIBLE);
                    binding.image.setImageBitmap(selfieBitmap);
                    binding.recap.setVisibility(View.VISIBLE);
                    binding.continuee.setVisibility(View.VISIBLE);
                    binding.gifMercedes.setVisibility(View.INVISIBLE);
                });
            }


        }).start();
    }

    public static JSONObject generarJsonFaceMatch(String foto1, String foto2) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fotoSelfie",foto1);
            jsonObject.put("voz",foto2);
            jsonObject.put("identificadorId","1");

          /*  jsonObject.put("algorithm", "F500");
            String[] strDocketNoDkt = new String[]{"VISIBLE_FRONTAL"};
            JSONArray jsonArray = new JSONArray();
            jsonArray.put("VISIBLE_FRONTAL");
            jsonObject.put("face_types", jsonArray);
            JSONObject comparator = new JSONObject();
            comparator.put("comparator", jsonObject);
            JSONObject workflow = new JSONObject();
            workflow.put("workflow", comparator);
            JSONObject probe1 = new JSONObject();
            probe1.put("VISIBLE_FRONTAL", foto2);
            JSONObject gale1 = new JSONObject();
            gale1.put("VISIBLE_FRONTAL", foto1);
            workflow.put("probe", probe1);
            workflow.put("gallery", gale1);*/
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void esperarCaptura() {
        new Thread() {
            @Override
            public void run() {
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
            }
        }.start();

    }


    @Override
    public void onGetOverridesStarted() {

    }

    @Override
    public void onGetOverridesComplete() {

    }

    @Override
    public void onGetOverridesDataComplete(int success, String result) {
        if (success == 401) {
        } else {
            mOverrideJson = result;
        }
        binding.gifMercedes.setVisibility(View.INVISIBLE);
        binding.iniciarCaptura.setVisibility(View.VISIBLE);
    }

    private String getWorkflowName() {
        String mWorkflow = "Delta6";
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
            mActionButtonListener = (SelfieListener) context;
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