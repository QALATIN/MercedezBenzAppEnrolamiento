package com.latinid.mercedes.ui.nuevosolicitante.facecapture.rest;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.latinid.mercedes.DatosRecolectados;
import com.latinid.mercedes.Main3Activity;
import com.latinid.mercedes.Main3Activity;
import com.latinid.mercedes.ui.nuevosolicitante.facecapture.util.Results;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestClientTask
{
    public enum ResultType { NONE, CAPTURE_VIDEO_LIVE, CAPTURE_VIDEO_TRY_AGAIN, CAPTURE_VIDEO_AUTOCAPTURE};
    private Main3Activity mActivity;
    SharedPreferences sharedPref;
    private String mServerUrl ="";
    private String mBase64ImageData;
    private Bitmap mDecodedBitmap;
    private int mMatchThresholdValue = 3;
    private String mEndpoint = "checkLiveness/";

    public RestClientTask(Main3Activity activity) {
        mActivity = activity;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);

        mServerUrl = sharedPref.getString("pref_faceliveness_server_url", mServerUrl);
        mBase64ImageData = null;
        mDecodedBitmap = null;
    }

    // pass in JSON
    public void executeLiveness(String videoPackage, int threshold) {
        mMatchThresholdValue = threshold;
        RunablePostOperation postOperation = new RunablePostOperation(mServerUrl, videoPackage, mEndpoint);
        RunnableProcessPostOperationResults postResultOperation = new RunnableProcessPostOperationResults();
        TaskExecutor runNetworkTask = new TaskExecutor();
        runNetworkTask.SetTask(postOperation, postResultOperation);
        runNetworkTask.Execute();

    }
    public void executeLiveness(String videoPackage, int threshold, String endpoint) {
        mMatchThresholdValue = threshold;
        RunablePostOperation postOperation = new RunablePostOperation(mServerUrl, videoPackage, endpoint);
        RunnableProcessPostOperationResults postResultOperation = new RunnableProcessPostOperationResults();
        TaskExecutor runNetworkTask = new TaskExecutor();
        runNetworkTask.SetTask(postOperation, postResultOperation);
        runNetworkTask.Execute();

    }


    private boolean saveAutocapureImage(String name, Bitmap decodedbitmap) {
        File extDir = mActivity.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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

    private void base64ToBitmapImage(String base64Image) {
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        mDecodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    protected int parseAutoCaptureResponse(JSONObject jsonRoot) {
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
                        if (mBase64ImageData != null)
                            //base64ToBitmapImage(mBase64ImageData);
                            DatosRecolectados.selfieB64=mBase64ImageData;
                        response = 1;
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

    protected int parseLivenessResponse(JSONObject jsonRoot) {
        int  response = -1;
        try {
            // let's get face_liveness
            if (jsonRoot.has("video")) {
                JSONObject video = jsonRoot.getJSONObject("video");
                if (video.has("liveness_result")) {
                    JSONObject liveness_result = (JSONObject) video.get("liveness_result");
                    int score = liveness_result.getInt("score");

                    if (score == 100)
                        response = 1;
                    else
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

    protected ResultType parseCaptureOnlyServerResults(String result) {
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

    protected Results parseCheckLivenessResults(String result) {
        Results livenessResult = new Results();

        try {
            JSONObject jsonRoot = new JSONObject(result);

            if (jsonRoot.has("video")) {
                JSONObject video = jsonRoot.getJSONObject("video");

                if (video.has("liveness_result")) {
                    JSONObject liveness_result = video.getJSONObject("liveness_result");
                    if(liveness_result.has ("score")) {
                        livenessResult.score = liveness_result.getDouble("score");
                    }
                    if(liveness_result.has ("decision")) {
                        livenessResult.decision = liveness_result.getString("decision");
                        livenessResult.live = (livenessResult.decision.equals("LIVE")) ? true : false;
                        livenessResult.score = (livenessResult.decision.equals("LIVE")) ? 100 : 0;
                    }

                    if (liveness_result.has("high_quality_capture"))
                        livenessResult.high_quality_capture = liveness_result.getBoolean("high_quality_capture");
                    else
                        livenessResult.high_quality_capture = false;

                    if (liveness_result.has("likely_to_be_live_result"))
                        livenessResult.likely_live_capture = liveness_result.getBoolean("likely_to_be_live_result");
                    else
                        livenessResult.likely_live_capture = false;
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        } catch (JSONException e) {
            return null;
        }

        return livenessResult;
    }


    public class TaskExecutor {
        private ExecutorService mExecutor;
        private Handler mHandler;
        private RunablePostOperation mBackgroundTask;
        private RunnableProcessPostOperationResults mUITask;
        String mResult;
        TaskExecutor() {
            mExecutor = Executors.newSingleThreadExecutor();
            mHandler = new Handler(Looper.getMainLooper());
        }

        public void SetTask(RunablePostOperation task, RunnableProcessPostOperationResults uiTask) {
            mBackgroundTask = task;
            mUITask = uiTask;
        }

        public void Execute() {
            mExecutor.execute(() -> {
                try {
                    mResult = mBackgroundTask.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mUITask.SetData(mResult);
                mHandler.post(() -> {
                    mUITask.call();
                });
            });
        }

        public void Terminate() {
            mExecutor.shutdownNow();
        }
    }


    class RunablePostOperation extends PostOperation implements Callable {
        String mResult;

        public RunablePostOperation(String url, String upload, String operation) {
            super(url, upload, operation);
        }

        String GetResult() { return mResult; }

        @Override
        public String call() {
            try {
                mResult = this.doPostJson();
            } catch (MalformedURLException e) {
                //Log.e(NEXA_FACE_REST_CLIENT_TAG, "Malformed URL: " + e.getMessage());
            }
            return mResult;
        }
    }

    class RunnableProcessPostOperationResults implements Callable {
        String mResult;
        public RunnableProcessPostOperationResults() {
            mResult = null;
        }

        public void SetData(String s) {
            mResult = s;
        }

        @Override
        public String call() {
            String message = null;
            int icon=0;

            if (mResult.contains("Failed")) {
               /* icon = R.drawable.info_icon;
                mActivity.HideHourglass();
                mActivity.ShowDialog(mActivity.getApplicationContext().getResources().getString(R.string.server_result),
                        "Server Error",
                        icon, null);*/
                return "Failed";
            }


            if (mEndpoint.contains("checkLiveness")) {
                Results livenessResult = parseCheckLivenessResults(mResult);
                if (livenessResult.live) {

                    DatosRecolectados.proofLifeSelfie= true;
                    /*message = mActivity.getApplicationContext().getResources().getString(R.string.video) + " - " +
                            mActivity.getApplicationContext().getResources().getString(R.string.live);*/
                }
                else {
                  /*  message = mActivity.getApplicationContext().getResources().getString(R.string.video) + " - " +
                            mActivity.getApplicationContext().getResources().getString(R.string.try_again);*/
                }
            }
            else {
                ResultType Result = parseCaptureOnlyServerResults(mResult);
                if (Result == ResultType.CAPTURE_VIDEO_AUTOCAPTURE) {
                   /* message = mActivity.getApplicationContext().getResources().getString(R.string.video) + " - " +
                            mActivity.getApplicationContext().getResources().getString(R.string.image_captured);*/
                }
                else if (Result == ResultType.CAPTURE_VIDEO_LIVE) {
                    DatosRecolectados.proofLifeSelfie= true;
                   /* message = mActivity.getApplicationContext().getResources().getString(R.string.video) + " - " +
                            mActivity.getApplicationContext().getResources().getString(R.string.live);*/
                } else {
                    /*message = mActivity.getApplicationContext().getResources().getString(R.string.video) + " - " +
                            mActivity.getApplicationContext().getResources().getString(R.string.try_again);*/
                }


            }
            DatosRecolectados.selfieFinish=true;
          /*  mActivity.HideHourglass();
            mActivity.ShowDialog(mActivity.getApplicationContext().getResources().getString(R.string.capture), message, 0, mDecodedBitmap);*/
            return "Success";
        }
    }


}
