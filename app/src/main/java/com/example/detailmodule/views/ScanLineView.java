package com.example.detailmodule.views;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.detailmodule.fragments.BaseFragment;
import com.example.detailmodule.utils.ParamsUtil;

public class ScanLineView extends ConstraintLayout {

    private Handler mHandler;
    private boolean mIsPortrait;
    private Context mContext;
    private int mBaseViewWidth;
    private int mBaseViewHeight;
    private ImageView mImageView;
    private int mImageViewId;
    private int mBaseViewId;
    private String mImgFilePath;
    private BaseFragment.ExitFragmentListener mExitFragListener;
    private int mScreenWidth;
    private int mScreenHeight;
    private final float SCAN_LINE_SCALE_P = 0.98f;
    private final float SCAN_LINE_SCALE_L = 0.88f;
    private int mNavigationBarH;
    private int SCAN_LINE_WIDTH = 360;
    private ObjectAnimator mTranslationXAnimator = null;

    public ScanLineView(@NonNull Context context) {
        super(context);
        mContext = context;
        initViewId();
        initNavigationBarHeight();
        mHandler = new Handler();
    }

    public ScanLineView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScanLineView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScanLineWidth(int width) {
        SCAN_LINE_WIDTH = width;
    }
    public void setImgFilePath(String filePath) {
        mImgFilePath = filePath;
        if (null != mImageView && null != mImgFilePath) {
            mImageView.setImageDrawable(Drawable.createFromPath(mImgFilePath));
        }
    }

    private void initViewId() {
        mBaseViewId = View.generateViewId();
        mImageViewId = View.generateViewId();
        setId(mBaseViewId);
    }

    private void initViews() {
        ConstraintLayout.LayoutParams imgLayoutParams;
        if (null == mImageView) {
            mImageView = new ImageView(mContext);
            mImageView.setId(mImageViewId);
            mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        if (null != mImgFilePath) {
            mImageView.setImageDrawable(Drawable.createFromPath(mImgFilePath));
        }
        imgLayoutParams = new ConstraintLayout.LayoutParams(mScreenWidth, mScreenHeight);
        imgLayoutParams.leftToLeft = mBaseViewId;
        imgLayoutParams.topToTop = mBaseViewId;
        imgLayoutParams.rightToRight = mBaseViewId;
        imgLayoutParams.bottomToBottom = mBaseViewId;
        removeView(mImageView);
        addView(mImageView, imgLayoutParams);

        setupTranslateXAnimation();
    }

    public ScanLineView getPortraitView() {
        removeAllViews();
        setScanLineViewParams(true);
        initViews();
        return this;
    }

    public ScanLineView getLandscapeView() {
        removeAllViews();
        setScanLineViewParams(false);
        initViews();
        return this;
    }

    private void setScanLineViewParams(boolean isPortrait) {
        mIsPortrait = isPortrait;
        mScreenWidth = ParamsUtil.getScreenWidth(mContext);
        mScreenHeight = ParamsUtil.getScreenHeight(mContext);
        if (isPortrait) {
            setLayoutParams(initPortraitParams(mScreenWidth, mScreenHeight));
        } else {
            setLayoutParams(initLandscapeParams(mScreenWidth, mScreenHeight));
        }
    }

    private ViewGroup.LayoutParams initPortraitParams(int width, int height) {
        FrameLayout.LayoutParams scanLineViewParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, (int) (height - mNavigationBarH));
        scanLineViewParams.gravity = Gravity.LEFT | Gravity.TOP;
        mBaseViewWidth = scanLineViewParams.width;
        mBaseViewHeight = scanLineViewParams.height;
        return scanLineViewParams;
    }

    private ViewGroup.LayoutParams initLandscapeParams(int width, int height) {
        FrameLayout.LayoutParams scanLineViewParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        scanLineViewParams.gravity = Gravity.LEFT | Gravity.TOP;
        mBaseViewWidth = scanLineViewParams.width;
        mBaseViewHeight = scanLineViewParams.height;
        return scanLineViewParams;
    }

    public void setExitFragmentListener(BaseFragment.ExitFragmentListener exitListener) {
        mExitFragListener = exitListener;
    }

    private void registerFocusListener() {
        ViewTreeObserver observer = mImageView.getViewTreeObserver();
        observer.addOnWindowFocusChangeListener(focusChangeListener);

        mImageView.setFocusable(true);
        mImageView.setFocusableInTouchMode(true);
        mImageView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                }
            }
        });
    }

    private void initNavigationBarHeight() {
        mNavigationBarH = 0;
        int resId = mContext.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resId > 0) {
            mNavigationBarH = mContext.getResources().getDimensionPixelSize(resId);
        }
    }

 /*   private void setupTranslateAnimation() {
        Animation horizontalAnim = new TranslateAnimation(
                -SCAN_LINE_WIDTH, mScreenWidth + SCAN_LINE_WIDTH, 0, 0);
        horizontalAnim.setDuration((int)(mScreenWidth * ParamsUtil.SCAN_SPEED));
        horizontalAnim.setRepeatCount(Animation.INFINITE);
        mImageView.setAnimation(horizontalAnim);
    }

    private void setupTranslateAnimationValue() {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 2.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f
        );
        translateAnimation.setDuration((int)(mScreenWidth * ParamsUtil.SCAN_SPEED));
        translateAnimation.setRepeatCount(Animation.INFINITE);
        mImageView.startAnimation(translateAnimation);
    }*/

    private void setupTranslateXAnimation() {
        ObjectAnimator translationXAnimator = ObjectAnimator.ofFloat(mImageView, "translationX",
                -(mScreenWidth /2 + SCAN_LINE_WIDTH)* 1.0f, mScreenWidth*1.0f);
        translationXAnimator.setDuration((long) (mScreenWidth * ParamsUtil.SCAN_SPEED));
        translationXAnimator.setRepeatCount(Animation.INFINITE);
        translationXAnimator.start();
    }

    public void clearAnimation() {
        if ((mImageView != null) && (mImageView.getAnimation() != null)) {
            mImageView.clearAnimation();
        }

        if (mTranslationXAnimator != null) {
            mTranslationXAnimator.cancel();
        }
        removeAllViews();
    }
    private ViewTreeObserver.OnWindowFocusChangeListener focusChangeListener =
            new ViewTreeObserver.OnWindowFocusChangeListener() {
                @Override
                public void onWindowFocusChanged(boolean hasFocus) {
//                    if (hasFocus) {
//                        getHandler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                mImageView.startAnimation(mImageView.getAnimation());
//                            }
//                        }, 100);
//                    } else {
//                        mImageView.clearAnimation();
//                    }
                }
            };

    public void orientationChanged(Configuration newConfig) {
        mScreenWidth = ParamsUtil.getScreenWidth(mContext);
        mScreenHeight = ParamsUtil.getScreenHeight(mContext);

        if (mTranslationXAnimator != null) {
            mTranslationXAnimator.cancel();
        }
        setupTranslateXAnimation();
    }
}
