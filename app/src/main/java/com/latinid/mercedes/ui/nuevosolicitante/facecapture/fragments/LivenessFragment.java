package com.latinid.mercedes.ui.nuevosolicitante.facecapture.fragments;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aware.facecapture.AutoCaptureFeedback;
import com.aware.facecapture.CaptureSessionStatus;
import com.aware.facecapture.Rectangle;
import com.latinid.mercedes.Main3Activity;
import com.latinid.mercedes.R;
import com.latinid.mercedes.ui.nuevosolicitante.facecapture.api.FaceCaptureApi;
import com.latinid.mercedes.util.BinnacleCongif;

import org.jetbrains.annotations.NotNull;

public class LivenessFragment extends Fragment implements
        FaceCaptureApi.PollingSessionStateListener {

    private final static String TAG = LivenessFragment.class.getSimpleName();
    private static final int BACKGROUND_ALPHA = 200;

    private FrameLayout mFrameLayout;
    private Main3Activity mUI;

    private TextView mFeedback;
    private ImageView mImageView;

    private FaceCaptureApi mFaceApi;

    private Canvas mCanvas;
    private Paint mOvalPaint;  // draws the oval outline
    private Paint mPorterDuffPaint;
    private RectF mAreadOfInterest;

    private Bitmap mCapturedImage;
    private Bitmap mForegroundRed;
    private Bitmap mForegroundGreen;
    private Bitmap mBackground;
    private String mCurrentFeedback = "";
    private boolean mIsPortrait;
    private byte[] mCurrentFrame;

    public static LivenessFragment newInstance(Main3Activity ui, boolean isPortrait) {
        LivenessFragment instance = new LivenessFragment();
        instance.mUI = ui;
        instance.mIsPortrait = isPortrait;
        return  instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_liveness_viewface, container, false);

        mFrameLayout = (FrameLayout) view.findViewById(R.id.frame_layout);

        try {
            mFaceApi = FaceCaptureApi.getInstance(mUI);
            mFaceApi.setPollingStatusListener(this);
        } catch (Exception e) {
            BinnacleCongif.writeLog(TAG,1, "Error cargando Instancia FaceCaptureApi -> function: onCreateView()", "Error: "+e.getLocalizedMessage(), getContext());
            Log.e(TAG, "1-No Face API: " + e.getMessage());
        }

        mFeedback = (TextView) view.findViewById(R.id.tv_feedback);
        mImageView = (ImageView) view.findViewById(R.id.imageView);

        if (mOvalPaint == null) {
            initializePaint();
        }

        if (mCapturedImage != null) {
            mCapturedImage.recycle();
            mCapturedImage = null;
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFeedback.setText("No Message");

        createBitmaps(mIsPortrait);

        mImageView.setImageBitmap(mBackground);
    }

    public Bitmap getCapturedImage() {
        return mCapturedImage;
    }

    public void initRegionOfInterest(Rectangle rect) {
        initForeground(mForegroundRed, Color.RED, rect);
        initForeground(mForegroundGreen, Color.GREEN, rect);
    }


    public void createBitmaps(boolean is_portrait) {
        if (mForegroundRed != null) {
            mForegroundRed.recycle();
        }
        if (mForegroundGreen != null) {
            mForegroundGreen.recycle();
        }

        if (mBackground != null) {
            mBackground.recycle();
        }

        if (is_portrait) {
            mForegroundRed = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
            mForegroundGreen = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
            mBackground = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
        }
        else {
            mForegroundRed = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
            mForegroundGreen = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
            mBackground = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            if ((mFaceApi != null) && (mFaceApi.isWorkflowValid())) {
                mFaceApi.onStart();
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mFaceApi.onResume();
        } catch (Exception e) {
            Log.e(TAG, "2-No Face API: " + e.getMessage());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (mFaceApi != null && mFaceApi.isWorkflowInProgress()) {
                mFaceApi.onPause();
            }

        } catch (Exception e) {
            Log.e(TAG, "3-No Face API: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mForegroundRed.recycle();
        mForegroundGreen.recycle();
        mBackground.recycle();
        mFaceApi.DestroyWorkflow();


    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onPollingStatusListener(CaptureSessionStatus status, byte[] frame, AutoCaptureFeedback feedback) {

        if (!this.isVisible())
            return;

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                reportFeedback(feedback);
            }
        });

        if ( (status == CaptureSessionStatus.COMPLETED) ) {
            mUI.onCaptureEnd();
            FaceAwareFragment.esconderFragment = 1;
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Log.d(TAG, "Complete");
        }
        else if ( (status == CaptureSessionStatus.TIMED_OUT) ) {
            mUI.onCaptureTimedout();
            FaceAwareFragment.esconderFragment = 2;
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Log.d(TAG, "Timed Out");
        }
        else if (status == CaptureSessionStatus.ABORTED) {
            mUI.onCaptureAbort();
            FaceAwareFragment.esconderFragment = 2;
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Log.d(TAG, "Abort");
        }
        else if (status == CaptureSessionStatus.STOPPED) {
            FaceAwareFragment.esconderFragment = 2;
            mUI.onCaptureStopped();
            Log.d(TAG, "Abort");
        }
        else if (status == CaptureSessionStatus.IDLE) {
            Log.d(TAG, "Idle");
        }
        else if (status == CaptureSessionStatus.STARTING) {
            Log.d(TAG, "Starting");
        }
        else if (status == CaptureSessionStatus.CAPTURING) {
            Log.d(TAG, "Capturing");
        }
        else if (status == CaptureSessionStatus.POST_CAPTURE_PROCESSING) {
            Log.d(TAG, "Post Capture Processing");
        }

        if (frame == null)
            return;
        Bitmap workingBitmap = BitmapFactory.decodeByteArray(frame, 0, frame.length);

        mCurrentFrame = frame;

        Bitmap combined;
        if(mCurrentFeedback != null && mCurrentFeedback.contains("Compliant")) {
            combined = combineTwoBitmaps(workingBitmap, mForegroundGreen);
        }
        else
            combined = combineTwoBitmaps(workingBitmap, mForegroundRed);
        mImageView.setImageBitmap(combined);
        mImageView.invalidate();
    }

    private void UpdateCompliantOval() {
        Bitmap workingBitmap = BitmapFactory.decodeByteArray(mCurrentFrame, 0, mCurrentFrame.length);

        if ( mCapturedImage == null ) {
            mCapturedImage = BitmapFactory.decodeByteArray(mCurrentFrame, 0, mCurrentFrame.length);
        }

        Bitmap combined;
        combined = combineTwoBitmaps(workingBitmap, mForegroundGreen);

        mImageView.setImageBitmap(combined);
        mImageView.invalidate();
    }

    private Bitmap combineTwoBitmaps(Bitmap background, Bitmap foreground) {
        Bitmap combinedBitmap = Bitmap.createBitmap(background.getWidth(), background.getHeight(), background.getConfig());
        Canvas canvas = new Canvas(combinedBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(background, 0, 0, paint);
        canvas.drawBitmap(foreground, 0, 0, paint);
        return combinedBitmap;
    }

    private void initForeground(Bitmap forground, int color, Rectangle rect) {
        int radius = 0;
        Canvas canvas = new Canvas(forground);

        canvas.drawBitmap(forground, 0, 0, null);

        mAreadOfInterest = new RectF();

        if (mIsPortrait) {
            mAreadOfInterest.left = rect.x;
            mAreadOfInterest.top  = rect.y;
            mAreadOfInterest.right = rect.x + rect.width;
            mAreadOfInterest.bottom = rect.y + rect.height;
            radius = rect.width;
        }
        else {
            int centerOffset = (rect.x + (rect.x + rect.width))/2;
            int imageCenter = canvas.getWidth()/2 ;
            if (centerOffset > imageCenter) {
                imageCenter = imageCenter + (centerOffset - imageCenter);
            }
            else {
                imageCenter = imageCenter - (centerOffset - imageCenter);
            }
            int ovalw = (int) (rect.height * ((float)canvas.getHeight()/(float) canvas.getWidth()));
            mAreadOfInterest.top  = rect.y;
            mAreadOfInterest.left = imageCenter - ovalw/2;
            mAreadOfInterest.right = imageCenter + ovalw/2;
            mAreadOfInterest.bottom = rect.y + rect.height;
            radius = ovalw;
        }


        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        RectF aoi = new RectF();
        aoi = mAreadOfInterest;

        int alpha = mOvalPaint.getAlpha();
        mOvalPaint.setColor(Color.GRAY);
        mOvalPaint.setAlpha(BACKGROUND_ALPHA);
        mOvalPaint.setStyle(Paint.Style.FILL);

        canvas.drawRect(0,0, canvas.getWidth(), canvas.getHeight(), mOvalPaint);

        //
        // Clear the inner part of the racetrack
        //
        if (mPorterDuffPaint == null)
            mPorterDuffPaint = new Paint();
        mPorterDuffPaint.setAlpha(160);
        mPorterDuffPaint.setStyle(Paint.Style.FILL);
        mPorterDuffPaint.setColor(Color.TRANSPARENT);
        mPorterDuffPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));


        RectF topOval = new RectF(aoi.left, aoi.top, aoi.right, aoi.top + radius);
        canvas.drawArc(topOval, 0, -180, false, mPorterDuffPaint);

        topOval.top = aoi.top + radius/2;
        topOval.bottom = aoi.bottom - radius/2;
        canvas.drawRect(topOval,mPorterDuffPaint);

        RectF bottomOval = new RectF(aoi.left, aoi.bottom - radius, aoi.right , aoi.bottom);
        canvas.drawArc(bottomOval, 0, 180, false, mPorterDuffPaint);

        //
        // Draw the racetrack outline
        //
        mOvalPaint.setAlpha(alpha);
        mOvalPaint.setColor(color);
        mOvalPaint.setStyle(Paint.Style.STROKE);
        topOval = new RectF(aoi.left, aoi.top, aoi.right, aoi.top + radius);

        canvas.drawArc(topOval, 0, -180, false, mOvalPaint);

        canvas.drawLine(aoi.left,aoi.top + (radius)/2, aoi.left, aoi.bottom - (radius)/2, mOvalPaint);
        canvas.drawLine(aoi.right,aoi.top + (radius)/2, aoi.right, aoi.bottom - (radius)/2, mOvalPaint);

        bottomOval = new RectF(aoi.left, aoi.bottom - aoi.width(), aoi.right, aoi.bottom);
        canvas.drawArc(bottomOval, 0, 180, false, mOvalPaint);
    }




    private void initializePaint() {
        mOvalPaint = new Paint(Paint.LINEAR_TEXT_FLAG);
        mOvalPaint.setColor(Color.RED);
        mOvalPaint.setStyle(Paint.Style.STROKE);
        mOvalPaint.setStrokeWidth(10.0f);
        mOvalPaint.setAlpha(255);
    }

    // this is called by LivenessFeedbackView
    public void reportFeedback(AutoCaptureFeedback feedback) {
        if (!this.isVisible())
            return;

        if (mFeedback != null && (feedback != null)) {
            mCurrentFeedback = getAutoCaptureString(feedback);
            mFeedback.setText(mCurrentFeedback);

            if (feedback == AutoCaptureFeedback.FACE_COMPLIANT) {
                UpdateCompliantOval();
            }
            Log.d(TAG, getAutoCaptureString(feedback));
        } else {
            mFeedback.setText("");
        }
    }

    private String getAutoCaptureString(AutoCaptureFeedback code) {
        String result = "";
        switch (code) {
            case FACE_COMPLIANT:
                result = (getContext().getResources().getString(R.string.compliant));
                break;
            case NO_FACE_DETECTED:
                result = (getContext().getResources().getString(R.string.no_face));
                break;
            case MULTIPLE_FACES_DETECTED:
                result = (getContext().getResources().getString(R.string.multiple_faces));
                break;
            case INVALID_POSE:
                result = (getContext().getResources().getString(R.string.invalid_pose));
                break;
            case FACE_TOO_FAR:
                result = (getContext().getResources().getString(R.string.face_too_far));
                break;
            case FACE_TOO_CLOSE:
                result = (getContext().getResources().getString(R.string.face_too_close));
                break;
            case FACE_ON_LEFT:
                result = (getContext().getResources().getString(R.string.face_too_left));
                break;
            case FACE_ON_RIGHT:
                result = (getContext().getResources().getString(R.string.face_too_right));
                break;
            case FACE_TOO_HIGH:
                result = (getContext().getResources().getString(R.string.face_too_high));
                break;
            case FACE_TOO_LOW:
                result = (getContext().getResources().getString(R.string.face_too_low));
                break;
            case INSUFFICIENT_LIGHTING:
                result = (getContext().getResources().getString(R.string.lighting_too_dark));
                break;
            case LEFT_EYE_CLOSED:
                result = (getContext().getResources().getString(R.string.left_eye_closed));
                break;
            case RIGHT_EYE_CLOSED:
                result = (getContext().getResources().getString(R.string.right_eye_closed));
                break;
            default:
                Log.d(TAG, "Un-monitored Feedback: " + code);
                break;
        }
        return result;
    }
}