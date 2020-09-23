package com.example.detailmodule.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.detailmodule.fragments.BaseFragment;
import com.example.detailmodule.utils.HttpUtils;
import com.example.detailmodule.utils.ParamsUtil;

public class LuckyView extends ConstraintLayout implements View.OnClickListener  {
    private static final float TEXT_SIZE = 20;
    private Context mContext;
    private ImageView mImageView;
    private TextView mTextView;
    private int mBaseViewWidth = 0;
    private int mBaseViewHeight = 0;
    private int mBaseViewId = -1;
    private int mImageViewId = -1;
    private int mTextViewId = -1;
    private boolean mIsPortrait;
    private String mTextStr=null;
    private String mImgUrl=null;
    private String mImgFilePath =null;
    private static final float CORNER_RADIUS = 56;
    private Paint roundPaint;
    private Paint imagePaint;
    private Path mRoundPath;
    private RectF mRoundRectF;
    private BaseFragment.ExitFragmentListener mExitFragListener;

    public LuckyView(@NonNull Context context) {
        super(context);
        mContext = context;
        setBackgroundColor(Color.TRANSPARENT);
        initRadiusParams();
        initViewId();
        setLuckyViewParams(ParamsUtil.screenPortrait((Activity) context));
        initViews();
        setOnClickListener(this::onClick);
    }

    public LuckyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LuckyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTextStr(String textStr) {
        mTextStr = textStr;
        if (mTextView != null) {
            mTextView.setText(mTextStr);
        }
    }

    public void setImgUrl(String imgUrl) {
        mImgUrl = imgUrl;
        if (mImageView != null) {
            if (null != (mImgFilePath = HttpUtils.getFileFromUri(mImgUrl))) {
                mImageView.setImageDrawable(Drawable.createFromPath(mImgFilePath));
            }
        }
    }

    public void setImgFilePath(String filePath) {
        mImgFilePath = filePath;
        if (null != mImageView && null != mImgFilePath) {
            mImageView.setImageDrawable(Drawable.createFromPath(mImgFilePath));
            invalidate();
        }
    }

    private void initViewId() {
        mBaseViewId = View.generateViewId();
        mImageViewId = View.generateViewId();
        mTextViewId = View.generateViewId();
        setId(mBaseViewId);
    }

    private void setLuckyViewParams(boolean isPortrait) {
        mIsPortrait = isPortrait;
        int width = ParamsUtil.getScreenWidth(mContext);
        int height = ParamsUtil.getScreenHeight(mContext);
        if (isPortrait) {
            setLayoutParams(initPortraitParams(width,height));
        } else {
            setLayoutParams(initLandscapeParams(width,height));
        }
    }

    private ViewGroup.LayoutParams initPortraitParams(int width, int height) {
        FrameLayout.LayoutParams luckyViewParams = new FrameLayout.LayoutParams((int)(width * 0.9f) ,(int)(height * 0.3f));
        luckyViewParams.gravity= Gravity.CENTER;
        mBaseViewWidth = luckyViewParams.width;
        mBaseViewHeight = luckyViewParams.height;
        return luckyViewParams;
    }

    private ViewGroup.LayoutParams initLandscapeParams(int width, int height) {
        FrameLayout.LayoutParams luckyViewParams = new FrameLayout.LayoutParams((int)(width * 0.5f) ,(int)(height * 0.9f));
        luckyViewParams.gravity= Gravity.CENTER;
        mBaseViewWidth = luckyViewParams.width;
        mBaseViewHeight = luckyViewParams.height;
        return luckyViewParams;
    }

    private void initViews() {
        initImageView();
        initTextView();
    }

    public View getPortraitView() {
        mIsPortrait = true;
        removeAllViews();
        setLuckyViewParams(true);
        initImageView();
        initTextView();
        return this;
    }

    public View getLandscapeView() {
        mIsPortrait = false;
        removeAllViews();
        setLuckyViewParams(false);
        initImageView();
        initTextView();
        return this;
    }

    private void initImageView() {
        ConstraintLayout.LayoutParams imgLayoutParams;
        if (null == mImageView) {
            mImageView = new ImageView(mContext);
            mImageView.setId(mImageViewId);
            mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        if (null != mImgFilePath) {
            mImageView.setImageDrawable(Drawable.createFromPath(mImgFilePath));
        }
        imgLayoutParams = new ConstraintLayout.LayoutParams(0,0);
        imgLayoutParams.leftToLeft = mBaseViewId;
        imgLayoutParams.topToTop = mBaseViewId;
        imgLayoutParams.rightToRight = mBaseViewId;
        imgLayoutParams.bottomToBottom = mBaseViewId;
        removeView(mImageView);
        addView(mImageView, imgLayoutParams);
    }

    private void initTextView() {
        if (null == mTextView) {
            mTextView = new TextView(mContext);
            mTextView.setText(mTextStr);
            mTextView.setTextSize(TEXT_SIZE);
            mTextView.setSingleLine();
            mTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            mTextView.setMarqueeRepeatLimit(-1);
            mTextView.setSelected(true);
            mTextView.setId(mTextViewId);
        }
        ConstraintLayout.LayoutParams textLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        textLayoutParams.leftToLeft = mImageViewId;
        textLayoutParams.topToTop = mImageViewId;
        textLayoutParams.rightToRight = mImageViewId;
        textLayoutParams.bottomToBottom = mImageViewId;
        if (mIsPortrait) {
            textLayoutParams.verticalBias = 0.575f;
            mTextView.setPadding((int)(mBaseViewWidth/4.5f),0,mBaseViewWidth/6,0);
        } else {
            textLayoutParams.verticalBias = 0.55f;
            mTextView.setPadding((int)(mBaseViewWidth/4.5f),0,mBaseViewWidth/6,0);
        }
        removeView(mTextView);
        addView(mTextView, textLayoutParams);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mIsPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        setLuckyViewParams(mIsPortrait);
        initViews();
    }

    private void initRadiusParams() {
//        roundPaint = new Paint();
//        roundPaint.setColor(Color.WHITE);
//        roundPaint.setAntiAlias(true);
//        roundPaint.setStyle(Paint.Style.FILL);
//        roundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

//        imagePaint = new Paint();
//        imagePaint.setXfermode(null);
        setWillNotDraw(false);
        mRoundPath = new Path();
        mRoundRectF = new RectF();

    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b){
        super.onLayout(changed, l, t, r, b);
        mRoundPath.reset();
        mRoundRectF.set(0f, 0f, getMeasuredWidth(), getMeasuredHeight());
        mRoundPath.addRoundRect(mRoundRectF, CORNER_RADIUS, CORNER_RADIUS, Path.Direction.CW);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.clipPath(mRoundPath);
        super.draw(canvas);
    }

    public void setExitFragmentListener(BaseFragment.ExitFragmentListener exitListener) {
        mExitFragListener = exitListener;
    }

    @Override
    public void onClick(View v) {
        mExitFragListener.removeFragment();
    }
}
