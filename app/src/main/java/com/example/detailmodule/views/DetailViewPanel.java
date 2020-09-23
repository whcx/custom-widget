package com.example.detailmodule.views;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.detailmodule.R;
import com.example.detailmodule.adapter.DetailRecycleAdapter;
import com.example.detailmodule.fragments.BaseFragment;
import com.example.detailmodule.fragments.DetailFragment;
import com.example.detailmodule.fragments.PanoramaFragment;
import com.example.detailmodule.utils.HttpUtils;
import com.example.detailmodule.utils.ParamsUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.detailmodule.utils.HttpUtils.BITMAP_LOAD_SUCCESS;

public class DetailViewPanel extends ConstraintLayout implements View.OnClickListener, View.OnFocusChangeListener {
    private static final String TAG = DetailViewPanel.class.getSimpleName();
    private DetailViewPanel mDetailViewPanel = null;
    private Context mContext;
    private final int BACKGROUND_COLOR = 0xC8FFFFFF; //ARGB
    private final int BG_ALPHA = 128;
    private int mParentViewId = -1;
    private int mDetailPanelViewId = -1;
    private int mRecyclerViewId = -1;
    private int mContentViewId = -1;
    private int mTitleViewId = -1;
    private int mAudioBtnId = -1;
    private int mPanoramicBtnId = -1;
    private FrameLayout.LayoutParams mDetailViewParams;
    private float mRadius;
    private Paint roundPaint;
    private Paint imagePaint;
    private Path mRoundPath;
    private RectF mRoundRectF;
    private List<Bitmap> mList;
    private String mDetailTitle="";
    private String mDetailDescription="";
    private String mDetailBitmapUrl="";
    private DetailRecycleAdapter mDetailRecycleAdapter;
    private RecyclerView mRecyclerView;
    private LayoutParams mRvParams;
    private TextView mTitleView;
    private TextView mContentView;
    private LayoutParams mTtvParams;
    private LayoutParams mCtvParams;
    private ImageButton mAudioBtn;
    private ImageButton mPanoramicBtn;
    private LayoutParams mAudioBtnParams;
    private LayoutParams mPanoramicBtnParams;
    private final int DETAIL_PANEL_PARAM_W = 96;
    private final int DETAIL_PANEL_PARAM_H = 96;
    private final int BUTTON_W_H = 128;
    private final int VERTICAL_WEIGHT_RV = 5;
    private final int VERTICAL_WEIGHT_TV = 1;
    private final int VERTICAL_WEIGHT_CV = 5;
    private final int TEXTVIEW_PADDING = 36;
    private Bitmap mAudioBitmap;
    private Bitmap mPanoramicBitmap;
    private HashMap<String ,Bitmap> mBitmapMap = null;
    private BaseFragment.ExitFragmentListener mExitFragmentListener;
    private Handler mHandler;
    private final int TIME_INTERVAL = 200;//200MS
    private int mClickCount = 0;
    private DetailFragment mDetailFragment;

    public DetailViewPanel(@NonNull Context context) {
        super(context);
        mContext = context;
        mDetailViewPanel = this;
        mHandler = new InnerHandler(this);
        initBitmapResource();
        initRadiusParams();
        initViewId();
        setId(mDetailPanelViewId);
        setBackgroundColor(BACKGROUND_COLOR);
//        getBackground().setAlpha(BG_ALPHA);
        mList = new ArrayList<Bitmap>();
        mBitmapMap = new HashMap<String ,Bitmap>();
//        setOnFocusChangeListener(this::onFocusChange);
    }

    public void setExitFragmentListener(BaseFragment.ExitFragmentListener exitListener) {
        mExitFragmentListener = exitListener;
    }

    public void setDetailFragment(DetailFragment detailFragment) {
        mDetailFragment = detailFragment;
    }

    private void initBitmapResource() {
        AssetManager assetManager = mContext.getResources().getAssets();
        try (InputStream inputStream = assetManager.open("gritworld/drawable/detail_audio.png")){
            mAudioBitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream inputStream = assetManager.open("gritworld/drawable/detail_panoramic.png")){
            mPanoramicBitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DetailViewPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DetailViewPanel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    class InnerHandler extends Handler {
        private WeakReference<DetailViewPanel> detailViewPanelWeakReference;
        public InnerHandler(DetailViewPanel detailViewPanel) {
            detailViewPanelWeakReference = new WeakReference<>(detailViewPanel);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case BITMAP_LOAD_SUCCESS:
                    if (null != detailViewPanelWeakReference.get().mDetailRecycleAdapter) {
                        detailViewPanelWeakReference.get().mDetailRecycleAdapter.notifyDataSetChanged();
                    }
                    showExitDetailHint();
                    break;
            }
        }
    }

    private void showExitDetailHint() {
        try {
            String msg = new String("双击退出标牌详细介绍!".getBytes(), "UTF-8");
            Toast toast = Toast.makeText(mContext,msg,Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setParentId(int id) {
        mParentViewId = id;
    }

    public void setInitData(String title, String description, String bitmapUrl) {
        mDetailTitle = title;
        mDetailDescription = description;
        mDetailBitmapUrl = bitmapUrl;

        if (mBitmapMap.get(mDetailBitmapUrl) == null) {
            new HttpUtils.DetailBitmapLoadTask(mHandler, mList, mBitmapMap).execute(mDetailBitmapUrl);
        }
    }

    private void initRadiusParams() {
        mRadius = 48.0f;
        roundPaint = new Paint();
        roundPaint.setColor(Color.WHITE);
        roundPaint.setAntiAlias(true);
        roundPaint.setStyle(Paint.Style.FILL);
        roundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        imagePaint = new Paint();
        imagePaint.setXfermode(null);
        setWillNotDraw(false);
        mRoundPath = new Path();
        mRoundRectF = new RectF();

    }
    private void initViewId() {
        mDetailPanelViewId = View.generateViewId();
        mRecyclerViewId = View.generateViewId();
        mContentViewId = View.generateViewId();
        mTitleViewId = View.generateViewId();
        mAudioBtnId = View.generateViewId();
        mPanoramicBtnId = View.generateViewId();
    }

    private void initRecyclerView(boolean isPortrait) {
        mRecyclerView = new RecyclerView(mContext);
        mRecyclerView.setId(mRecyclerViewId);
        mRecyclerView.setFocusable(true);
        mRecyclerView.requestFocus();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mDetailRecycleAdapter = new DetailRecycleAdapter(mList, mContext);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mDetailRecycleAdapter);
        if (isPortrait) {
            mRvParams = new ConstraintLayout.LayoutParams(mDetailViewParams.width,
                    mDetailViewParams.height*VERTICAL_WEIGHT_RV/(VERTICAL_WEIGHT_RV+VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV));
            mRvParams.topToTop = mDetailPanelViewId;
            mRvParams.bottomToTop = mTitleViewId;
            mRvParams.leftToLeft = mDetailPanelViewId;
            mRvParams.rightToRight = mDetailPanelViewId;
            mRvParams.verticalWeight = VERTICAL_WEIGHT_RV;
        } else {
            mRvParams = new ConstraintLayout.LayoutParams(
                    mDetailViewParams.width*VERTICAL_WEIGHT_RV/(VERTICAL_WEIGHT_RV+VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV),
                    mDetailViewParams.height);
            mRvParams.topToTop = mDetailPanelViewId;
            mRvParams.bottomToBottom = mDetailPanelViewId;
            mRvParams.leftToLeft = mDetailPanelViewId;
            mRvParams.rightToLeft = mContentViewId;
            mRvParams.horizontalWeight = VERTICAL_WEIGHT_RV;
        }
    }

    private void initDescriptionView(boolean isPortrait) {
        mTitleView = new TextView(mContext);
        mTitleView.setId(mTitleViewId);
        mTitleView.setTextSize(24.0f);
        mTitleView.setText(mDetailTitle);

        mContentView = new TextView(mContext);
        mContentView.setId(mContentViewId);
        mContentView.setMovementMethod(ScrollingMovementMethod.getInstance());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mContentView.setVerticalScrollbarThumbDrawable(new ColorDrawable(0xFFA8A8A8));
        }
        mContentView.setScrollbarFadingEnabled(false);
        mContentView.setVerticalScrollBarEnabled(true);
        mContentView.setText(mDetailDescription);
        mContentView.setFocusable(true);
        mContentView.requestFocus();

        if (isPortrait) {
            mTtvParams = new LayoutParams(mDetailViewParams.width,
                    mDetailViewParams.height*VERTICAL_WEIGHT_TV/(VERTICAL_WEIGHT_RV+VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV));
            mTtvParams.topToBottom = mRecyclerViewId;
            mTtvParams.bottomToTop = mContentViewId;
            mTtvParams.verticalWeight = VERTICAL_WEIGHT_TV;
            mTitleView.setPadding(TEXTVIEW_PADDING *2,0,0,0);
            mTitleView.setGravity(Gravity.CENTER_VERTICAL);

            mCtvParams = new LayoutParams(mDetailViewParams.width,
                    mDetailViewParams.height*VERTICAL_WEIGHT_CV/(VERTICAL_WEIGHT_RV+VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV));
            mCtvParams.topToBottom = mTitleViewId;
            mCtvParams.bottomToBottom = mDetailPanelViewId;
            mCtvParams.verticalWeight = VERTICAL_WEIGHT_CV;
            mContentView.setPadding(TEXTVIEW_PADDING *2,0,TEXTVIEW_PADDING *2,0);
        } else {
            mTtvParams = new LayoutParams(
                    mDetailViewParams.width*(VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV)/(VERTICAL_WEIGHT_RV+VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV),
                    mDetailViewParams.height*VERTICAL_WEIGHT_TV/(VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV));
            mTtvParams.leftToRight = mRecyclerViewId;
            mTtvParams.rightToRight = mDetailPanelViewId;
            mTtvParams.topToTop = mDetailPanelViewId;
            mTtvParams.bottomToTop = mContentViewId;
            mTtvParams.horizontalWeight = VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV;
            mTitleView.setPadding(TEXTVIEW_PADDING *4,0,0,TEXTVIEW_PADDING);
            mTitleView.setGravity(Gravity.CENTER_VERTICAL);

            mCtvParams = new LayoutParams(
                    mDetailViewParams.width*(VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV)/(VERTICAL_WEIGHT_RV+VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV),
                    mDetailViewParams.height*VERTICAL_WEIGHT_CV/(VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV));
            mCtvParams.topToBottom = mTitleViewId;
            mCtvParams.bottomToBottom = mDetailPanelViewId;
            mCtvParams.leftToRight = mRecyclerViewId;
            mCtvParams.rightToRight = mDetailPanelViewId;
            mCtvParams.horizontalWeight = VERTICAL_WEIGHT_TV+VERTICAL_WEIGHT_CV;
            mContentView.setPadding(TEXTVIEW_PADDING *4,0,0,TEXTVIEW_PADDING*2);
        }
    }

    private void initButtonView(boolean isPortrait) {
        mAudioBtn = new ImageButton(mContext);
        mAudioBtn.setId(mAudioBtnId);
        mAudioBtn.setImageBitmap(mAudioBitmap);
        mAudioBtn.setBackgroundColor(Color.TRANSPARENT);
        mAudioBtn.setFocusable(true);
        mAudioBtn.requestFocus();
        mAudioBtn.setOnClickListener(this);

        mPanoramicBtn = new ImageButton(mContext);
        mPanoramicBtn.setId(mPanoramicBtnId);
        mPanoramicBtn.setImageBitmap(mPanoramicBitmap);
        mPanoramicBtn.setBackgroundColor(Color.TRANSPARENT);
        mPanoramicBtn.setFocusable(true);
        mPanoramicBtn.requestFocus();
        mPanoramicBtn.setOnClickListener(this);

        if (isPortrait) {
            float verticalBias = VERTICAL_WEIGHT_RV * 1.0f / (VERTICAL_WEIGHT_RV + VERTICAL_WEIGHT_TV + VERTICAL_WEIGHT_CV);
            mPanoramicBtnParams = new ConstraintLayout.LayoutParams(BUTTON_W_H, BUTTON_W_H);
            mPanoramicBtnParams.rightToRight = mDetailPanelViewId;
            mPanoramicBtnParams.topToTop = mDetailPanelViewId;
            mPanoramicBtnParams.bottomToBottom = mDetailPanelViewId;
            mPanoramicBtnParams.verticalBias = verticalBias;
            mPanoramicBtnParams.rightMargin = BUTTON_W_H / 2;

            mAudioBtnParams = new ConstraintLayout.LayoutParams(BUTTON_W_H, BUTTON_W_H);
            mAudioBtnParams.rightToRight = mDetailPanelViewId;
            mAudioBtnParams.topToTop = mDetailPanelViewId;
            mAudioBtnParams.bottomToBottom = mDetailPanelViewId;
            mAudioBtnParams.verticalBias = verticalBias;
            mAudioBtnParams.rightMargin = BUTTON_W_H * 2;
        } else {
            float horizontalBias = VERTICAL_WEIGHT_RV * 0.9f / (VERTICAL_WEIGHT_RV + VERTICAL_WEIGHT_CV);
            mPanoramicBtnParams = new ConstraintLayout.LayoutParams(BUTTON_W_H, BUTTON_W_H);
            mPanoramicBtnParams.leftToLeft = mDetailPanelViewId;
            mPanoramicBtnParams.rightToRight = mDetailPanelViewId;
            mPanoramicBtnParams.bottomToBottom = mDetailPanelViewId;
            mPanoramicBtnParams.horizontalBias = horizontalBias;
            mPanoramicBtnParams.bottomMargin = BUTTON_W_H / 2;

            mAudioBtnParams = new ConstraintLayout.LayoutParams(BUTTON_W_H, BUTTON_W_H);
            mAudioBtnParams.leftToLeft = mDetailPanelViewId;
            mAudioBtnParams.rightToRight = mDetailPanelViewId;
            mAudioBtnParams.bottomToBottom = mDetailPanelViewId;
            mAudioBtnParams.horizontalBias = horizontalBias;
            mAudioBtnParams.bottomMargin = BUTTON_W_H * 2;
        }
    }

    private void initPortraitParams() {
        int width = ParamsUtil.getScreenWidth(mContext);
        int height = ParamsUtil.getScreenHeight(mContext);
        mDetailViewParams = new FrameLayout.LayoutParams(width - 2*DETAIL_PANEL_PARAM_W,height - 6*DETAIL_PANEL_PARAM_H);
        mDetailViewParams.gravity = Gravity.CENTER;
    }

    private void initLandscapeParams() {
        int width = ParamsUtil.getScreenWidth(mContext);
        int height = ParamsUtil.getScreenHeight(mContext);
        mDetailViewParams = new FrameLayout.LayoutParams(width - 6*DETAIL_PANEL_PARAM_W,height - 2*DETAIL_PANEL_PARAM_H);
        mDetailViewParams.gravity = Gravity.CENTER_VERTICAL;
        mDetailViewParams.leftMargin = (int)(2.5f * DETAIL_PANEL_PARAM_W);
    }

    public DetailViewPanel getPortraitView() {
        removeAllViews();
        initPortraitParams();
        setLayoutParams(mDetailViewParams);
        initRecyclerView(true);
        initDescriptionView(true);
        initButtonView(true);
        addView(mRecyclerView, mRvParams);
        addView(mContentView, mCtvParams);
        addView(mTitleView, mTtvParams);
        addView(mPanoramicBtn, mPanoramicBtnParams);
        addView(mAudioBtn, mAudioBtnParams);
        setFocusable(true);
        requestFocus();
        return this;
    }

    public DetailViewPanel getLandscapeView() {
        removeAllViews();
        initLandscapeParams();
        setLayoutParams(mDetailViewParams);
        initRecyclerView(false);
        initDescriptionView(false);
        initButtonView(false);
        addView(mRecyclerView, mRvParams);
        addView(mContentView, mCtvParams);
        addView(mTitleView, mTtvParams);
        addView(mPanoramicBtn, mPanoramicBtnParams);
        addView(mAudioBtn, mAudioBtnParams);
        setFocusable(true);
        requestFocus();
        return this;
    }

    public void orientationChanged(Configuration newConfig) {
    }

    private void removeDetailViews() {
        mExitFragmentListener.removeFragment();
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b){
        super.onLayout(changed, l, t, r, b);
        mRoundPath.rewind();
        mRoundRectF.set(0f, 0f, getMeasuredWidth(), getMeasuredHeight());
        mRoundPath.addRoundRect(mRoundRectF, mRadius, mRadius, Path.Direction.CW);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.clipPath(mRoundPath);
        super.draw(canvas);
    }

    @Override
    public void onClick(View v) {

        mClickCount = 0;
        if (v == mAudioBtn) {

        } else if (v == mPanoramicBtn) {
            mDetailFragment.showPanoramaView();
        }
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
                        removeDetailViews();
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
            removeDetailViews();
        }
    }
}
