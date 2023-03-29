package com.latinid.mercedes.ui.nuevosolicitante.facecapture.api;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.aware.facecapture.AutoCaptureFeedback;
import com.aware.facecapture.CameraOrientation;
import com.aware.facecapture.CameraPosition;
import com.aware.facecapture.CaptureSessionStatus;
import com.aware.facecapture.EncryptionType;
import com.aware.facecapture.FaceCaptureException;
import com.aware.facecapture.FaceCaptureJNI;
import com.aware.facecapture.IFaceCapture;
import com.aware.facecapture.PackageType;
import com.aware.facecapture.Rectangle;
import com.aware.facecapture.WorkflowProperty;

import com.latinid.mercedes.Main3Activity;
import com.latinid.mercedes.ui.nuevosolicitante.facecapture.api.exceptions.FaceCaptureApiException;


import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FaceCaptureApi {
    private static String TAG = FaceCaptureApi.class.getSimpleName();
    private static FaceCaptureApi sFaceCaptureApi;
    private FaceCaptureJNI mFaceCapture;
    private FaceCaptureJNI.IWorkflow mWorkFlow;

    private ApiData mSessionData;

    private PollingSessionStateTask mPollingSessionStateTask;

    private PollingSessionStateListener mPollingSessionStateListener;

    private AtomicBoolean mCaptureInProcess = new AtomicBoolean(false);

    private ExecutorService mStatusExecutor;

    private PackageType mPackageType = PackageType.HIGH_USABILITY;
    private CameraPosition mCameraPosition = CameraPosition.FRONT;
    private CameraOrientation mCameraOrientation = CameraOrientation.PORTRAIT;
    private int mCameraIndex = 0;
    private Main3Activity mActivity;

    private AtomicInteger updateCount = new AtomicInteger(0);
    private int count = 0;
    private int mCaptureTimeoutDuration = 2;
    private Handler mTimeoutHandler = null;
    IFaceCapture.ICamera[] mCameraList = null;

    public boolean isWorkflowInProgress() {
        return mCaptureInProcess.get();
    }

    public static class ApiData {
        public String workflow;
        public String userName;
        public Double captureTimeout;
        public byte[] profileData;
        public String packageType;
        public String cameraPosition;
        public String cameraOrientation;
    }

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("TIMER", "FPS:........." + updateCount.get()/5);
            updateCount.set(0);
            timerHandler.postDelayed(this, 5000);
        }
    };


    public boolean isWorkflowValid() {return mWorkFlow != null;}


    /**
     * Implemented by any class requiring notification of Initialization complete
     */
    public interface PollingSessionStateListener {
        void onPollingStatusListener(CaptureSessionStatus status, byte[] frame, AutoCaptureFeedback feedback);
        void initRegionOfInterest(Rectangle rect);
    }

    //To application.
    public void setPollingStatusListener(PollingSessionStateListener listener) throws Exception {
        mPollingSessionStateListener = listener;
    }

    public static synchronized FaceCaptureApi getInstance(Main3Activity context) throws Exception {
        if (sFaceCaptureApi == null) {
            sFaceCaptureApi = new FaceCaptureApi();
        }
        sFaceCaptureApi.mActivity = context;
        if (sFaceCaptureApi.mFaceCapture == null) {
            try {
                sFaceCaptureApi.mFaceCapture = new FaceCaptureJNI();
            } catch (FaceCaptureException e) {
                Log.e(TAG, "getInstance exception " + e.getMessage());
                throw new Exception(e);
            }
            catch (Exception e) {
                Log.e(TAG, "getInstance exception " + e.getMessage());
                throw new Exception(e);
            }
            catch (UnsatisfiedLinkError e) //this is not an exception
            {
                Log.e(TAG, "getInstance UnsatisfiedLinkError " + e.getMessage());
                throw new Exception(e);
            }
        }
        return sFaceCaptureApi;
    }

    //Create a workflow.
    private void allocateWorkflow(String data) throws Exception {
        String workflowName = null;
        try {
            Log.d(TAG, "allocateWorkflow input " + data);
            if(data.toLowerCase().equals("foxtrot")) {
                workflowName = FaceCaptureJNI.FOXTROT;
            }
            else if(data.toLowerCase().equals("charlie")) {
                workflowName = FaceCaptureJNI.CHARLIE;
            }
            Log.d(TAG, "allocateWorkflow create workflow " + workflowName);
            mWorkFlow = mFaceCapture.workflowCreate(workflowName);
        } catch (FaceCaptureException e) {
            Log.d(TAG, "allocateWorkflow error on create " + e.getMessage());
            throw new Exception(e);
        }
    }

    public void DestroyWorkflow() {
        try {
            if (mWorkFlow != null) {
                mWorkFlow.destroy();
                mWorkFlow = null;
                mCaptureInProcess.set(false);
            }
        }
        catch (Exception e) {

        }
    }

    //Get a face server package.
    public String getServerPackage() throws Exception {
        String json = "";
        try {
            json = mFaceCapture.getServerPackage(mWorkFlow, mPackageType);
        } catch (FaceCaptureException e) {
            throw new Exception(e);
        }
        return json;
    }

    public String getEncryptedServerPackage(EncryptionType encryptionType, String public_key) throws Exception {
        String json = "";
        try {
            json = mFaceCapture.getEncryptedServerPackage(encryptionType, public_key, mWorkFlow, mPackageType);
        } catch (FaceCaptureException e) {
            throw new Exception(e);
        }
        return json;
    }


    private void SetCameraOrientation(String value) {
        String orientation = value.toLowerCase();
        if (orientation.equals("portrait")) {
            mCameraOrientation = CameraOrientation.PORTRAIT;
        }
        else if (orientation.equals("landscape")) {
            mCameraOrientation = CameraOrientation.LANDSCAPE;
        }
    }

    private void SetCameraPosition(String value) {
        String position = value.toLowerCase();

        if (position.equals("front")) {
            mCameraPosition = CameraPosition.FRONT;
        }
        else if (position.equals("back")) {
            mCameraPosition = CameraPosition.BACK;
        }
    }

    private void SetPackageType(String value) {
        String packageType = value.toLowerCase();

        if (packageType.equals("high usability")) {
            mPackageType = PackageType.HIGH_USABILITY;
        }
        else if (packageType.equals("balanced")) {
            mPackageType = PackageType.BALANCED;
        }
        else if (packageType.equals("high security")) {
            mPackageType = PackageType.HIGH_SECURITY;
        }
    }

    //Set properties.
    public void setupSessionData(ApiData session_data) throws Exception {
        mSessionData = session_data;

        try {
            Log.d(TAG, "setupSessionData call create workflow " + session_data.workflow);
            allocateWorkflow(session_data.workflow);
            Log.d(TAG, "setupSessionData after create workflow " + session_data.workflow);
            setWorkflowProperty(PropertyTag.USERNAME, session_data.userName);
            setWorkflowProperty(PropertyTag.CAPTURE_TIMEOUT, session_data.captureTimeout);

            String profile_data = new String(session_data.profileData, StandardCharsets.UTF_8);
            setWorkflowProperty(PropertyTag.CAPTURE_PROFILE, profile_data);

            SetCameraOrientation(mSessionData.cameraOrientation);
            SetCameraPosition(mSessionData.cameraPosition);
            SetPackageType(mSessionData.packageType);

            try
            {
                mCameraList = mFaceCapture.getCameraList(mCameraPosition);

                if (mCameraList.length > 1) {
                    try {
                        SelectCamera(mCameraList);
                    }
                    catch(Exception e) {
                        Toast.makeText(mActivity, "Could not create camera selection menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        throw e;
                    }
                }
                else {
                    mCameraIndex = 0;
                    Log.d(TAG, "setupSessionData cameralist size 1  " + mCameraIndex);
                    mActivity.sessionSetupComplete();
                }
            }
            catch (FaceCaptureException e)
            {
                Log.e(TAG, "setupSessionData Camera list error: " + e.getMessage());
                throw e;
            }
        } catch (Exception e) {
            Log.d(TAG, "setupSessionData error " + e.getMessage());
            throw new Exception(e);
        }
    }
    private void SelectCamera(IFaceCapture.ICamera[] cameraList) throws FaceCaptureException {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mActivity);
        builderSingle.setIcon(null);
        builderSingle.setTitle("Select Camera:-");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.select_dialog_singlechoice);
        for(int i=0;i< cameraList.length;i++) {
            try {
                String name = cameraList[i].getName();
                arrayAdapter.add( name );
            }
            catch(Exception e) {
                throw e;
            }
        }

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                mCameraIndex = which;
                AlertDialog.Builder builderInner = new AlertDialog.Builder(mActivity);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                       mActivity.sessionSetupComplete();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }

    private void startCaptureTimeoutHandler() {
        if (mCaptureTimeoutDuration > 0) {
            mTimeoutHandler = new Handler(Looper.getMainLooper());
            mTimeoutHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mFaceCapture.captureSessionEnableAutocapture(true);
                    }
                    catch (Exception e) {
                        Log.d(TAG, "No capture session " + e.getMessage());
                    }
                }
            }, (long) (mCaptureTimeoutDuration * 1000));
        }
    }


    /**
     *
     * Android Life Cycle procedure called from the fragment running the Face capture API
     *
     */
    public void onStart() throws Exception {
        Rectangle rect;
        try {
            Log.d(TAG, "onStart");
            if (mFaceCapture == null) {
                mFaceCapture = new FaceCaptureJNI();

                setupSessionData(mSessionData);
            }

            if (mWorkFlow == null) {
                throw new FaceCaptureApiException(FaceCaptureApiException.ErrorCode.WorkflowInvalid);
            }

            mCaptureInProcess.set(true);


            if (mWorkFlow != null) {

                try {
                    mCameraList[mCameraIndex].setOrientation(mCameraOrientation);
                    mFaceCapture.startCaptureSession(mWorkFlow, mCameraList[mCameraIndex]);
                    rect = mFaceCapture.captureSessionGetCaptureRegion();
                    mFaceCapture.captureSessionEnableAutocapture(false);
                    mPollingSessionStateListener.initRegionOfInterest(rect);
                    startCaptureTimeoutHandler();
                }
                catch (FaceCaptureException e) {
                    Log.e(TAG, "Exceoption: " + e.getMessage());
                    Toast.makeText(mActivity, "Start Session Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    throw e;
                }
                count++ ;
                mPollingSessionStateTask = new PollingSessionStateTask();
                mPollingSessionStateTask.main();

                mCaptureInProcess.set(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "onStart Exception: " + e.getMessage());
            Toast.makeText(mActivity, "onStartError: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            throw new Exception(e);
        }
    }

    /**
     *
     * Android Life Cycle procedure called from the fragment running the Face capture API
     *
     */
    public void onResume() {
    }

    /**
     *
     * Android Life Cycle procedure called from the fragment running the Face capture API
     *
     */
    public void onPause() {
        Log.d(TAG, "On Pause API: start ");
        if (mFaceCapture != null && mCaptureInProcess.get()) {
            try {
                Log.d(TAG, "On Pause API: stop capture ");
                mFaceCapture.stopCaptureSession();
            } catch (FaceCaptureException e) {
                Log.d(TAG, "On Pause API: stop capture " + e.getMessage());
                e.printStackTrace();
            }

            if (mStatusExecutor != null) {
                while (!mStatusExecutor.isShutdown()) {
                    try {
                        Thread.sleep(100);
                    }
                    catch (Exception e) {
                        Log.w("WARNING", "On Pause API: " + e.getMessage());
                    }
                }
            }
            mCaptureInProcess.set(false);
        }
        Log.d(TAG, "On Pause API: complete ");
    }

    private void setWorkflowProperty(PropertyTag  propertyName, String propertyValue) throws Exception {
        switch (propertyName) {
            case USERNAME:
                try {
                    mWorkFlow.setPropertyString(WorkflowProperty.USERNAME, propertyValue);
                } catch (FaceCaptureException e) {
                    throw new Exception(String.valueOf(-1));
                }
                break;
            case CAPTURE_PROFILE:
                try {
                    mWorkFlow.setPropertyString(WorkflowProperty.CAPTURE_PROFILE, propertyValue);
                } catch (FaceCaptureException e) {
                    throw new Exception(String.valueOf(-1));
                }
                break;
            default:
                throw new Exception(String.valueOf(-1));
        }
    }

    private void setWorkflowProperty(PropertyTag  propertyName, double propertyValue) throws Exception {

        switch (propertyName) {
            case CAPTURE_TIMEOUT:
                if (propertyValue < 0)
                    throw new Exception(String.valueOf(-1));
                try {
                    mWorkFlow.setPropertyDouble(WorkflowProperty.CAPTURE_TIMEOUT, propertyValue);
                } catch (FaceCaptureException e) {
                    throw new Exception(String.valueOf(-1));
                }
                break;
        }
    }

    public enum PropertyTag {
        USERNAME,
        CAPTURE_TIMEOUT,
        CAPTURE_PROFILE,
    }

    public class PollingSessionStateTask {
        //public enum CaptureSessionStatus {
        //    PREPARING,
        //    STARTING,
        //    CAPTURING,
        //    POST_CAPTURE_PROCESSING,
        //    COMPLETED,
        //    ABORTED;

        // public enum AutoCaptureFeedback {
        //     FACE_COMPLIANT,
        //     NO_FACE_DETECTED,
        //     MULTIPLE_FACES_DETECTED,
        //     INVALID_POSE,
        //     FACE_TOO_FAR,
        //     FACE_TOO_CLOSE,
        //     FACE_ON_LEFT,
        //     FACE_ON_RIGHT,
        //     FACE_TOO_HIGH,
        //     FACE_TOO_LOW,
        //     INSUFFICIENT_LIGHTING,
        //     LEFT_EYE_CLOSED,
        //     RIGHT_EYE_CLOSED,
        //     DARK_GLASSES_DETECTED;

        private final String TAG = PollingSessionStateTask.class.getSimpleName();
        CaptureSessionStatus status;
        private byte[] frame;
        AutoCaptureFeedback feedback = null;
        boolean donePolling = false;
        boolean pollingError = false;
        IFaceCapture.ICaptureState state;
        public void main() {

            mStatusExecutor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            mStatusExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    //Background work here
                    while(!donePolling && mFaceCapture != null) {
                        try {

                            state = mFaceCapture.getCaptureSessionState();

                            status = state.getStatus();
                            Log.d(TAG, "Receive Status:........." + status.toString());
                            if (status == CaptureSessionStatus.COMPLETED ||
                                    status == CaptureSessionStatus.STOPPED ||
                                    status == CaptureSessionStatus.TIMED_OUT ||
                                    status == CaptureSessionStatus.ABORTED)  {
                                donePolling = true;
                            } else if (status == CaptureSessionStatus.CAPTURING) {
                                frame = state.getFrame();
                                feedback = state.getFeedback();
                                Log.d(TAG, "Receive Status feedback:........." + feedback);
                            }
                        } catch (Exception e) {
                            pollingError = true;
                            Log.e(TAG, "Receive status error : " + e.getMessage());
                        }

                        if (donePolling) {
                            mPollingSessionStateListener.onPollingStatusListener(status, frame, feedback);
                            Log.d(TAG, "Receive Status feedback sending last:........." + feedback);
                            mStatusExecutor.shutdownNow();
                        }

                        if (!donePolling) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //UI Thread work here
                                    if (!pollingError ) {
                                        Log.d(TAG, "Post status message to listener: " + status);
                                        mPollingSessionStateListener.onPollingStatusListener(status, frame, feedback);
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }
}
