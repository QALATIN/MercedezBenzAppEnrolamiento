package com.latinid.mercedes.ui.nuevosolicitante.selfie.rest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;


import com.latinid.mercedes.DatosRecolectados;

import com.latinid.mercedes.Main2Activity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;


/**
 * Created by hyim on 11/20/2017.
 *
 * This is an utility class that connects to the Aware servers.
 */

public class RestClientTask
{
    public enum ResultType { NONE, CAPTURE_VIDEO_LIVE, CAPTURE_VIDEO_TRY_AGAIN, CAPTURE_VIDEO_AUTOCAPTURE};
    private Main2Activity mActivity;
    private ClientTask clientTask = null;

    private String mServerUrl = "https://mobileauth.aware-demos.com/faceliveness";
    private String mBase64ImageData;
    private Bitmap mDecodedBitmap;
    private int mMatchThresholdValue = 3;

    public RestClientTask(Main2Activity activity) {
        mActivity = activity;
        mBase64ImageData = null;
        mDecodedBitmap = null;
    }

    // pass in JSON

    public void executeLiveness(String videoPackage, int threshold) {
        mMatchThresholdValue = threshold;
        clientTask = new ClientTask();
        clientTask.execute(mServerUrl, videoPackage, "analyze/");
    }

    public boolean isRunningOrPending() {
        if (clientTask != null && clientTask.getStatus() != AsyncTask.Status.FINISHED) {
            return true;
        }
        return false;
    }

    public boolean saveAutocapureImage(String name, Bitmap decodedbitmap) {
        File extDir = mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        OutputStream outstream = null;

        File file = new File(extDir, name +".jpg");
        try {
            outstream = new FileOutputStream(file);
            decodedbitmap.compress(Bitmap.CompressFormat.JPEG, 85, outstream);
            outstream.flush();
            outstream.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public void base64ToBitmapImage(String base64Image) {
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        mDecodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        //DatosRecolectados.faceBitmap = mDecodedBitmap;
    }

    public int parseAutoCaptureResponse(JSONObject jsonRoot) {
        int  response = -1;
        try {
            if (jsonRoot.has("video")) {
                JSONObject video = jsonRoot.getJSONObject("video");
                if (video.has("autocapture_result")) {
                    JSONObject autocapture = video.getJSONObject("autocapture_result");
                    if (autocapture.has("error")) {
                        response = -1;
                    } else if (autocapture.has("captured_frame")) {
                        mBase64ImageData = autocapture.getString("captured_frame");
                        if (mBase64ImageData != null){
                            System.out.println("Chido");
                            /*IdentificacionModel identificacionModel = DatosRecolectados.identificacion;
                            new Extras().getComparacionFacial(identificacionModel.getFotoRecorteB64(), mBase64ImageData);
                            DatosRecolectados.faceB64=mBase64ImageData;
                            DatosRecolectados.persona.setFotoSelfieB64(mBase64ImageData);*/
                            //base64ToBitmapImage(mBase64ImageData);
                            DatosRecolectados.selfieB64=mBase64ImageData;
                            response = 1;
                        }
                    } else
                        response = -1;
                }
            }
            else if (jsonRoot.has("autocapture_result")) {
                JSONObject autocapture = jsonRoot.getJSONObject("autocapture_result");
                if (autocapture.has("error")) {
                    response = -1;
                } else if (autocapture.has("captured_frame")) {
                    mBase64ImageData = autocapture.getString("captured_frame");
                    if (mBase64ImageData != null)
                        base64ToBitmapImage(mBase64ImageData);
                    response = 1;
                } else
                    response = -1;
            }
            else
                response = -1;
        }
        catch (JSONException e) {
            response = -1;
        }
        return response;
    }

    public int parseLivenessResponse(JSONObject jsonRoot) {
        int  response = -1;
        try {
            // let's get face_liveness
            if (jsonRoot.has("video")) {
                JSONObject video = jsonRoot.getJSONObject("video");
                if (video.has("liveness_result")) {
                    JSONObject liveness_result = (JSONObject) video.get("liveness_result");
                    int score = liveness_result.getInt("score");

                    if (score == 100) {
                        System.out.println("Chido live");
                       /* DatosRecolectados.persona.setPruebaDeVida("true");
                        DatosRecolectados.vivo = true;*/
                        response = 1;
                    }else
                        response = score;
                } else {
                    response = -2;
                }
            } else if (jsonRoot.has("liveness_result")) {
                JSONObject liveness_result = (JSONObject) jsonRoot.get("liveness_result");
                int score = liveness_result.getInt("score");

                if (score == 100)
                    response = 1;
                else
                    response = score;
            } else
                response = -2;
        }
        catch (JSONException e) {
            response = -2;
        }
        return response;
    }

    public ResultType parseCaptureOnlyServerResults(String result) {
        int livenessResult = -1;
        ResultType resultType = ResultType.NONE;
        int autocaptureResult = -1;

        try {
            JSONObject jsonRoot = new JSONObject(result);
            autocaptureResult = parseAutoCaptureResponse(jsonRoot);
            livenessResult = parseLivenessResponse(jsonRoot);

            if (autocaptureResult> 0 && livenessResult > 0) {
                if (livenessResult >=0) {
                    if (livenessResult == 1) {
                        resultType = ResultType.CAPTURE_VIDEO_LIVE;
                    }
                    else {
                        resultType = ResultType.CAPTURE_VIDEO_TRY_AGAIN;
                    }
                }
                else {
                    resultType = ResultType.CAPTURE_VIDEO_TRY_AGAIN;
                }
            }
            else  {
                if (autocaptureResult >=0) {
                    if (autocaptureResult == 1) {
                        resultType = ResultType.CAPTURE_VIDEO_AUTOCAPTURE;
                    }
                    else {
                        resultType = ResultType.CAPTURE_VIDEO_TRY_AGAIN;
                    }
                }
                else {
                    resultType = ResultType.CAPTURE_VIDEO_TRY_AGAIN;
                }

                if (livenessResult >=0) {
                    if (livenessResult == 1) {
                        resultType = ResultType.CAPTURE_VIDEO_LIVE;
                    }
                    else {
                        resultType = ResultType.CAPTURE_VIDEO_TRY_AGAIN;
                    }
                }
                else if (livenessResult != -2){
                    resultType = ResultType.CAPTURE_VIDEO_TRY_AGAIN;
                }

                if ((autocaptureResult<0) && (livenessResult<0)){
                    resultType = ResultType.CAPTURE_VIDEO_TRY_AGAIN;
                }
            }
        }
        catch (JSONException e) {
            resultType = ResultType.CAPTURE_VIDEO_TRY_AGAIN;
        }

        return resultType;
    }

    class ClientTask extends AsyncTask<String, Integer, Long> {
        String mResult;
        protected Long doInBackground(String... params) {

            PostOperation post = new PostOperation(params[0], params[1], params[2]); // url, data, operation
            mResult = "";
            try {
                mResult = post.doPostJson();
            } catch (MalformedURLException e) {
                //Log.e(NEXA_FACE_REST_CLIENT_TAG, "Malformed URL: " + e.getMessage());
            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Long res) {

            // thread as this operation is time consuming
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String message = null;
                    int icon=0;
                    if (mResult.contains("Failed")) {

                    }
                    ResultType Result = parseCaptureOnlyServerResults(mResult);
                    if (Result == ResultType.CAPTURE_VIDEO_AUTOCAPTURE) {

                    } else if (Result == ResultType.CAPTURE_VIDEO_LIVE) {
                        System.out.println("Terminado");
                        DatosRecolectados.proofLifeSelfie= true;
                  /*      DatosRecolectados.vivo = true;
                        DatosRecolectados.persona.setPruebaDeVida("true");*/
                    }
                    DatosRecolectados.selfieFinish=true;
                }
            }).start();
        }
    }
}