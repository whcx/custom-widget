package com.example.detailmodule.fragments;

import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.detailmodule.utils.ParamsUtil;
import com.example.detailmodule.views.DetailViewPanel;
import com.example.detailmodule.views.ScanLineView;

public class ScanLineFragment extends BaseFragment implements BaseFragment.ExitFragmentListener{
    private String mImgFileName;
    private ScanLineView mScanLineView;
    private int mBitmapWidth = 360;

    public ScanLineFragment(String imgFileName) {
        super();
        mImgFileName = imgFileName;
        android.util.Log.d("","mImgFileName="+mImgFileName);
    }

    private String checkFileName(String fileName) {
        if (fileName == null || fileName.equalsIgnoreCase("")) {
            return null;
        }
        if (fileName.startsWith("/")) {
            return fileName.replaceFirst("/","");
        }
        return fileName;
    }

    @Override
    public View initPortraitView(ViewGroup view) {
        if (null == mScanLineView) {
            mScanLineView = new ScanLineView(getContext());
        }
        mScanLineView = mScanLineView.getPortraitView();
        mScanLineView.setScanLineWidth(mBitmapWidth);
        mScanLineView.setExitFragmentListener(this);
        mScanLineView.setImgFilePath(mImgFileName);
        view.removeView(mScanLineView);
        view.addView(mScanLineView);
        return mScanLineView;
    }

    @Override
    public View initLandscapeView(ViewGroup view) {
        if (null == mScanLineView) {
            mScanLineView = new ScanLineView(getContext());
        }
        mScanLineView = mScanLineView.getLandscapeView();
        mScanLineView.setScanLineWidth(mBitmapWidth);
        mScanLineView.setExitFragmentListener(this);
        mScanLineView.setImgFilePath(mImgFileName);
        view.removeView(mScanLineView);
        view.addView(mScanLineView);
        return mScanLineView;
    }

    @Override
    protected void preLoadingData() {
        if (mImgFileName != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mImgFileName, options);
            mBitmapWidth = (options.outWidth != -1) ? options.outWidth : mBitmapWidth;
        }
    }

    @Override
    public void dispatchOrientation(Configuration newConfig) {
        if (null == mScanLineView) {
            mScanLineView = new ScanLineView(getContext());
        }

        mScanLineView.setExitFragmentListener(this);
        mScanLineView.setImgFilePath(mImgFileName);
        mScanLineView.orientationChanged(newConfig);
        ViewGroup contentView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            initPortraitView(contentView);
        } else {
            initLandscapeView(contentView);
        }
    }

    @Override
    public void removeFragment() {
        if (null != mScanLineView) {
            mScanLineView.clearAnimation();
        }
        ViewGroup contentView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.removeView(mScanLineView);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ScanLineFragment scanLineFragment = (ScanLineFragment)fragmentManager.findFragmentByTag(BaseFragment.SCAN_LINE_TAG);
        if (null != scanLineFragment) {
            fragmentTransaction.hide(scanLineFragment);
            fragmentTransaction.remove(scanLineFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void removeViews() {
        if (null != mScanLineView) {
            mScanLineView.clearAnimation();
        }
        ViewGroup contentView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.removeView(mScanLineView);
    }
}
