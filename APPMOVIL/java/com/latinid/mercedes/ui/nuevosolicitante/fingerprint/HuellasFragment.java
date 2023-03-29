package com.latinid.mercedes.ui.nuevosolicitante.fingerprint;

import static com.latinid.mercedes.util.OperacionesUtiles.writeToFile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.latinid.mercedes.DatosRecolectados;

import com.latinid.mercedes.R;

import com.latinid.mercedes.databinding.FragmentHuellasBinding;
import com.latinid.mercedes.db.DataBase;
import com.latinid.mercedes.db.model.ActiveEnrollment;
import com.latinid.mercedes.model.local.FingerModel;
import com.latinid.mercedes.ui.nuevosolicitante.archivos.DocsFragment;
import com.latinid.mercedes.Main2Activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintImage;
import cn.com.aratek.fp.FingerprintScanner;
import cn.com.aratek.util.Result;


public class HuellasFragment extends Fragment {

    private static final String TAG = "HuellasFragment";
    private FragmentHuellasBinding binding;
    private FingerprintScanner mFingerprintScanner;
    private static String FP_DB_PATH = "/sdcard/fpMBBFingers.db";
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ExecutorService executorFin = Executors.newSingleThreadExecutor();

    private static int ESTADO_MANO_DERECHA = 0;
    private static int ESTADO_MANO_IZQUIERDA = 1;
    private static int DEDO_PULGAR = 0;
    private static int DEDO_INDICE = 1;
    private static int DEDO_MEDIO = 2;
    private static int DEDO_ANULAR = 3;
    private static int DEDO_MENIQUE = 4;
    private int estadoActual = ESTADO_MANO_DERECHA;
    private int dedoActual = DEDO_PULGAR;
    private boolean omitir = false;

    private static final int STATUS_CLOSED = 0;
    private static final int STATUS_OPENED = 1;
    private static final int STATUS_CLOSING = 2;
    private static final int STATUS_OPENING = 3;
    private static final int STATUS_ENROLL = 4;

    private AtomicInteger mStatus;
    private AtomicInteger mStatusCap;
    AnimationDrawable frameAnimation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHuellasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.resetSensor.setPaintFlags(binding.resetSensor.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FP_DB_PATH = requireContext().getFilesDir().getAbsolutePath() + File.separator + "fpMB.db";
        mFingerprintScanner = FingerprintScanner.getInstance(requireContext());
        mStatus = new AtomicInteger(STATUS_CLOSED);
        mStatusCap = new AtomicInteger(STATUS_CLOSED);
      /*  new Thread(() -> {
            try {
                int[] fingersEnroll = {101, 102, 103, 104, 105, 106, 107, 108, 109, 110};
                for (int idF : fingersEnroll) {
                    int resp = Bione.delete(idF);
                    Log.i(TAG, "Respuesta borrado de huellas: " + idF + " : " + resp);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).start();*/
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread() {
            @Override
            public void run() {
                iniciarEscaner();
            }
        }.start();
        if (STATUS_ENROLL == mStatusCap.get()) {
            try {
                omitir = true;
                executor.shutdownNow();
                executor.awaitTermination(500, TimeUnit.MILLISECONDS);
                Thread.sleep(500);
                if (executor.isShutdown()) {
                    executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        seguirFlujo();
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        binding.resetSensor.setOnClickListener(view -> {

            new Thread(() -> {
                try {
                    onResume();
                    Thread.sleep(1000);
                    detenerAnimacionDedo();
                    omitir = true;
                    executor.shutdownNow();
                    executor.awaitTermination(500, TimeUnit.MILLISECONDS);
                    Thread.sleep(500);
                    if (executor.isShutdown()) {
                        executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            seguirFlujo();
                        });
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }).start();


        });
        binding.finalizar.setOnClickListener(view -> {
            executorFin.execute(() -> {
                requireActivity().runOnUiThread(() -> {
                    view.setVisibility(View.INVISIBLE);
                    binding.gifMercedes.setBackgroundResource(R.drawable.loadinggeneral);
                    frameAnimation = (AnimationDrawable) binding.gifMercedes.getBackground();
                    frameAnimation.start();
                    binding.gifMercedes.setVisibility(View.VISIBLE);
                });
                FingerModel fingerModel = new FingerModel();
                fingerModel.setThumbRight(DatosRecolectados.thumbRight);
                fingerModel.setIndexRight(DatosRecolectados.indexRight);
                fingerModel.setMiddleRight(DatosRecolectados.middleRight);
                fingerModel.setRingRight(DatosRecolectados.ringRight);
                fingerModel.setLittleRight(DatosRecolectados.littleRight);
                fingerModel.setThumbLeft(DatosRecolectados.thumbLeft);
                fingerModel.setIndexLeft(DatosRecolectados.indexLeft);
                fingerModel.setMiddleLeft(DatosRecolectados.middleLeft);
                fingerModel.setRingLeft(DatosRecolectados.ringLeft);
                fingerModel.setLittleLeft(DatosRecolectados.littleLeft);
                String json = new Gson().toJson(fingerModel);
                String json_fingerPath = writeToFile(requireActivity().getFilesDir(), json);
                ActiveEnrollment activeEnrollment = DatosRecolectados.activeEnrollment;
                activeEnrollment.setFinger_id("10");
                activeEnrollment.setState_id("4");
                activeEnrollment.setJson_finger(json_fingerPath);
                DataBase dataBase = new DataBase(requireContext());
                dataBase.open();
                dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
                dataBase.close();

                try {
                    int error = Bione.clear();
                    if (error == Bione.RESULT_OK) {
                        System.out.println("CleanDataBase");
                    } else {
                        System.out.println(getErrorString(error));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                requireActivity().runOnUiThread(() -> {
                    ((Main2Activity) getActivity()).replaceFragments(DocsFragment.class);
                });
            });
        });
        binding.capturaHuellas.setOnClickListener(view -> {
            view.setVisibility(View.INVISIBLE);
            binding.omitirDedo.setVisibility(View.VISIBLE);
            binding.recapturar.setVisibility(View.VISIBLE);
            binding.regresarDedo.setVisibility(View.VISIBLE);
            binding.textoMensaje.setText("Empieza con el pulgar");
            binding.textTitulo.setText("Mano derecha");
            mStatusCap.set(STATUS_ENROLL);
            executor.execute(() -> {
                seguirFlujo();
            });
        });
        binding.omitirDedo.setOnClickListener(view -> {
            MenuSkipFragment menuSkipFragment = new MenuSkipFragment();
            menuSkipFragment.setCancelable(true);
            menuSkipFragment.show(getParentFragmentManager(), "Omisión");

            getParentFragmentManager().setFragmentResultListener("omitido", this, (requestKey, bundle) -> {
                String result = bundle.getString("action");
                switch (result) {
                    case "amputado":
                        saveFingers("amputado");
                        break;
                    case "vendado":
                        saveFingers("vendado");
                        break;
                    case "na":
                        saveFingers("na");
                        break;
                    case "cerrar":
                        break;
                }

                new Thread(() -> {
                    try {
                        detenerAnimacionDedo();
                        omitir = true;
                        executor.shutdownNow();
                        executor.awaitTermination(500, TimeUnit.MILLISECONDS);
                        Thread.sleep(500);
                        if (dedoActual == DEDO_MENIQUE) {
                            if (estadoActual == ESTADO_MANO_DERECHA) {
                                dedoActual = DEDO_PULGAR;
                                estadoActual = ESTADO_MANO_IZQUIERDA;
                                getActivity().runOnUiThread(() -> {
                                    capturarManoIzquierdaUI();
                                    binding.textoMensaje.setText("Pulgar");
                                    binding.textTitulo.setText("Mano izquierda");
                                });
                            } else if (estadoActual == ESTADO_MANO_IZQUIERDA) {
                                finalizarCaptura();
                                return;
                            }
                        } else {
                            dedoActual++;
                        }
                        if (executor.isShutdown()) {
                            executor = Executors.newSingleThreadExecutor();
                            executor.execute(() -> {
                                seguirFlujo();
                            });
                        }
                    } catch (Throwable e) {
                        Log.e(TAG, "ERROR EN OMITIR DEDO", e);
                    }
                }).start();
            });


        });
        binding.recapturar.setOnClickListener(view -> {
            new Thread() {
                @Override
                public void run() {
                    try {
                        detenerAnimacionDedo();
                        omitir = true;
                        executor.shutdownNow();
                        executor.awaitTermination(500, TimeUnit.MILLISECONDS);
                        Thread.sleep(500);
                        reiniciar();
                        reiniciarUI();
                        if (executor.isShutdown()) {
                            executor = Executors.newSingleThreadExecutor();
                            executor.execute(() -> {
                                seguirFlujo();
                            });
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        });
        binding.regresarDedo.setOnClickListener(view -> {
            new Thread() {
                @Override
                public void run() {
                    try {
                        detenerAnimacionDedo();
                        omitir = true;
                        executor.shutdownNow();
                        executor.awaitTermination(500, TimeUnit.MILLISECONDS);
                        Thread.sleep(500);
                        if (dedoActual == DEDO_PULGAR) {
                            if (estadoActual == ESTADO_MANO_IZQUIERDA) {
                                dedoActual = DEDO_MENIQUE;
                                estadoActual = ESTADO_MANO_DERECHA;
                                getActivity().runOnUiThread(() -> {
                                    capturarManoIzquierdaUI();
                                    binding.textoMensaje.setText("Meñique");
                                    binding.textTitulo.setText("Mano derecha");
                                    manoCapturada();
                                });
                            } else if (estadoActual == ESTADO_MANO_DERECHA) {
                                /*SE SUPONE QUE DEBERIA ESTAR DESABILITADO CUANDO ESTE EN ESTE ESTADO*/
                                return;
                            }
                        } else {
                            dedoActual--;
                        }
                        if (estadoActual == ESTADO_MANO_DERECHA) {
                            if (dedoActual == DEDO_INDICE) {
                                getActivity().runOnUiThread(() -> {
                                    view.setEnabled(false);
                                    binding.regresarDedo.setEnabled(false);
                                    binding.recapturar.setEnabled(false);
                                });
                            }
                        }
                        if (executor.isShutdown()) {
                            executor = Executors.newSingleThreadExecutor();
                            executor.execute(() -> {
                                seguirFlujo();
                            });
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        });
    }

    private void seguirFlujo() {
        try {
            omitir = false;
            FingerprintImage fi;
            switch (dedoActual) {
                case 0:
                    getActivity().runOnUiThread(() -> {
                        binding.textoMensaje.setText("Pulgar");
                        binding.slotPulgar.setImageResource(R.mipmap.nofinger);
                    });
                    indicarDedoUI(binding.slotPulgar);
                    fi = esperarHuella();

                    if (fi != null) {
                        detenerDedoUI(binding.slotPulgar);
                        updateFingerprintImage(fi, binding.slotPulgar);
                        String b64 = Base64.encodeToString(fi.convert2Wsq(), Base64.DEFAULT);
                        saveFingers(b64);
                        dedoActual = DEDO_INDICE;
                        Thread.sleep(1500);
                        seguirFlujo();
                    }
                    break;
                case 1:
                    getActivity().runOnUiThread(() -> {
                        binding.textoMensaje.setText("Índice");
                        binding.regresarDedo.setEnabled(true);
                        binding.slotIndice.setImageResource(R.mipmap.nofinger);
                    });
                    indicarDedoUI(binding.slotIndice);
                    fi = esperarHuella();
                    if (fi != null) {
                        detenerDedoUI(binding.slotIndice);
                        updateFingerprintImage(fi, binding.slotIndice);
                        String b64 = Base64.encodeToString(fi.convert2Wsq(), Base64.DEFAULT);
                        saveFingers(b64);
                        dedoActual = DEDO_MEDIO;
                        Thread.sleep(1500);
                        seguirFlujo();
                    }
                    break;
                case 2:
                    getActivity().runOnUiThread(() -> {
                        binding.textoMensaje.setText("Medio");
                        binding.recapturar.setEnabled(true);
                        binding.slotMedio.setImageResource(R.mipmap.nofinger);
                    });
                    indicarDedoUI(binding.slotMedio);
                    fi = esperarHuella();
                    if (fi != null) {
                        detenerDedoUI(binding.slotMedio);
                        updateFingerprintImage(fi, binding.slotMedio);
                        String b64 = Base64.encodeToString(fi.convert2Wsq(), Base64.DEFAULT);
                        saveFingers(b64);
                        dedoActual = DEDO_ANULAR;
                        Thread.sleep(1500);
                        seguirFlujo();
                    }

                    break;
                case 3:
                    getActivity().runOnUiThread(() -> {
                        binding.textoMensaje.setText("Anular");
                        binding.slotAnular.setImageResource(R.mipmap.nofinger);
                    });
                    indicarDedoUI(binding.slotAnular);
                    fi = esperarHuella();
                    if (fi != null) {
                        detenerDedoUI(binding.slotAnular);
                        updateFingerprintImage(fi, binding.slotAnular);
                        String b64 = Base64.encodeToString(fi.convert2Wsq(), Base64.DEFAULT);
                        saveFingers(b64);
                        dedoActual = DEDO_MENIQUE;
                        Thread.sleep(1500);
                        seguirFlujo();
                    }
                    break;
                case 4:
                    getActivity().runOnUiThread(() -> {
                        binding.textoMensaje.setText("Meñique");
                        binding.slotMenique.setImageResource(R.mipmap.nofinger);
                    });
                    indicarDedoUI(binding.slotMenique);
                    fi = esperarHuella();
                    if (fi != null) {
                        detenerDedoUI(binding.slotMenique);
                        updateFingerprintImage(fi, binding.slotMenique);
                        String b64 = Base64.encodeToString(fi.convert2Wsq(), Base64.DEFAULT);
                        saveFingers(b64);
                        if (estadoActual == ESTADO_MANO_DERECHA) {
                            dedoActual = DEDO_PULGAR;
                            estadoActual = ESTADO_MANO_IZQUIERDA;
                            Thread.sleep(1500);
                            getActivity().runOnUiThread(() -> {
                                capturarManoIzquierdaUI();
                                binding.textoMensaje.setText("Pulgar");
                                binding.textTitulo.setText("Mano izquierda");
                            });
                            seguirFlujo();
                        } else {
                            finalizarCaptura();
                        }
                    }

                    break;
                default:
            }
        } catch (Throwable e) {
            Log.e(TAG, "ERROR EN HUELLAS: ", e);
        }
    }

    private void saveFingers(String fingerPrint) {
        try {
            if (fingerPrint.equals("amputado") || fingerPrint.equals("vendado") || fingerPrint.equals("na")) {
                drawOmission(fingerPrint);
            }
            switch (dedoActual) {
                case 0:
                    if (estadoActual == ESTADO_MANO_DERECHA) {
                        DatosRecolectados.thumbRight = fingerPrint;
                    }
                    if (estadoActual == ESTADO_MANO_IZQUIERDA) {
                        DatosRecolectados.thumbLeft = fingerPrint;
                    }
                    break;
                case 1:
                    if (estadoActual == ESTADO_MANO_DERECHA) {
                        DatosRecolectados.indexRight = fingerPrint;
                    }
                    if (estadoActual == ESTADO_MANO_IZQUIERDA) {
                        DatosRecolectados.indexLeft = fingerPrint;
                    }
                    break;
                case 2:
                    if (estadoActual == ESTADO_MANO_DERECHA) {
                        DatosRecolectados.middleRight = fingerPrint;
                    }
                    if (estadoActual == ESTADO_MANO_IZQUIERDA) {
                        DatosRecolectados.middleLeft = fingerPrint;
                    }
                    break;
                case 3:
                    if (estadoActual == ESTADO_MANO_DERECHA) {
                        DatosRecolectados.ringRight = fingerPrint;
                    }
                    if (estadoActual == ESTADO_MANO_IZQUIERDA) {
                        DatosRecolectados.ringLeft = fingerPrint;
                    }
                    break;
                case 4:
                    if (estadoActual == ESTADO_MANO_DERECHA) {
                        DatosRecolectados.littleRight = fingerPrint;
                    }
                    if (estadoActual == ESTADO_MANO_IZQUIERDA) {
                        DatosRecolectados.littleLeft = fingerPrint;
                    }
                    break;
                default:
            }
        } catch (Throwable e) {
            Log.e(TAG, "Error saveFingers() ", e);
        }
    }

    List<Integer> fingersID = new ArrayList<>();
    int indexFingerCount = 101;

    private FingerprintImage esperarHuella() {
        FingerprintImage fi = null;
        Result res;

        mFingerprintScanner.prepare();
        do {
            if (omitir) {
                Log.d(TAG, "omitido");
                break;
            }
            if (executor.isShutdown()) {
                break;
            }
            res = mFingerprintScanner.capture();
            fi = (FingerprintImage) res.data;
            if (fi != null) {
                if (Bione.getFingerprintQuality(fi) > 60) {
                    if (res.error != FingerprintScanner.NO_FINGER || executor.isShutdown()) {
                        byte[] fpFeat = null, fpTemp;
                        Result res2;
                        res2 = Bione.extractFeature(fi);
                        if (res2.error != Bione.RESULT_OK) {
                            Log.e(TAG, "Error() Bione.extractFeature(fi)");
                        }
                        fpFeat = (byte[]) res2.data;
                        int id = Bione.identify(fpFeat);
                        if (id < 0) {
                            requireActivity().runOnUiThread(() -> {
                                binding.textoMensaje2.setVisibility(View.GONE);
                            });
                            res = Bione.makeTemplate(fpFeat, fpFeat, fpFeat);
                            if (res.error != Bione.RESULT_OK) {
                                Log.e(TAG, "Error() Bione.makeTemplate()");
                            }
                            fpTemp = (byte[]) res.data;

                            int idR = Bione.getFreeID();
                            Log.d(TAG, "free id" + indexFingerCount);
                            if (idR < 0) {
                                Log.e(TAG, "Base de huellas llena");
                            }
                            int ret = Bione.enroll(indexFingerCount, fpTemp);
                            if (ret != Bione.RESULT_OK) {
                                Log.e(TAG, "Error() Bione.enroll()");
                            }
                            fingersID.add(indexFingerCount);
                            indexFingerCount++;
                            break;
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                binding.textoMensaje2.setVisibility(View.VISIBLE);
                            });
                        }
                    }
                }
                //Log.i(TAG, "Fingerprint image quality is " + Bione.getFingerprintQuality(fi));
            }
        } while (true);
        mFingerprintScanner.finish();
        return fi;
    }

    private void updateFingerprintImage(FingerprintImage fi, ImageView view) {
        byte[] fpBmp;
        Bitmap bitmap;
        if (fi == null || (fpBmp = fi.convert2Bmp()) == null ||
                (bitmap = BitmapFactory.decodeByteArray(fpBmp, 0, fpBmp.length)) == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.nofinger);
        }
        Bitmap finalBitmap = bitmap;
        getActivity().runOnUiThread(() -> {
            view.setImageBitmap(finalBitmap);
        });
    }

    private void drawOmission(String omi) {
        try {
            requireActivity().runOnUiThread(() -> {
                switch (dedoActual) {
                    case 0:
                        binding.slotPulgar.setBackgroundColor(0);
                        binding.slotPulgar.clearColorFilter();
                        if (omi.equals("amputado")) {
                            binding.slotPulgar.setImageResource(R.mipmap.amputated);
                        } else {
                            binding.slotPulgar.setImageResource(R.mipmap.band_aid);
                        }
                        break;
                    case 1:
                        binding.slotIndice.setBackgroundColor(0);
                        binding.slotIndice.clearColorFilter();
                        if (omi.equals("amputado")) {
                            binding.slotIndice.setImageResource(R.mipmap.amputated);
                        } else {
                            binding.slotIndice.setImageResource(R.mipmap.band_aid);
                        }
                        break;
                    case 2:
                        binding.slotMedio.setBackgroundColor(0);
                        binding.slotMedio.clearColorFilter();
                        if (omi.equals("amputado")) {
                            binding.slotMedio.setImageResource(R.mipmap.amputated);
                        } else {
                            binding.slotMedio.setImageResource(R.mipmap.band_aid);
                        }
                        break;
                    case 3:
                        binding.slotAnular.setBackgroundColor(0);
                        binding.slotAnular.clearColorFilter();
                        if (omi.equals("amputado")) {
                            binding.slotAnular.setImageResource(R.mipmap.amputated);
                        } else {
                            binding.slotAnular.setImageResource(R.mipmap.band_aid);
                        }
                        break;
                    case 4:
                        binding.slotMenique.setBackgroundColor(0);
                        binding.slotMenique.clearColorFilter();
                        if (omi.equals("amputado")) {
                            binding.slotMenique.setImageResource(R.mipmap.amputated);
                        } else {
                            binding.slotMenique.setImageResource(R.mipmap.band_aid);
                        }
                        break;
                    default:
                }
            });
        } catch (Throwable e) {
            Log.e(TAG, "Error drawOmission() ", e);
        }
    }

    private void finalizarCaptura() {
        getActivity().runOnUiThread(() -> {
            binding.textoMensaje.setText("Finalizado");
            binding.textTitulo.setText("Captura de huellas");
            binding.regresarDedo.setVisibility(View.INVISIBLE);
            binding.omitirDedo.setVisibility(View.INVISIBLE);
            binding.recapturar.setVisibility(View.INVISIBLE);
            binding.capturaHuellas.setVisibility(View.INVISIBLE);
            binding.finalizar.setVisibility(View.VISIBLE);
            try {
                //Thread.sleep(1500);
                //((MainActivity) getActivity()).replaceFragments(DocsFragment.class);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    private void reiniciar() {
        getActivity().runOnUiThread(() -> {
            estadoActual = ESTADO_MANO_DERECHA;
            dedoActual = DEDO_PULGAR;
            binding.textoMensaje.setText("Pulgar");
            binding.textTitulo.setText("Mano derecha");
        });
    }

    private void indicarDedoUI(ImageView s) {
        getActivity().runOnUiThread(() -> {
            try {
                s.clearColorFilter();
                s.clearAnimation();
                s.setBackgroundColor(getResources().getColor(R.color.app_blue_initial));
                s.invalidate();
                s.setVisibility(View.VISIBLE);
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                fadeIn.setDuration(400);
                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                fadeOut.setStartOffset(400);
                fadeOut.setDuration(400);
                fadeOut.setRepeatCount(Animation.INFINITE);
                fadeOut.setRepeatMode(Animation.REVERSE);
                AnimationSet animation = new AnimationSet(false); //change to false
                animation.addAnimation(fadeIn);
                animation.addAnimation(fadeOut);
                s.setAnimation(animation);
            } catch (Throwable e) {
                //Verificar fallo de  java.lang.IllegalStateException: Fragment HuellasFragment{e89260b} (d1b00920-d5fc-4c38-89af-9141bad564da) not attached to a context.
                e.printStackTrace();
            }

        });
    }

    private void capturarManoIzquierdaUI() {
        reinciarSlots();
        binding.manoPulgar.setImageResource(R.mipmap.img_pulgar_izq);
        binding.maniInidice.setImageResource(R.mipmap.img_indice_izq);
        binding.manoMedio.setImageResource(R.mipmap.img_medio_izq);
        binding.manoAnular.setImageResource(R.mipmap.img_anular_izq);
        binding.manoMenique.setImageResource(R.mipmap.img_menique_izq);
    }

    private void manoCapturada() {
        binding.slotPulgar.setImageResource(R.mipmap.huella_general);
        binding.slotPulgar.setBackgroundColor(0);
        binding.slotIndice.setImageResource(R.mipmap.huella_general);
        binding.slotIndice.setBackgroundColor(0);
        binding.slotMedio.setImageResource(R.mipmap.huella_general);
        binding.slotMedio.setBackgroundColor(0);
        binding.slotAnular.setImageResource(R.mipmap.huella_general);
        binding.slotAnular.setBackgroundColor(0);
        binding.slotMenique.setImageResource(R.mipmap.huella_general);
        mostrarManoDerecha();
    }

    private void reinciarSlots() {
        binding.slotPulgar.setImageResource(R.mipmap.nofinger);
        binding.slotPulgar.setBackgroundColor(getResources().getColor(R.color.app_blue_initial));
        binding.slotPulgar.clearColorFilter();
        binding.slotIndice.setImageResource(R.mipmap.nofinger);
        binding.slotIndice.setBackgroundColor(getResources().getColor(R.color.app_blue_initial));
        binding.slotIndice.clearColorFilter();
        binding.slotMedio.setImageResource(R.mipmap.nofinger);
        binding.slotMedio.setBackgroundColor(getResources().getColor(R.color.app_blue_initial));
        binding.slotMedio.clearColorFilter();
        binding.slotAnular.setImageResource(R.mipmap.nofinger);
        binding.slotAnular.setBackgroundColor(getResources().getColor(R.color.app_blue_initial));
        binding.slotAnular.clearColorFilter();
        binding.slotMenique.setImageResource(R.mipmap.nofinger);
        binding.slotMenique.setBackgroundColor(getResources().getColor(R.color.app_blue_initial));
        binding.slotMenique.clearColorFilter();
    }

    private void reiniciarUI() {
        getActivity().runOnUiThread(() -> {
            binding.regresarDedo.setEnabled(false);
            binding.recapturar.setEnabled(false);
            reinciarSlots();
            mostrarManoDerecha();
        });
    }

    private void mostrarManoDerecha() {
        binding.manoPulgar.setImageResource(R.mipmap.img_pulgar_der);
        binding.maniInidice.setImageResource(R.mipmap.img_indice_der);
        binding.manoMedio.setImageResource(R.mipmap.img_medio_der);
        binding.manoAnular.setImageResource(R.mipmap.img_anular_der);
        binding.manoMenique.setImageResource(R.mipmap.img_menique_der);
    }


    private void detenerAnimacionDedo() {
        getActivity().runOnUiThread(() -> {
            detenerDedoUI(binding.slotPulgar);
            detenerDedoUI(binding.slotIndice);
            detenerDedoUI(binding.slotMedio);
            detenerDedoUI(binding.slotAnular);
            detenerDedoUI(binding.slotMenique);
        });
    }

    private void detenerDedoUI(ImageView s) {
        getActivity().runOnUiThread(() -> {
            s.clearAnimation();
        });
    }

    private void iniciarEscaner() {
        mStatus.set(STATUS_OPENING);
        mFingerprintScanner.powerOn(); // ignore power on errors
        int error;
        Result res;
        error = mFingerprintScanner.open();
        if (error == FingerprintScanner.RESULT_OK) {
            mStatus.set(STATUS_OPENED);
            res = mFingerprintScanner.getFirmwareVersion();
            res = mFingerprintScanner.getSerial();
            mFingerprintScanner.setLfdLevel(0);

            // initialize fingerprint algorithm
            Log.i(TAG, "Fingerprint algorithm version: " + Bione.getVersion());
            if ((error = Bione.initialize(requireContext(), FP_DB_PATH))
                    != Bione.RESULT_OK) {
            }
        }
    }

    private void cerrarEscaner() {


        mFingerprintScanner.finish();
        mStatus.set(STATUS_CLOSING);
        int error;
        Bione.exit();
        error = mFingerprintScanner.close();
        mFingerprintScanner.powerOff(); // ignore power off errors


    }

    @Override
    public void onPause() {
        super.onPause();
        cerrarEscaner();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        System.out.println("Si onDetach");

       /* new Thread(() -> {
            try {
                int error = Bione.clear();
                if (error == Bione.RESULT_OK) {
                    System.out.println("CleanDataBase");
                } else {
                    System.out.println(getErrorString(error));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).start();*/


        try {
            cerrarEscaner();
            detenerAnimacionDedo();
            omitir = true;
            executor.shutdownNow();
            executor.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }

    private String getErrorString(int error) {
        int strid;
        switch (error) {
            case FingerprintScanner.RESULT_OK:
                strid = R.string.operation_successful;
                break;
            case FingerprintScanner.RESULT_FAIL:
                strid = R.string.error_operation_failed;
                break;
            case FingerprintScanner.WRONG_CONNECTION:
                strid = R.string.error_wrong_connection;
                break;
            case FingerprintScanner.DEVICE_BUSY:
                strid = R.string.error_device_busy;
                break;
            case FingerprintScanner.DEVICE_NOT_OPEN:
                strid = R.string.error_device_not_open;
                break;
            case FingerprintScanner.TIMEOUT:
                strid = R.string.error_timeout;
                break;
            case FingerprintScanner.NO_PERMISSION:
                strid = R.string.error_no_permission;
                break;
            case FingerprintScanner.WRONG_PARAMETER:
                strid = R.string.error_wrong_parameter;
                break;
            case FingerprintScanner.DECODE_ERROR:
                strid = R.string.error_decode;
                break;
            case FingerprintScanner.INIT_FAIL:
                strid = R.string.error_initialization_failed;
                break;
            case FingerprintScanner.UNKNOWN_ERROR:
                strid = R.string.error_unknown;
                break;
            case FingerprintScanner.NOT_SUPPORT:
                strid = R.string.error_not_support;
                break;
            case FingerprintScanner.NOT_ENOUGH_MEMORY:
                strid = R.string.error_not_enough_memory;
                break;
            case FingerprintScanner.DEVICE_NOT_FOUND:
                strid = R.string.error_device_not_found;
                break;
            case FingerprintScanner.DEVICE_REOPEN:
                strid = R.string.error_device_reopen;
                break;
            case FingerprintScanner.NO_FINGER:
                strid = R.string.error_no_finger;
                break;
            case Bione.INITIALIZE_ERROR:
                strid = R.string.error_algorithm_initialization_failed;
                break;
            case Bione.INVALID_FEATURE_DATA:
                strid = R.string.error_invalid_feature_data;
                break;
            case Bione.BAD_IMAGE:
                strid = R.string.error_bad_image;
                break;
            case Bione.NOT_MATCH:
                strid = R.string.error_not_match;
                break;
            case Bione.LOW_POINT:
                strid = R.string.error_low_point;
                break;
            case Bione.NO_RESULT:
                strid = R.string.error_no_result;
                break;
            case Bione.OUT_OF_BOUND:
                strid = R.string.error_out_of_bound;
                break;
            case Bione.DATABASE_FULL:
                strid = R.string.error_database_full;
                break;
            case Bione.LIBRARY_MISSING:
                strid = R.string.error_library_missing;
                break;
            case Bione.UNINITIALIZE:
                strid = R.string.error_algorithm_uninitialize;
                break;
            case Bione.REINITIALIZE:
                strid = R.string.error_algorithm_reinitialize;
                break;
            case Bione.REPEATED_ENROLL:
                strid = R.string.error_repeated_enroll;
                break;
            case Bione.NOT_ENROLLED:
                strid = R.string.error_not_enrolled;
                break;
            default:
                return "";
        }
        return getString(strid);
    }
}