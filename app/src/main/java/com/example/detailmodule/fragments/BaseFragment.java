package com.example.detailmodule.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.detailmodule.utils.ParamsUtil;


public abstract class BaseFragment extends Fragment {
    public boolean mScreenPortrait = true;
    public static final String PANORAMA_TAG = "panorama_view";
    public static final String DETAIL_TAG = "detail_view";
    public static final String LUCKY_TAG = "lucky_view";

    public BaseFragment() {
    }

    public static interface ExitFragmentListener {
        void removeFragment();
    }

    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preLoadingData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup contentView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        View view = ismScreenPortrait() ? initPortraitView(contentView) : initLandscapeView(contentView);
        return (view == null) ? super.onCreateView(inflater, container, savedInstanceState) :view;
    }

    public abstract View initPortraitView(ViewGroup view);
    public abstract View initLandscapeView(ViewGroup view);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View getView() {
        return super.getView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected abstract void preLoadingData();

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dispatchOrientation(newConfig);
    }

    public abstract void dispatchOrientation(Configuration newConfig);

    public boolean ismScreenPortrait() {
        mScreenPortrait = ParamsUtil.screenPortrait(getActivity());
        return mScreenPortrait;
    }

    public FragmentTransaction getFragmentTransaction() {
        return getActivity().getSupportFragmentManager().beginTransaction();
    }

    public FragmentManager getFragmentManager_() {
        return getActivity().getSupportFragmentManager();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
/*        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();
        fragments.forEach(fragment -> {
            fragmentTransaction.hide(fragment);
            fragmentTransaction.remove(fragment);
        });
        fragmentTransaction.commit();*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
