package com.latinid.mercedes.ui.nuevosolicitante.archivos;

import static com.latinid.mercedes.util.OperacionesUtiles.writeToFile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.latinid.mercedes.DatosRecolectados;

import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentDocsBinding;
import com.latinid.mercedes.db.DataBase;
import com.latinid.mercedes.db.model.ActiveEnrollment;
import com.latinid.mercedes.model.local.DocModel;
import com.latinid.mercedes.ui.nuevosolicitante.ReviewFragment;
import com.latinid.mercedes.Main2Activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DocsFragment extends Fragment {

    private FragmentDocsBinding binding;
    private File fileTemporal;
    private static final int CAPTURA_DOMICILIO = 1;
    private static final int CAPTURA_BANCARIO = 2;
    private static final int CAPTURA_INGRESOS = 3;
    private int estadoDocumento = 0;
    private File fotoDomicilio;
    private File fotoBancario;
    private File fotoIngresos;
    private Uri uriDomicilio;
    private Uri uriBancario;
    private Uri uriIngresos;
    private boolean domicilioCapturado = false;
    private boolean bancarioCapturado = false;
    private boolean ingresosCapturado = false;
    private String domicilioDOC="";
    private String bancarioDOC="";
    private String ingresosDOC="";
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private AnimationDrawable frameAnimation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDocsBinding.inflate(inflater, container, false);
        botonesCaptura();
        botonesVerArchivos();
        botonesQuitarDocumento();
        binding.siguiente.setOnClickListener(view -> {

            executor.execute(() -> {
                requireActivity().runOnUiThread(() -> {
                    view.setVisibility(View.INVISIBLE);
                    binding.gifMercedes.setBackgroundResource(R.drawable.loadinggeneral);
                    frameAnimation = (AnimationDrawable) binding.gifMercedes.getBackground();
                    frameAnimation.start();
                    binding.gifMercedes.setVisibility(View.VISIBLE);
                    binding.chipIngresos.setEnabled(false);
                    binding.capturaBancario.setEnabled(false);
                    binding.chipDomicilio.setEnabled(false);
                    binding.verDomicilio.setEnabled(false);
                    binding.verIngresos.setEnabled(false);
                    binding.verBancario.setEnabled(false);
                });

                DocModel docModel = new DocModel();
                docModel.setDocAndress(DatosRecolectados.docB64Address);
                docModel.setDocIncome(DatosRecolectados.docB64Income);
                docModel.setDocBanking(DatosRecolectados.docB64Banking);
                docModel.setTypeAndress(DatosRecolectados.fileTypeAddress);
                docModel.setTypeIncome(DatosRecolectados.fileTypeIncome);
                docModel.setTypeBanking(DatosRecolectados.fileTypeBanking);

                String json = new Gson().toJson(docModel);

                String pathDocs = writeToFile(requireActivity().getFilesDir(),json);

                ActiveEnrollment activeEnrollment = DatosRecolectados.activeEnrollment;
                activeEnrollment.setJson_docs(pathDocs);
                activeEnrollment.setDocs_id("10");
                activeEnrollment.setState_id("5");

                DataBase dataBase = new DataBase(requireContext());
                dataBase.open();
                dataBase.updateEnroll(activeEnrollment,activeEnrollment.getEnroll_id());
                dataBase.close();

                requireActivity().runOnUiThread(()->{
                    ((Main2Activity) getActivity()).replaceFragments(ReviewFragment.class);
                });


            });



            //((MainActivity) getActivity()).replaceFragments(ReviewFragment.class);
            /*executor.execute(() -> {
             IdentificacionModel identificacion = DatosRecolectados.identificacion;
             PersonaModel persona = DatosRecolectados.persona;

            SolicitanteModel solicitanteModel = new SolicitanteModel();
            solicitanteModel.setNombre(persona.getNombre());
            solicitanteModel.setPaterno(persona.getPaterno());
            solicitanteModel.setMaterno(persona.getMaterno());
            try {
                String fechaFormada = dateFormat(persona.getFechaDeNacimiento());
                if (fechaFormada == null) {
                    solicitanteModel.setFechaDeNacimiento(fechaFormada());
                } else {
                    solicitanteModel.setFechaDeNacimiento(fechaFormada);
                }
            }catch (Exception e ){
                solicitanteModel.setFechaDeNacimiento(fechaFormada());
                e.printStackTrace();
            }
            solicitanteModel.setEmision(identificacion.getNumeroDeEmision());
            solicitanteModel.setSerie(identificacion.getSerie());
            solicitanteModel.setDireccionCompleta(persona.getDomicilioCompleto());
            solicitanteModel.setLugarDeNacimiento("D.f");
            solicitanteModel.setCurp(persona.getCurp());
            solicitanteModel.setCorreoElectronico("ahuerta@latinid.com.mx");
            solicitanteModel.setTelefono("5543578404");
            solicitanteModel.setNombreCompletoDelSolicitante(persona.getNombre() + " " + persona.getPaterno() + " " + persona.getMaterno());
            solicitanteModel.setSexo(persona.getSexo());
            solicitanteModel.setScoreDeLaComparacionFacial("75.696");
            solicitanteModel.setResultadoDeLaComparacionFacial("true");
            solicitanteModel.setFotoEnBase64(persona.getFotoSelfieB64().replaceAll("\n","").replace("\\",""));
            solicitanteModel.setSelfieBase64(persona.getFotoSelfieB64().replaceAll("\n","").replace("\\",""));
            solicitanteModel.setCapturaFrenteEnBase64(identificacion.getCapturaIdentificacionFrente().replaceAll("\n","").replace("\\",""));
            solicitanteModel.setCapturaReversoEnBase64(identificacion.getCapturaIdentificacionReverso().replaceAll("\n","").replace("\\",""));
            solicitanteModel.setFotoDeIdentificacionEnBase64(identificacion.getFotoRecorteB64().replaceAll("\n","").replace("\\",""));
            solicitanteModel.setTipoDeDocumento(identificacion.getTipoDeIdentificacion());
            solicitanteModel.setNumeroDeDocumento(identificacion.getOCR());
            solicitanteModel.setUsuarioId(1);
            solicitanteModel.setFechaDeRegistro(fechaFormada());
            String uuid = UUID.randomUUID().toString();
            solicitanteModel.setGuid(uuid);
            solicitanteModel.setAnioRegistro(identificacion.getFechaDeRegistro());

            if(identificacion.getTipoDeIdentificacion().equals("PASAPORTE CON CHIP")){
                solicitanteModel.setNumeroDeDocumento(identificacion.getNumeroDeDocumento());
                if(identificacion.getResultado() == 1){
                    solicitanteModel.setResultadoGeneral("Passed");
                }else{
                    solicitanteModel.setResultadoGeneral("Failed");
                }
            }else{
                switch (identificacion.getResult().result){
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
            solicitanteModel.setCoordenadasGps("19.412596570431628, -99.18741658511824");
            String json = new Gson().toJson(solicitanteModel);

            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject jsonObject1 = GetPost.crearPost("http://10.10.8.240:8080/api/paquetes/solicitantes", jsonObject, requireContext());
                System.out.println(jsonObject1);
                int idSolicitante = jsonObject1.getInt("solicitanteId");
                sendFingers(idSolicitante, DatosRecolectados.thumbRight.replaceAll("\n","").replace("\\",""),1,1);
                sendFingers(idSolicitante, DatosRecolectados.indexRight.replaceAll("\n","").replace("\\",""),1,2);
                sendFingers(idSolicitante, DatosRecolectados.middleRight.replaceAll("\n","").replace("\\",""),1,3);
                sendFingers(idSolicitante, DatosRecolectados.ringRight.replaceAll("\n","").replace("\\",""),1,4);
                sendFingers(idSolicitante, DatosRecolectados.littleRight.replaceAll("\n","").replace("\\",""),1,5);
                sendFingers(idSolicitante, DatosRecolectados.thumbLeft.replaceAll("\n","").replace("\\",""),1,6);
                sendFingers(idSolicitante, DatosRecolectados.indexLeft.replaceAll("\n","").replace("\\",""),1,7);
                sendFingers(idSolicitante, DatosRecolectados.middleLeft.replaceAll("\n","").replace("\\",""),1,8);
                sendFingers(idSolicitante, DatosRecolectados.ringLeft.replaceAll("\n","").replace("\\",""),1,9);
                sendFingers(idSolicitante, DatosRecolectados.littleLeft.replaceAll("\n","").replace("\\",""),1,10);
                sendDocs(idSolicitante,DatosRecolectados.docB64Address.replaceAll("\n","").replace("\\",""),1,DatosRecolectados.fileTypeAddress);
                sendDocs(idSolicitante,DatosRecolectados.docB64Banking.replaceAll("\n","").replace("\\",""),1,DatosRecolectados.fileTypeBanking);
                sendDocs(idSolicitante,DatosRecolectados.docB64Income.replaceAll("\n","").replace("\\",""),1,DatosRecolectados.fileTypeIncome);
                Data.Builder data = new Data.Builder();
                data.putString("nombre", persona.getNombre());
                data.putString("paterno", persona.getPaterno());
                data.putString("materno", persona.getMaterno());
                data.putInt("solicitanteId", idSolicitante);
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SignatureBackWok.class).setInputData(data.build())
                        .build();
                WorkManager.getInstance(requireContext())
                        .beginWith(Collections.singletonList(workRequest))
                        .enqueue();


            }catch (Throwable e){
                e.printStackTrace();
            }
        });*/

        });
        return binding.getRoot();
    }

    private void botonesCaptura() {
        binding.capturarDomicilio.setOnClickListener(view -> {
            estadoDocumento = CAPTURA_DOMICILIO;
            fotoDomicilio = null;
            uriDomicilio = null;
            mostrarModoCaptura("Comprobante de domicilio", "CFE, TELMEX. No mayor a 2 meses");
        });
        binding.capturaBancario.setOnClickListener(view -> {
            estadoDocumento = CAPTURA_BANCARIO;
            fotoBancario = null;
            uriBancario = null;
            mostrarModoCaptura("Comprobante bancario", "Estado de cuenta");
        });
        binding.capturarIngesos.setOnClickListener(view -> {
            estadoDocumento = CAPTURA_INGRESOS;
            fotoIngresos = null;
            uriIngresos = null;
            mostrarModoCaptura("Comprobante de ingresos", "Recibo de nómina");
        });
        getParentFragmentManager().setFragmentResultListener("modoCaptura", this, (requestKey, bundle) -> {
            System.out.println(requestKey);
            String result = bundle.getString("accion");
            switch (result) {
                case "camara":
                    accionarCamara();
                    break;
                case "adjuntar":
                    accionarAdjuntarArchivo();
                    break;
                case "cerrar":
                    break;
            }
        });
    }

    private void botonesQuitarDocumento(){
        binding.chipDomicilio.setOnCloseIconClickListener(view -> {
            view.setVisibility(View.INVISIBLE);
            binding.capturarDomicilio.setVisibility(View.VISIBLE);
            binding.verDomicilio.setVisibility(View.INVISIBLE);
            binding.siguiente.setEnabled(false);
            domicilioCapturado = false;
        });
        binding.chipBancario.setOnCloseIconClickListener(view -> {
            view.setVisibility(View.INVISIBLE);
            binding.capturaBancario.setVisibility(View.VISIBLE);
            binding.verBancario.setVisibility(View.INVISIBLE);
            binding.siguiente.setEnabled(false);
            bancarioCapturado = false;
        });
        binding.chipIngresos.setOnCloseIconClickListener(view -> {
            view.setVisibility(View.INVISIBLE);
            binding.capturarIngesos.setVisibility(View.VISIBLE);
            binding.verIngresos.setVisibility(View.INVISIBLE);
            binding.siguiente.setEnabled(false);
            ingresosCapturado = false;
        });
    }

    private void botonesVerArchivos(){
        binding.verDomicilio.setOnClickListener(view -> {
            if(fotoDomicilio!=null){
                mostrarFoto(fotoDomicilio);
            } else if(uriDomicilio!=null){
                mostrarAdjunto(uriDomicilio, domicilioDOC);
            }
        });
        binding.verBancario.setOnClickListener(view -> {
            if(fotoBancario!=null){
                mostrarFoto(fotoBancario);
            } else if(uriBancario!=null){
                mostrarAdjunto(uriBancario, bancarioDOC);
            }
        });
        binding.verIngresos.setOnClickListener(view -> {
            if(fotoIngresos!=null){
                mostrarFoto(fotoIngresos);
            } else if(uriIngresos!=null){
                mostrarAdjunto(uriIngresos, ingresosDOC);
            }
        });
    }

    private void mostrarFoto(File file){
        Uri photoURI = FileProvider.getUriForFile(requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(photoURI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }



    private void mostrarAdjunto(Uri uri, String doc){
        try {
            if(Objects.requireNonNull(getMimeType(uri)).equals("pdf")){
                ViewPdfFragment viewPdfFragment = ViewPdfFragment.newInstance(doc);
                viewPdfFragment.setCancelable(true);
                viewPdfFragment.show(getParentFragmentManager(),"pdf");
            }else{
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setData(uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |  Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivity(intent);
            }
        } catch (Throwable e) {
            Log.e("DocsFragment","Error: ", e);
        }
    }

    private  String getMimeType(Uri uri) {
      try {
          ContentResolver cR = requireContext().getContentResolver();
          MimeTypeMap mime = MimeTypeMap.getSingleton();
          return mime.getExtensionFromMimeType(cR.getType(uri));
      }catch (Throwable e){
          e.printStackTrace();
          return null;
      }
    }

    private void mostrarModoCaptura(String titulo, String subtitulo) {
        ModoCapturaFragment modoCapturaFragment = ModoCapturaFragment.newInstance(titulo, subtitulo);
        modoCapturaFragment.setCancelable(false);
        modoCapturaFragment.show(getParentFragmentManager(), titulo);
    }


    @SuppressLint("QueryPermissionsNeeded")
    private void accionarCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra("android.intent.extra.USE_BACK_CAMERA", true);
        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Log.e("Main", "IOException", e);
            }
            if (photoFile != null) {
                fileTemporal = photoFile;
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                tomarFotoResultLauncher.launch(cameraIntent);
            }
        }
    }

    ActivityResultLauncher<Intent> tomarFotoResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {

                if (result.getResultCode() == Activity.RESULT_OK) {
                    String fotoBase64 = getBase64FromPath(fileTemporal);
                    if (estadoDocumento == CAPTURA_DOMICILIO) {
                        domicilioCapturado = true;
                        terminarCapturaDomicilio(null, "comprobante.jpg");
                        fotoDomicilio = fileTemporal;
                        domicilioDOC = fotoBase64;
                        DatosRecolectados.docB64Address = fotoBase64;
                        DatosRecolectados.fileTypeAddress = "jpg";
                    } else if (estadoDocumento == CAPTURA_BANCARIO) {
                        bancarioCapturado = true;
                        terminarCapturaBancario(null,"bancario.jpg");
                        fotoBancario = fileTemporal;
                        bancarioDOC = fotoBase64;
                        DatosRecolectados.docB64Banking = fotoBase64;
                        DatosRecolectados.fileTypeBanking = "jpg";
                    } else if (estadoDocumento == CAPTURA_INGRESOS) {
                        ingresosCapturado = true;
                        terminarCapturaIngresos(null,"ingresos.jpg");
                        fotoIngresos = fileTemporal;
                        ingresosDOC = fotoBase64;
                        DatosRecolectados.docB64Income = fotoBase64;
                        DatosRecolectados.fileTypeIncome = "jpg";
                    }
                    //fileTemporal.delete();
                    fileTemporal = null;
                }
            });

    private String getBase64FromPath(File path) {
        String base64 = "";
        try {
            byte[] buffer = new byte[(int) path.length() + 100];
            int length = new FileInputStream(path).read(buffer);
            base64 = Base64.encodeToString(buffer, 0, length,
                    Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64;
    }

    private void accionarAdjuntarArchivo() {
        String[] mimeTypes = {"image/*", "application/pdf"};
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        adjuntarResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> adjuntarResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri uri;
                    if (data != null) {
                        uri = data.getData();
                        String docB64 = convertirUritoStringB64(uri);
                        if (estadoDocumento == CAPTURA_DOMICILIO) {
                            domicilioDOC = docB64;
                            domicilioCapturado = true;
                            uriDomicilio = uri;
                            DatosRecolectados.docB64Address = docB64;
                            DatosRecolectados.fileTypeAddress = getMimeType(uri);
                            terminarCapturaDomicilio(uri, "");
                        } else if (estadoDocumento == CAPTURA_BANCARIO) {
                            bancarioCapturado = true;
                            uriBancario = uri;
                            bancarioDOC = docB64;
                            DatosRecolectados.docB64Banking = docB64;
                            DatosRecolectados.fileTypeBanking = getMimeType(uri);
                            terminarCapturaBancario(uri, "");
                        } else if (estadoDocumento == CAPTURA_INGRESOS) {
                            ingresosDOC = docB64;
                            DatosRecolectados.docB64Income = docB64;
                            DatosRecolectados.fileTypeIncome = getMimeType(uri);
                            ingresosCapturado = true;
                            uriIngresos = uri;
                            terminarCapturaIngresos(uri, "");
                        }
                    }
                }
            });

    private void terminarCapturaDomicilio(Uri uri, String name){
        binding.capturarDomicilio.setVisibility(View.INVISIBLE);
        binding.verDomicilio.setVisibility(View.VISIBLE);
        binding.chipDomicilio.setText((name.equals(""))? getFileName(uri) : name);
        binding.chipDomicilio.setVisibility(View.VISIBLE);
        binding.capturaBancario.setVisibility(View.VISIBLE);
        if(bancarioCapturado & ingresosCapturado){
            binding.siguiente.setEnabled(true);
        }
    }

    private void terminarCapturaBancario(Uri uri, String name){
        binding.capturaBancario.setVisibility(View.INVISIBLE);
        binding.verBancario.setVisibility(View.VISIBLE);
        binding.chipBancario.setText((name.equals(""))? getFileName(uri) : name);
        binding.chipBancario.setVisibility(View.VISIBLE);
        binding.capturarIngesos.setVisibility(View.VISIBLE);
        if(domicilioCapturado & ingresosCapturado){
            binding.siguiente.setEnabled(true);
        }
    }

    private void terminarCapturaIngresos(Uri uri, String name){
        binding.capturarIngesos.setVisibility(View.INVISIBLE);
        binding.verIngresos.setVisibility(View.VISIBLE);
        binding.chipIngresos.setText((name.equals(""))? getFileName(uri) : name);
        binding.chipIngresos.setVisibility(View.VISIBLE);
        if(domicilioCapturado & bancarioCapturado){
            binding.siguiente.setEnabled(true);
        }
    }

    private String convertirUritoStringB64(Uri uri) {
        try {
            InputStream in = getActivity().getContentResolver().openInputStream(uri);
            byte[] bytes = getBytes(in);
            String Document = Base64.encodeToString(bytes, Base64.DEFAULT);
            return Document;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    cursor.moveToFirst();
                    result = cursor.getString(nameIndex);
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getFilesDir();
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }



}