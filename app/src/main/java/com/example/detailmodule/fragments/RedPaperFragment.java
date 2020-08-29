package com.example.detailmodule.fragments;

import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.detailmodule.utils.HttpUtils;
import com.example.detailmodule.views.LuckyView;

public class RedPaperFragment extends BaseFragment implements HttpUtils.LoadSuccess, BaseFragment.ExitFragmentListener{
    private final String IMG_FILE_DIRS = "/sdcard/Assets/drawable/";
    private String mImgUrl;
    private String mImgFileName;
    private final String mTextStr;
    private LuckyView mLuckyView;

    public RedPaperFragment(String imgUrl, String imgFileName, String textStr) {
        super();
        mImgUrl = (imgUrl == null || imgUrl.equalsIgnoreCase("")) ? null : imgUrl;
        mImgFileName = checkFileName(imgFileName);
        mTextStr = textStr;
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
        if (null == mLuckyView) {
            mLuckyView = new LuckyView(getContext());
        }
        mLuckyView.setExitFragmentListener(this);
        if (null != mImgFileName) {
            mLuckyView.setImgFilePath(IMG_FILE_DIRS+mImgFileName);
        } else {
            mLuckyView.setImgUrl(mImgUrl);
        }
        mLuckyView.setTextStr(mTextStr);
        view.removeView(mLuckyView);
        view.addView(mLuckyView);
        return mLuckyView;
    }

    @Override
    public View initLandscapeView(ViewGroup view) {
        if (null == mLuckyView) {
            mLuckyView = new LuckyView(getContext());
        }
        mLuckyView.setExitFragmentListener(this);
        if (null != mImgFileName) {
            mLuckyView.setImgFilePath(IMG_FILE_DIRS+mImgFileName);
        } else {
            mLuckyView.setImgUrl(mImgUrl);
        }
        mLuckyView.setTextStr(mTextStr);
        view.removeView(mLuckyView);
        view.addView(mLuckyView);
        return mLuckyView;
    }

    @Override
    protected void preLoadingData() {
        if ((mImgFileName == null)
                && (mImgUrl != null && mImgUrl.startsWith("http"))
                && (null == HttpUtils.getFileFromUri(mImgUrl))) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpUtils.downLoadFileFromUri(getContext(), mImgUrl, null, RedPaperFragment.this);
                }
            }).start();
        }
    }

    @Override
    public void onLoadSuccess(String result) {
        if (null != mLuckyView) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLuckyView.setImgFilePath(result);
                }
            });
        }
    }

    @Override
    public void dispatchOrientation(Configuration newConfig) {

    }

    @Override
    public void removeFragment() {
        ViewGroup contentView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.removeView(mLuckyView);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RedPaperFragment luckyFragment = (RedPaperFragment)fragmentManager.findFragmentByTag(BaseFragment.LUCKY_TAG);
        if (null != luckyFragment) {
            fragmentTransaction.hide(luckyFragment);
            fragmentTransaction.remove(luckyFragment);
            fragmentTransaction.commit();
        }
    }
}
