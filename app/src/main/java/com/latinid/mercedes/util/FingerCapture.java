package com.latinid.mercedes.util;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintImage;
import cn.com.aratek.fp.FingerprintScanner;
import cn.com.aratek.util.Result;

public class FingerCapture extends Fragment {

    private static final String TAG = "FingerCapture.class";
    private FingerprintScanner mFingerprintScanner;
    private static String FP_DB_PATH = "/sdcard/fpMBBLogin.db";

    private AtomicInteger mStatus;
    private AtomicInteger mStatusCap;
    private static final int STATUS_CLOSED = 0;
    private static final int STATUS_OPENED = 1;
    private static final int STATUS_CLOSING = 2;
    private static final int STATUS_OPENING = 3;
    private static final int STATUS_ENROLL = 4;
    public boolean cancelCap = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFingerprintScanner = FingerprintScanner.getInstance(requireContext());
        mStatus = new AtomicInteger(STATUS_CLOSED);
        mStatusCap = new AtomicInteger(STATUS_CLOSED);
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(() -> startScanner()).start();
    }

    public FingerprintImage waitFinger() {

        FingerprintImage fi = null;
        Result res;
        mFingerprintScanner.prepare();

        do {

            if (cancelCap) {
                cancelCap = false;
                Log.d("TAG", "cancelada");
                break;
            }

            res = mFingerprintScanner.capture();
            fi = (FingerprintImage) res.data;
            if (fi != null) {
                if (Bione.getFingerprintQuality(fi) > 60) {
                    if (res.error != FingerprintScanner.NO_FINGER ) {
                        break;
                    }
                }
                //Log.i(TAG, "Fingerprint image quality is " + Bione.getFingerprintQuality(fi));
            }
        } while (true);
        mFingerprintScanner.finish();
        return fi;
    }

    public void startScanner() {
        try {
            mStatus.set(STATUS_OPENING);
            mFingerprintScanner.powerOn(); // ignore power on errors
            int error;
            Result res;
            error = mFingerprintScanner.open();
            if (error == FingerprintScanner.RESULT_OK) {
                //System.out.println( requireContext().getFilesDir().getAbsolutePath());
                FP_DB_PATH = requireContext().getFilesDir().getAbsolutePath()+File.separator+"fpMBB.db";
                mStatus.set(STATUS_OPENED);
                res = mFingerprintScanner.getFirmwareVersion();
                res = mFingerprintScanner.getSerial();
                mFingerprintScanner.setLfdLevel(0);

                // initialize fingerprint algorithm
                Log.i("TAG", "Fingerprint algorithm version: " + Bione.getVersion());
                if ((error = Bione.initialize(requireContext(), FP_DB_PATH))
                        != Bione.RESULT_OK) {
                }
            } else {
                mFingerprintScanner.powerOff(); // ignore power off errors
                mStatus.set(STATUS_CLOSED);
            }
        }catch (Throwable e){
            e.printStackTrace();
            BinnacleCongif.writeLog(TAG,1, "Error iniciando dispositivo de huellas -> function: startScanner()", "Error: "+e.getMessage()+"|"+e.getLocalizedMessage()+"|"+e.getCause().getMessage(), requireContext());
            //BinnacleCongif.writeLog("FingerCapture",1, "ERROR: "+e.getLocalizedMessage()+","+e.getMessage()+","+e.getCause().toString(), "startScanner()", requireContext());
        }

    }

    public void closeScanner() {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (STATUS_CLOSED == mStatus.get()) {
                        return;
                    }
                    mFingerprintScanner.finish();
                    mStatus.set(STATUS_CLOSING);
                    int error;
                    Bione.exit();
                    error = mFingerprintScanner.close();
                    if (error == FingerprintScanner.RESULT_OK) {
                        mStatus.set(STATUS_CLOSED);
                    } else {

                    }
                    mFingerprintScanner.powerOff(); // ignore power off errors
                }catch (Throwable e){
                    BinnacleCongif.writeLog(TAG,1, "Error cerrando dispositivo de huellas -> function: closeScanner()", "Error: "+e.getMessage()+"|"+e.getLocalizedMessage()+"|"+e.getCause().getMessage(), requireContext());
                   e.printStackTrace();
                }

            }
        }.start();

    }

    @Override
    public void onPause() {
        super.onPause();
        closeScanner();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeScanner();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
