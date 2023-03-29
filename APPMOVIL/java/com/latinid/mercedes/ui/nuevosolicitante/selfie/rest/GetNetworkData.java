package com.latinid.mercedes.ui.nuevosolicitante.selfie.rest;

import android.content.Context;
import android.util.Log;


/**
 * Created by mbramley on 3/1/2018.
 */

public class GetNetworkData implements RetrieveNetworkDataTask.RetrieveNetworkDataListener, Runnable {

    private static final String GET_NETWORK_DATA_TAG = "GET_NETWORK_DATA";
    private static final int SPLASH_TIME = 3000;
    private RetrieveNetworkDataTask mGetNetworDataTask;
    private boolean mIsInitializing;
    private Context mContext;
    private String mUrl;
    private String mUrlComponent;
    private GetNetworkDataListener mGetNetworkDataListener = null;
    private NetworkDataResult mNetworkResult;
    private String mRequest;

    /**
     * Implemented by any class requiring notification of Initialization complete
     */
    public interface GetNetworkDataListener {
        void onStarted();
        void onRetrieveComplete(int success, String result);
    }


    GetNetworkData(GetNetworkDataListener listener, Context context, String url, String urlComponent, String usernamne) {
        mGetNetworkDataListener = listener;
        mContext = context;
        mUrl = url;
        mUrlComponent = urlComponent;
        mRequest = usernamne;
        mNetworkResult = new NetworkDataResult();
    }

    public NetworkDataResult begin() {
        return mNetworkResult;
    }

    @Override
    public void run() {
        mIsInitializing = true;
        mGetNetworDataTask = new RetrieveNetworkDataTask(this, mContext, mUrl, mRequest);
        mGetNetworDataTask.execute(mUrlComponent);

        synchronized (GetNetworkData.this) {
            while (mIsInitializing) {
                try {
                    GetNetworkData.this.wait(SPLASH_TIME);
                } catch (InterruptedException e) {
                    Log.e(GET_NETWORK_DATA_TAG, "Config Settings Launch interrupted: " + e.getMessage());
                    break;
                }
            }
        }
    }

    @Override
    public void onNetworkDataRetrieveStarted() {

    }

    @Override
    public void onNetworkDataRetrieveComplete(int success) {
        String result = null;

        result = mGetNetworDataTask.getServerResponse();
        mNetworkResult.mErrorCode = success;
        mNetworkResult.mResultingData = result;
        if (mGetNetworkDataListener != null) {
            mGetNetworkDataListener.onRetrieveComplete(success,  result);
        }


        mIsInitializing = false;
    }
}

