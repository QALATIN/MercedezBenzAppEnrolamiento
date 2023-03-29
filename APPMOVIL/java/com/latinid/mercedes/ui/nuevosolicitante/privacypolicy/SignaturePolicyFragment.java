package com.latinid.mercedes.ui.nuevosolicitante.privacypolicy;

import static com.latinid.mercedes.util.OperacionesUtiles.dateEnrollment;
import static com.latinid.mercedes.util.OperacionesUtiles.generarTXTJson;
import static com.latinid.mercedes.util.OperacionesUtiles.writeToFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import com.latinid.mercedes.DatosRecolectados;

import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentSignaturePolicyBinding;
import com.latinid.mercedes.db.DataBase;
import com.latinid.mercedes.db.model.ActiveEnrollment;
import com.latinid.mercedes.model.local.SignatureModel;
import com.latinid.mercedes.ui.nuevosolicitante.capturaid.IdentificacionFragment;
import com.latinid.mercedes.Main2Activity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SignaturePolicyFragment extends Fragment {

    private static final String TAG = "SignaturePolicy";
    private FragmentSignaturePolicyBinding binding;
    private signature mSignature;
    private View mView;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignaturePolicyBinding.inflate(inflater, container, false);
        mSignature = new signature(requireContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        binding.linearLayout.addView(mSignature, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        mView = binding.linearLayout;
        binding.buttonClear.setOnClickListener(view -> {
            mSignature.clear();
        });
        binding.buttonBack.setOnClickListener(view -> {
            ((Main2Activity) getActivity()).removerFragment();
        });
        buttonNext();
        return binding.getRoot();
    }

    private void buttonNext(){
        binding.siguiente.setOnClickListener(view -> {
            requireActivity().runOnUiThread(() -> {
                view.setEnabled(false);
                view.setVisibility(View.INVISIBLE);
                binding.buttonClear.setEnabled(false);
                binding.loadSignature.setVisibility(View.VISIBLE);
                binding.loadSignature.setBackgroundResource(R.drawable.loadinggeneral);
                binding.loadSignature.setImageBitmap(null);
                binding.loadSignature.getLayoutParams().width = 50;
                binding.loadSignature.getLayoutParams().height = 50;
                AnimationDrawable frameAnimation = (AnimationDrawable) binding.loadSignature.getBackground();
                frameAnimation.start();
                mView.setDrawingCacheEnabled(true);
            });
            executor.execute(() -> {
                DataBase dataBase = new DataBase(requireContext());
                try {
                    dataBase.open();
                    String enrollTemp = String.valueOf(dataBase.getEnrolls().size()+1);
                    String signature = createSignatureBase64();
                    generarTXTJson(requireContext(), "firmaCreada.txt", signature);
                    signature = signature.replaceAll("\n", "").replace("\\", "");
                    if(signature.equals(readTxt("firmavacia.txt"))){
                        requireActivity().runOnUiThread(() -> {
                            binding.textView5.setVisibility(View.VISIBLE);
                            binding.buttonClear.setEnabled(true);
                            binding.loadSignature.setVisibility(View.INVISIBLE);
                            binding.siguiente.setVisibility(View.VISIBLE);
                            binding.buttonClear.setEnabled(true);
                            binding.siguiente.setEnabled(true);
                            mView.setDrawingCacheEnabled(false);
                        });
                        return;
                    }

                    SignatureModel signatureModel = new SignatureModel();
                    signatureModel.setBase64Signature(signature.replaceAll("\n", "").replace("\\", ""));

                    String jsonSignature = new Gson().toJson(signatureModel);

                    String signaturePath = writeToFile(
                            requireActivity().getFilesDir(),
                            jsonSignature);

                    ActiveEnrollment activeEnrollment = new ActiveEnrollment();
                    activeEnrollment.setEnroll_id(enrollTemp);
                    activeEnrollment.setState_id("1");
                    activeEnrollment.setDate(dateEnrollment());

                    activeEnrollment.setSignature_id("1");
                    activeEnrollment.setJson_signature(signaturePath);
                    activeEnrollment.setTipo_enroll("Nueva");
                    if(DatosRecolectados.typeEnrollActivate == null){
                        activeEnrollment.setTipo_enroll("Nueva");
                    }else{
                        activeEnrollment.setSolicitante_id(DatosRecolectados.activeEnrollmentTempAvalesCoacredit.getSolicitante_id());
                        activeEnrollment.setTipo_enroll(DatosRecolectados.typeEnrollActivate);
                        activeEnrollment.setEnroll_solicitante(DatosRecolectados.activeEnrollmentTempAvalesCoacredit.getEnroll_id());
                    }
                    DatosRecolectados.typeEnrollActivate = null;
                    dataBase.insertEnroll(activeEnrollment);
                    DatosRecolectados.activeEnrollment = activeEnrollment;
                   /* Data.Builder data = new Data.Builder();
                    data.putString("nombre", "Antonio");
                    data.putString("paterno", "Huerta");
                    data.putString("materno", "Nuñez");
                    //data.putInt("solicitanteId", idSolicitante);
                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SignatureBackWok.class).setInputData(data.build())
                            .build();
                    WorkManager.getInstance(requireContext())
                            .beginWith(Collections.singletonList(workRequest))
                            .enqueue();*/
                    nextFragment();

                } catch (Throwable e) {
                    Log.e(TAG, "Error saveSignature()", e);
                }finally {
                    Log.i(TAG, "Base de datos cerrada");
                    dataBase.close();
                }
            });
        });
    }

    private void nextFragment(){
        requireActivity().runOnUiThread(() -> {
            ((Main2Activity) getActivity()).replaceFragments(IdentificacionFragment.class);
        });
    }

    private String createSignatureBase64() {
        Bitmap bitmap = binding.linearLayout.getDrawingCache();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }




    public class signature extends View {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void clear() {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            //btnSiguiente.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }
            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }



    private String readTxt(String file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(requireContext().getAssets().open(file), "UTF-8"));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                mLine += line + "\n";
            }
            return mLine;
        } catch (IOException e) {
            //log the exception
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }
}