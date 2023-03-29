package com.latinid.mercedes.ui.nuevosolicitante;

import static com.latinid.mercedes.util.OperacionesUtiles.dateEnrollment;
import static com.latinid.mercedes.util.OperacionesUtiles.dateFormat2;
import static com.latinid.mercedes.util.OperacionesUtiles.fechaFormada;
import static com.latinid.mercedes.util.OperacionesUtiles.generarTXTJson;
import static com.latinid.mercedes.util.OperacionesUtiles.readFile;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.latinid.mercedes.DatosRecolectados;

import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentSendInfoBinding;
import com.latinid.mercedes.db.DataBase;
import com.latinid.mercedes.db.model.ActiveEnrollment;
import com.latinid.mercedes.model.local.DocModel;
import com.latinid.mercedes.model.local.FingerModel;
import com.latinid.mercedes.model.local.IdentificacionModel;
import com.latinid.mercedes.model.local.PersonaModel;
import com.latinid.mercedes.model.local.SignatureModel;
import com.latinid.mercedes.model.local.SolicitanteModel;
import com.latinid.mercedes.ui.applicants.CompleteProcessFragment;
import com.latinid.mercedes.Main2Activity;
import com.latinid.mercedes.util.Conexiones;
import com.latinid.mercedes.util.GetPost;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SendInfoFragment extends Fragment {

    private FragmentSendInfoBinding binding;
    private static final String TAG = "SendInfoFragment";

    private AnimationDrawable frameAnimation;
    private ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSendInfoBinding.inflate(inflater, container, false);
        sendPackage();
        saveFolio();
        try {
            if(DatosRecolectados.activeEnrollment.getFolio() != null || !DatosRecolectados.activeEnrollment.getFolio().equals("")){
                binding.outlinedFol.getEditText().setText(DatosRecolectados.activeEnrollment.getFolio());
            }
        }catch (Throwable ignored){}

        return binding.getRoot();
    }

    private void sendPackage() {

        binding.sendPack.setOnClickListener(view -> {
            executor.execute(() -> {
                requireActivity().runOnUiThread(() -> {
                    view.setEnabled(false);
                    binding.outlinedFol.setError(null);
                    binding.outlinedFol.setEnabled(false);
                    binding.sendPack.setEnabled(false);
                    binding.gifMercedes.setVisibility(View.VISIBLE);
                    binding.gifMercedes.setBackgroundResource(R.drawable.loadinggeneral);
                    frameAnimation = (AnimationDrawable) binding.gifMercedes.getBackground();
                    frameAnimation.start();
                });
                String folio = binding.outlinedFol.getEditText().getText().toString();
                if(folio.length() != 13){
                    requireActivity().runOnUiThread(() -> {
                        view.setEnabled(true);
                        binding.outlinedFol.setEnabled(true);
                        binding.sendPack.setEnabled(true);
                        binding.gifMercedes.setVisibility(View.INVISIBLE);
                        binding.outlinedFol.setError("Se necesita un folio válido");
                    });
                    return;
                }
                ActiveEnrollment activeEnrollment = DatosRecolectados.activeEnrollment;
                activeEnrollment.setFolio(folio);
                activeEnrollment.setState_id("8");

                DataBase dataBase = new DataBase(requireContext());
                dataBase.open();
                dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
                dataBase.close();
                IdentificacionModel identificacion = new Gson().fromJson(readFile(new File(DatosRecolectados.activeEnrollment.getJson_ident())), IdentificacionModel.class);
                PersonaModel persona = new Gson().fromJson(readFile(new File(DatosRecolectados.activeEnrollment.getJson_pers())), PersonaModel.class);
                SignatureModel signatureModel = new Gson().fromJson(readFile(new File(DatosRecolectados.activeEnrollment.getJson_signature())), SignatureModel.class);
                FingerModel fingerModel = new Gson().fromJson(readFile(new File(DatosRecolectados.activeEnrollment.getJson_finger())), FingerModel.class);
                DocModel docModel = new Gson().fromJson(readFile(new File(DatosRecolectados.activeEnrollment.getJson_docs())), DocModel.class);

                SolicitanteModel solicitanteModel = new SolicitanteModel();
                solicitanteModel.setIdentificadorCiudadano(identificacion.getCIC());
                solicitanteModel.setIneNumeroDeEmision(identificacion.getNumeroDeEmision());
                solicitanteModel.setDocumentoPdfBase64(identificacion.getDocumentoPdfBase64().replaceAll("\n", "").replace("\\", ""));
                solicitanteModel.setFolio(folio);
                solicitanteModel.setEstatus("Nueva");
                solicitanteModel.setTipoCliente(activeEnrollment.getTipo_enroll());
                solicitanteModel.setNombre(persona.getNombre());
                solicitanteModel.setPaterno(persona.getPaterno());
                solicitanteModel.setMaterno(persona.getMaterno());
                try {
                    String fechaFormada = dateFormat2(persona.getFechaDeNacimiento());
                    if (fechaFormada == null) {
                        solicitanteModel.setFechaDeNacimiento(fechaFormada());
                    } else {
                        solicitanteModel.setFechaDeNacimiento(fechaFormada);
                    }
                } catch (Exception e) {
                    solicitanteModel.setFechaDeNacimiento(fechaFormada());
                    e.printStackTrace();
                }
                solicitanteModel.setEmision(identificacion.getNumeroDeEmision());
                solicitanteModel.setSerie(identificacion.getSerie());
                solicitanteModel.setDireccionCompleta(persona.getDomicilioCompleto());
                solicitanteModel.setLugarDeNacimiento("D.f");
                solicitanteModel.setCurp(persona.getCurp());
                solicitanteModel.setCorreoElectronico(persona.getCorreo());
                solicitanteModel.setTelefono(persona.getTelefono());
                solicitanteModel.setNombreCompletoDelSolicitante(persona.getNombre() + " " + persona.getPaterno() + " " + persona.getMaterno());
                solicitanteModel.setSexo(persona.getSexo());
                solicitanteModel.setScoreDeLaComparacionFacial(persona.getComparacionFacial());
                solicitanteModel.setResultadoDeLaComparacionFacial("true");
                solicitanteModel.setFotoEnBase64(persona.getFotoSelfieB64().replaceAll("\n", "").replace("\\", ""));
                //solicitanteModel.setSelfieBase64(persona.getFotoSelfieB64().replaceAll("\n", "").replace("\\", ""));

                try {
                    solicitanteModel.setCapturaFrenteEnBase64(identificacion.getCapturaIdentificacionFrente().replaceAll("\n", "").replace("\\", ""));
                }catch (Throwable ignored){
                    solicitanteModel.setCapturaFrenteEnBase64("");
                }

                try {
                    solicitanteModel.setCapturaReversoEnBase64(identificacion.getCapturaIdentificacionReverso().replaceAll("\n", "").replace("\\", ""));
                }catch (Throwable ignored){
                    solicitanteModel.setCapturaReversoEnBase64("");
                }

                solicitanteModel.setFotoDeIdentificacionEnBase64(identificacion.getFotoRecorteB64().replaceAll("\n", "").replace("\\", ""));
                solicitanteModel.setTipoDeDocumento(identificacion.getTipoDeIdentificacion());
                solicitanteModel.setNumeroDeDocumento(identificacion.getOCR());
                solicitanteModel.setUsuarioId(DatosRecolectados.usuarioId);
                solicitanteModel.setFechaDeRegistro(fechaFormada());
                String uuid = UUID.randomUUID().toString();
                solicitanteModel.setGuid(uuid);
                solicitanteModel.setAnioRegistro(identificacion.getFechaDeRegistro());

                if (identificacion.getTipoDeIdentificacion().equals("PASAPORTE CON CHIP")) {
                    solicitanteModel.setNumeroDeDocumento(identificacion.getNumeroDeDocumento());
                    if (identificacion.getResultado() == 1) {
                        solicitanteModel.setResultadoGeneral("Passed");
                    } else {
                        solicitanteModel.setResultadoGeneral("Failed");
                    }
                } else {
                    switch (identificacion.getResult().result) {
                        case "1":
                            solicitanteModel.setResultadoGeneral("Passed");
                            break;
                        case "2":
                            solicitanteModel.setResultadoGeneral("Failed");
                            break;
                        case "3":
                            solicitanteModel.setResultadoGeneral("Skipped");
                            break;
                        case "4":
                            solicitanteModel.setResultadoGeneral("Caution");
                            break;
                        case "5":
                            solicitanteModel.setResultadoGeneral("Attention");
                            break;
                        default:
                            solicitanteModel.setResultadoGeneral("Failed");
                    }
                }
                solicitanteModel.setClaveElector(identificacion.getElectorNumber());
                solicitanteModel.setCalleNumero(persona.getCalle_Numero());
                solicitanteModel.setColonia(persona.getColonia());
                solicitanteModel.setMunicipio(persona.getMunicipio());
                solicitanteModel.setCodigoPostal(persona.getCodigoPostal());
                solicitanteModel.setCIC(identificacion.getCIC());
                solicitanteModel.setOCR(identificacion.getOCR());
                solicitanteModel.setEdad(persona.getEdad());
                solicitanteModel.setMrz(identificacion.getMRZ());
                solicitanteModel.setPruebaDeVida("true");
                solicitanteModel.setCoordenadasGps(DatosRecolectados.coors);
                System.out.println(activeEnrollment);
                System.out.println(activeEnrollment.getSolicitante_id());
                if(!activeEnrollment.getTipo_enroll().equals("Nueva")){
                    solicitanteModel.setSolicitanteId(Integer.parseInt(activeEnrollment.getSolicitante_id()));
                }

                String json = new Gson().toJson(solicitanteModel);
                generarTXTJson(requireContext(), "solicitante.json", json.toString());
                try {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("Authorization", "Bearer " + DatosRecolectados.token);
                    JSONObject jsonObject = new JSONObject(json);
                    Log.d(TAG,"Token "+DatosRecolectados.token);
                    JSONObject jsonObject1 = GetPost.crearPostHeaders(Conexiones.webServiceGeneral+"/Gateway/api/paquetes/solicitantes", jsonObject,params, requireContext());
                    //JSONObject jsonObject1 = GetPost.crearPostHeaders("http://10.10.17.47:22266/api/paquetes/solicitantes", jsonObject,params, requireContext());
                    System.out.println(jsonObject1);
                    int idSolicitante = jsonObject1.getInt("solicitanteId");
                    //sendAviso(idSolicitante,identificacion.getDocumentoPdfBase64(),2,signatureModel.getReference());
                    sendFingers(idSolicitante, fingerModel.getThumbRight().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, 1);
                    sendFingers(idSolicitante, fingerModel.getIndexRight().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, 2);
                    sendFingers(idSolicitante, fingerModel.getMiddleRight().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, 3);
                    sendFingers(idSolicitante, fingerModel.getRingRight().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, 4);
                    sendFingers(idSolicitante, fingerModel.getLittleRight().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, 5);
                    sendFingers(idSolicitante, fingerModel.getThumbLeft().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, 6);
                    sendFingers(idSolicitante, fingerModel.getIndexLeft().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, 7);
                    sendFingers(idSolicitante, fingerModel.getMiddleLeft().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, 8);
                    sendFingers(idSolicitante,fingerModel.getRingLeft().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, 9);
                    sendFingers(idSolicitante, fingerModel.getLittleLeft().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, 10);
                    sendDocs(idSolicitante, docModel.getDocAndress().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, docModel.getTypeAndress(), "2");
                    sendDocs(idSolicitante, docModel.getDocBanking().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, docModel.getTypeBanking(),"3");
                    sendDocs(idSolicitante, docModel.getDocIncome().replaceAll("\n", "").replace("\\", ""), DatosRecolectados.usuarioId, docModel.getTypeIncome(),"1");
                    sendAviso(idSolicitante,signatureModel.getBase64DocSignature(),DatosRecolectados.usuarioId,signatureModel.getReference());
                    //sendAviso(idSolicitante,DatosRecolectados.pdfToSignTemp,DatosRecolectados.usuarioId,"NA");

                    activeEnrollment.setSolicitante_id(String.valueOf(idSolicitante));
                    activeEnrollment.setState_id("9");
                    dataBase.open();
                    dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
                    dataBase.close();
                    requireActivity().runOnUiThread(() -> {
                        ((Main2Activity) getActivity()).replaceFragments(CompleteProcessFragment.class);
                    });

                } catch (Throwable e) {
                    Log.e(TAG,"Error,", e);
                }


            });
        });
    }

    private void saveFolio() {
        binding.saveFolio.setOnClickListener(view -> {
            executor.execute(() -> {
                requireActivity().runOnUiThread(() -> {
                    view.setEnabled(false);
                    binding.outlinedFol.setEnabled(false);
                    binding.sendPack.setEnabled(false);
                    binding.gifMercedes.setVisibility(View.VISIBLE);
                    binding.gifMercedes.setBackgroundResource(R.drawable.loadinggeneral);
                    frameAnimation = (AnimationDrawable) binding.gifMercedes.getBackground();
                    frameAnimation.start();
                });
                String folio = binding.outlinedFol.getEditText().getText().toString();
                ActiveEnrollment activeEnrollment = DatosRecolectados.activeEnrollment;
                activeEnrollment.setFolio(folio);
                activeEnrollment.setDate(dateEnrollment());
                activeEnrollment.setState_id("8");

                DataBase dataBase = new DataBase(requireContext());
                dataBase.open();
                dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
                dataBase.close();

                requireActivity().runOnUiThread(() -> {
                    ((Main2Activity) getActivity()).replaceFragments(CompleteProcessFragment.class);
                });
            });
        });
    }

    private void sendAviso(int solicitanteId, String doc, int userId, String Referencia) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Id", 0);
            jsonObject.put("SolicitanteId", solicitanteId);
            jsonObject.put("UsuarioId", userId);
            jsonObject.put("FechaDeRegistro", fechaFormada());
            jsonObject.put("Referencia", Referencia);
            jsonObject.put("DocumentoBase64", doc.replaceAll("\n","").replace("\\",""));
            generarTXTJson(requireContext(), "priv.json", jsonObject.toString());
            HashMap<String, String> params = new HashMap<>();
            params.put("Authorization", "Bearer " + DatosRecolectados.token);
            JSONObject jsonObject1 = GetPost.crearPostHeaders(Conexiones.webServiceGeneral+"/Gateway/api/paquetes/avisoprivacidad", jsonObject, params, requireContext());
            System.out.println(jsonObject1);
        } catch (Throwable e) {
            Log.e(TAG, "Error()", e);
        }
    }


    private void sendDocs(int solicitanteId, String doc, int userId, String TipoDocumento, String nombreDoc) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Id", 0);
            jsonObject.put("SolicitanteId", solicitanteId);
            jsonObject.put("UsuarioId", userId);
            jsonObject.put("FechaDeRegistro", fechaFormada());
            jsonObject.put("NombreDocumento", nombreDoc);
            jsonObject.put("TipoDocumento", TipoDocumento);//NombreDocumento
            jsonObject.put("DocumentoBase64", doc.replaceAll("\n", "").replace("\\", ""));
            generarTXTJson(requireContext(), "doc.txt", jsonObject.toString());
            HashMap<String, String> params = new HashMap<>();
            params.put("Authorization", "Bearer " + DatosRecolectados.token);
            JSONObject jsonObject1 = GetPost.crearPostHeaders(Conexiones.webServiceGeneral+"/Gateway/api/paquetes/documentos", jsonObject, params,requireContext());
            System.out.println(jsonObject1);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void sendFingers(int solicitanteId, String finger, int userId, int fingerId) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Id", 0);
            jsonObject.put("SolicitanteId", solicitanteId);
            jsonObject.put("UsuarioId", userId);
            jsonObject.put("FechaDeRegistro", fechaFormada());
            jsonObject.put("DedoId", fingerId);

            if (finger.equals("na")) {
                jsonObject.put("OmisionId", 4);
            } else if (finger.equals("amputado")) {
                jsonObject.put("OmisionId", 3);
            } else if (finger.equals("vendado")) {
                jsonObject.put("OmisionId", 2);
            } else {
                jsonObject.put("OmisionId", 1);
            }
            jsonObject.put("HuellaBase64", finger.replaceAll("\n", "").replace("\\", ""));
            generarTXTJson(requireContext(), "huellas.txt", jsonObject.toString());
            HashMap<String, String> params = new HashMap<>();
            params.put("Authorization", "Bearer " + DatosRecolectados.token);
            JSONObject jsonObject1 = GetPost.crearPostHeaders(Conexiones.webServiceGeneral+"/Gateway/api/paquetes/huellas", jsonObject, params, requireContext());
            System.out.println(jsonObject1);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}