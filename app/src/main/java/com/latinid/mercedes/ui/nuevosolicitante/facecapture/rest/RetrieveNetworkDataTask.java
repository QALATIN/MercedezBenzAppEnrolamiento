package com.latinid.mercedes.ui.nuevosolicitante.facecapture.rest;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RetrieveNetworkDataTask  {

    private static final String FETCH_CONFIG_TAG = "fetch_config_tag";
    private RetrieveNetworkDataListener mNetworkDataListener;

    private String mRequestData;
    private boolean mParseResponse;
    private String mProcessResponse;
    private int mNexaFaceThreshold;
    private int mFetchDataStatus = 0;
    private Context mContext;
    private String mUrl;
    private ExecutorService mExecutor;
    private Handler mHandler;



    /**
     * Implemented by any class requiring notification of Initialization complete
     */
    public interface RetrieveNetworkDataListener {
        void onNetworkDataRetrieveStarted();
        void onNetworkDataRetrieveComplete(int success);
    }

    public RetrieveNetworkDataTask(RetrieveNetworkDataListener listener, Context context, String url, String username) {
        mRequestData = username;
        mUrl = url;
        mNetworkDataListener = listener;
        mContext = context;

        mExecutor = Executors.newSingleThreadExecutor();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public String getServerResponse() {
        return mProcessResponse;
    }

    public void Execute(String urlComponent) {
        mExecutor.execute(() -> {
            try {
                doInBackground(urlComponent);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mHandler.post(() -> {
                onPostExecute();
            });
        });
    }

    private Long doInBackground(String param) {
        PostOperation postOperation = null;
        mParseResponse = false;
        String url = mUrl;

        if (param != "")
            postOperation = new PostOperation(url, "", param + '/' + mRequestData);
        else
            postOperation = new PostOperation(url, "", mRequestData);

        if (mNetworkDataListener != null)
            mNetworkDataListener.onNetworkDataRetrieveStarted();

        try {
            mProcessResponse = postOperation.doGetResponse();
            testBadResponse(mProcessResponse);
        } catch (MalformedURLException e) {
            Log.v(FETCH_CONFIG_TAG, "Fetch MalformedURL: " + e.getMessage());
            return null;
        }

        return 0L;
    }


    private void onPostExecute() {
        if (mNetworkDataListener != null) {
            mNetworkDataListener.onNetworkDataRetrieveComplete(mFetchDataStatus);
        }
    }

    private void testBadResponse(String response) {
        if (response.startsWith(PostOperation.POST_FAILED_KEY)) {
            mFetchDataStatus = -1;
            return;
        }
        try {
            if (response.equals("401")) {
                mFetchDataStatus = 401;
                return;
            }
        }
        catch(Exception e)
        {
            mFetchDataStatus = -2;
            Log.e(FETCH_CONFIG_TAG, "Failed to parse NexaFace response: " + response);
            Log.e(FETCH_CONFIG_TAG, "Failed to parse NexaFace, exception: " + e.getMessage());
        }
    }

//    private void parseGetNexaFaceResponse(String response) {
//        Log.v(FETCH_CONFIG_TAG, "Fetch Config NexaFace Response: " + response);
//        if (response.startsWith(PostOperation.POST_FAILED_KEY)) {
//            return;
//        }
//        try {
//            if (response.equals("401")) {
//                mFetchDataStatus = 401;
//                return;
//            }
//            JSONObject jsonRoot = new JSONObject(response);
//            mNexaFaceThreshold = (jsonRoot.getInt("nexa_face_threshold"));
//            Log.d(FETCH_CONFIG_TAG, "nexa_face_threshold: " + mNexaFaceThreshold);
//
//        } catch (JSONException e) {
//            Log.e(FETCH_CONFIG_TAG, "Failed to parse NexaFace response: " + response);
//            Log.e(FETCH_CONFIG_TAG, "Failed to parse NexaFace, exception: " + e.getMessage());
//        }
//    }
}