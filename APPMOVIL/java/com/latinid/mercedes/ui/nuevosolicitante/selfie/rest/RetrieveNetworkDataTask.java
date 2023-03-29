package com.latinid.mercedes.ui.nuevosolicitante.selfie.rest;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

/**
 * Created by stoth on 3/14/2017.
 */

public class RetrieveNetworkDataTask extends AsyncTask<String, Void, Long> {

    private static final String FETCH_CONFIG_TAG = "fetch_config_tag";
    private RetrieveNetworkDataListener mNetworkDataListener;

    private String mRequestData;
    private boolean mParseResponse;
    private String mProcessResponse;
    private int mNexaFaceThreshold;
    private int mFetchDataStatus = 0;
    private Context mContext;
    private String mUrl;

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
    }

    public String getServerResponse() {
        return mProcessResponse;
    }

    @Override
    protected Long doInBackground(String... params) {
        PostOperation postOperation = null;
        mParseResponse = false;
        String url = mUrl;

        if (params[0] != "")
            postOperation = new PostOperation(url, "", params[0] + '/' + mRequestData);
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

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Long aLong) {
        if (mNetworkDataListener != null) {
            mNetworkDataListener.onNetworkDataRetrieveComplete(mFetchDataStatus);
        }
        super.onPostExecute(aLong);
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

    private void parseGetNexaFaceResponse(String response) {
        Log.v(FETCH_CONFIG_TAG, "Fetch Config NexaFace Response: " + response);
        if (response.startsWith(PostOperation.POST_FAILED_KEY)) {
            return;
        }
        try {
            if (response.equals("401")) {
                mFetchDataStatus = 401;
                return;
            }
            JSONObject jsonRoot = new JSONObject(response);
            mNexaFaceThreshold = (jsonRoot.getInt("nexa_face_threshold"));
            Log.d(FETCH_CONFIG_TAG, "nexa_face_threshold: " + mNexaFaceThreshold);

        } catch (JSONException e) {
            Log.e(FETCH_CONFIG_TAG, "Failed to parse NexaFace response: " + response);
            Log.e(FETCH_CONFIG_TAG, "Failed to parse NexaFace, exception: " + e.getMessage());
        }
    }
}
