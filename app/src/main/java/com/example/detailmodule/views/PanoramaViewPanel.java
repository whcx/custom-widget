package com.example.detailmodule.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.detailmodule.adapter.PanoramaBall;
import com.example.detailmodule.fragments.BaseFragment;
import com.example.detailmodule.utils.HttpUtils;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class PanoramaViewPanel extends ConstraintLayout implements SensorEventListener, View.OnClickListener, View.OnFocusChangeListener {
    private static final String TAG = PanoramaViewPanel.class.getSimpleName();
    public static final int BITMAP_LOAD_SUCCESS = 1;
    private PanoramaViewPanel mPanoramaViewPanel = null;
    private Context mContext;
    private ConstraintLayout.LayoutParams mPanoramaLayoutParams;
    private int mParentViewId = -1;
    private int mPanoramaViewPanelId = -1;
    private int mGLSurfaceViewId = -1;
    private FrameLayout.LayoutParams mPanoramaViewParams;
    private LayoutParams mGLSurfaceViewParams;
    private GLSurfaceView mGLSurfaceView;
    private HashMap<String ,Bitmap> mBitmapMap = null;
    private BaseFragment.ExitFragmentListener mExitFragmentListener;
    private Handler mHandler;
    private final int TIME_INTERVAL = 200;//200MS
    private int mClickCount = 0;
    private float mPreviousY;
    private float mPreviousYs;
    private float mPreviousX;
    private float mPreviousXs;
    private float mPreDegrees = 0.0F;
    private PanoramaBall mPanoramaBall;
    private SensorManager mSensorManager;
    private Sensor mGyroscopeSensor;
    private static final float NS2S = 1.0E-9F;
    private float mTimestamp;
    private float[] mAngle = new float[3];
    private Handler mHandlerSensor;
    private Handler mHandlerRestore;
    private final int SENSOR_DETECT_MSG = 101;
    private int mRestoreCount;
    private boolean mIsPortrait = true;

    public PanoramaViewPanel(@NonNull Context context) {
        super(context);
        mContext = context;
        mPanoramaViewPanel = this;
        mHandlerSensor = new HandlerSensor();
        mHandlerRestore = new Handler();
        mRestoreCount = 0;
        initSensor();
        mHandler = new InnerHandler(this);
        initBitmapResource();
        initViewId();
        setId(mPanoramaViewPanelId);
    }
    public PanoramaViewPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PanoramaViewPanel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setExitFragmentListener(BaseFragment.ExitFragmentListener exitListener) {
        mExitFragmentListener = exitListener;
    }

    private void initSensor() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private void initBitmapResource() {
/*        AssetManager assetManager = mContext.getResources().getAssets();
        try (InputStream inputStream = assetManager.open("gritworld/drawable/detail_audio.png")){
            mAudioBitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void setParentId(int id) {
        mParentViewId = id;
    }

    public void setInitData(String panoramaUrl) {
        mPanoramaBall = new PanoramaBall(mContext, HttpUtils.getFileFromUri(panoramaUrl));
    }

    public void setInitData(int resId) {
        mPanoramaBall = new PanoramaBall(mContext, resId);
    }

    private void initViewId() {
        mPanoramaViewPanelId = View.generateViewId();
        mGLSurfaceViewId = View.generateViewId();
    }

    private void initGlSurfaceView(boolean isPortrait) {
        mGLSurfaceView = new GLSurfaceView(mContext);
        mGLSurfaceView.setId(mGLSurfaceViewId);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(mPanoramaBall);

        mGLSurfaceViewParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mGLSurfaceViewParams.leftToLeft = mPanoramaViewPanelId;
        mGLSurfaceViewParams.topToTop = mPanoramaViewPanelId;
        mGLSurfaceViewParams.rightToRight = mPanoramaViewPanelId;
        mGLSurfaceViewParams.bottomToBottom = mPanoramaViewPanelId;
    }

    private void initPortraitParams() {
        mPanoramaViewParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mPanoramaViewParams.gravity = Gravity.CENTER;
    }

    private void initLandscapeParams() {
        mPanoramaViewParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mPanoramaViewParams.gravity = Gravity.CENTER;
    }

    public View getPortraitView() {
        mIsPortrait = true;
        removeAllViews();
        initPortraitParams();
        setLayoutParams(mPanoramaViewParams);
        initGlSurfaceView(true);
        addView(mGLSurfaceView, mGLSurfaceViewParams);
        setFocusable(true);
        requestFocus();
        return this;
    }

    public View getLandscapeView() {
        mIsPortrait = false;
        removeAllViews();
        initLandscapeParams();
        setLayoutParams(mPanoramaViewParams);
        initGlSurfaceView(false);
        addView(mGLSurfaceView, mGLSurfaceViewParams);
        setFocusable(true);
        requestFocus();
        return this;
    }

    public void orientationChanged(Configuration newConfig) {
    }

    private void removePanoramaViews() {
        mSensorManager.unregisterListener(this);
        mExitFragmentListener.removeFragment();
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (mTimestamp != 0.0F) {
                float dT = ((float) sensorEvent.timestamp - mTimestamp) * 1.0E-9F;
                float[] tmpAngle = mAngle;
                tmpAngle[0] += sensorEvent.values[0] * dT;
                tmpAngle = mAngle;
                tmpAngle[1] += sensorEvent.values[1] * dT;
                tmpAngle = mAngle;
                tmpAngle[2] += sensorEvent.values[2] * dT;
                float anglex = (float) Math.toDegrees((double) mAngle[0]);
                float angley = (float) Math.toDegrees((double) mAngle[1]);
                float anglez = (float) Math.toDegrees((double) mAngle[2]);
                SensorDetect info = new SensorDetect();
                info.setSensorX(angley);
                info.setSensorY(anglex);
                info.setSensorZ(anglez);
                Message msg = new Message();
                msg.what = SENSOR_DETECT_MSG;
                msg.obj = info;
                mHandlerSensor.sendMessage(msg);
            }
            mTimestamp = (float) sensorEvent.timestamp;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public boolean onTouchEvent(MotionEvent event) {
        mSensorManager.unregisterListener(this);
        float y = event.getY();
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = y - mPreviousYs;
                float dx = x - mPreviousXs;
                PanoramaBall tmpBall = mPanoramaBall;
                tmpBall.yAngle += dx * 0.3F;
                tmpBall = mPanoramaBall;
                tmpBall.xAngle += dy * 0.3F;
                if (mPanoramaBall.xAngle < -60.0F) {
                    mPanoramaBall.xAngle = -60.0F;
                } else if (mPanoramaBall.xAngle > 60.0F) {
                    mPanoramaBall.xAngle = 60.0F;
                }
                rotate();
                break;
        }
        mPreviousYs = y;
        mPreviousXs = x;
        return true;
    }

    private void rotate() {
//        RotateAnimation anim = new RotateAnimation(mPreDegrees, -mPanoramaBall.yAngle, 1, 0.5F, 1, 0.5F);
//        anim.setDuration(200L);
//        img.startAnimation(anim);
        mPreDegrees = -mPanoramaBall.yAngle;
    }

    private void restore() {
        mRestoreCount = (int) ((mPanoramaBall.yAngle - 90.0F) / 10.0F);
        mHandlerRestore.post(new Runnable() {
            public void run() {
                if (mRestoreCount != 0) {
                    if (mRestoreCount > 0) {
                        mPanoramaBall.yAngle -= 10.0F;
                        mHandlerRestore.postDelayed(this, 16L);
                        --mRestoreCount;
                    }

                    if (mRestoreCount < 0) {
                        mPanoramaBall.yAngle += 10.0F;
                        mHandlerRestore.postDelayed(this, 16L);
                        ++mRestoreCount;
                    }
                } else {
                    mPanoramaBall.yAngle = 90.0F;
                }
                mPanoramaBall.xAngle = 0.0F;
            }
        });
    }

    private void showExitDetailHint() {
        try {
            String msg = new String("双击详细介绍!".getBytes(), "UTF-8");
            Toast toast = Toast.makeText(mContext,msg,Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b){
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    public void onClick(View v) {
        mClickCount = 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            mClickCount++;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mClickCount == 2) {
                        removePanoramaViews();
                    }
                    mClickCount = 0;
                }
            }, TIME_INTERVAL);
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            removePanoramaViews();
        }
    }

    class InnerHandler extends Handler {
        private WeakReference<PanoramaViewPanel> panoramaViewPanelWeakReference;
        public InnerHandler(PanoramaViewPanel panoramaViewPanel) {
            panoramaViewPanelWeakReference = new WeakReference<>(panoramaViewPanel);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case BITMAP_LOAD_SUCCESS:

                    showExitDetailHint();
                    break;
            }
        }
    }

    class HandlerSensor extends Handler {
        HandlerSensor() { }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SENSOR_DETECT_MSG:
                    SensorDetect info = (SensorDetect) msg.obj;
                    float y = info.getSensorY();
                    float x = info.getSensorX();
                    float dy = y - mPreviousY;
                    float dx = x - mPreviousX;
                    PanoramaBall tmpBall = mPanoramaBall;
                    if (mIsPortrait) {
                        tmpBall.yAngle += dx * 2.0F;
                        tmpBall = mPanoramaBall;
                        tmpBall.xAngle += dy * 0.5F;
                    } else {
                        tmpBall.yAngle += dy * 2.0F;
                        tmpBall = mPanoramaBall;
                        tmpBall.xAngle += dx * 0.5F;

                    }
                    if (mPanoramaBall.xAngle < -60.0F) {
                        mPanoramaBall.xAngle = -60.0F;
                    } else if (mPanoramaBall.xAngle > 60.0F) {
                        mPanoramaBall.xAngle = 60.0F;
                    }
                    mPreviousY = y;
                    mPreviousX = x;
                    rotate();
                default:
            }
        }
    }

    class SensorDetect {
        float sensorX;
        float sensorY;
        float sensorZ;

        SensorDetect() {
        }

        float getSensorX() {
            return sensorX;
        }

        void setSensorX(float sensorX_) {
            sensorX = sensorX_;
        }

        float getSensorY() {
            return sensorY;
        }

        void setSensorY(float sensorY_) {
            sensorY = sensorY_;
        }

        float getSensorZ() {
            return sensorZ;
        }

        void setSensorZ(float sensorZ_) {
            sensorZ = sensorZ_;
        }
    }
}
