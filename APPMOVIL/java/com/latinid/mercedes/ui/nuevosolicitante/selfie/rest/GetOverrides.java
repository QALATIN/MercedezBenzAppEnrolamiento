package com.latinid.mercedes.ui.nuevosolicitante.selfie.rest;

import android.content.Context;
import android.os.ConditionVariable;

/**
 * Created by mbramley on 3/5/2018.
 */

public class GetOverrides implements GetNetworkData.GetNetworkDataListener {
    private NetworkDataResult mResult;
    private ConditionVariable mTaskComplete;
    private GetOverridesListener mListener;
    private GetNetworkData mFetchOverridesTask;
    private Thread mThread = null;

    public interface GetOverridesListener {
        void onGetOverridesStarted();
        void onGetOverridesComplete();
        void onGetOverridesDataComplete(int success, String result);
    }

    public GetOverrides() {
        mResult = new NetworkDataResult();
    }

    public void getOverrides(GetOverridesListener activity, Context context, String url, ConditionVariable taksComplete, String model) {
        mTaskComplete = taksComplete;
        mListener = activity;
        mThread = new Thread(mFetchOverridesTask = new GetNetworkData(this, context, url, "Android", model) );
        mThread.start();
    }

    public void interrupt() {
        if (mThread != null)
            mThread.interrupt();
    }

    public NetworkDataResult getOverridesData() {
        return mResult;
    }

    @Override
    public void onStarted() {
        if (mListener != null)
            mListener.onGetOverridesStarted();
    }

    @Override
    public void onRetrieveComplete(int success, String result) {
        mResult.mErrorCode = success;
        mResult.mResultingData = result;
        mTaskComplete.open();
        mThread = null;

        if (mListener != null) {
            mListener.onGetOverridesComplete();
            mListener.onGetOverridesDataComplete(success, result);
        }
    }
}
