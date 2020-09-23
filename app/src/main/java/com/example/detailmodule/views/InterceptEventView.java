package com.example.detailmodule.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.detailmodule.fragments.DetailFragment;

public class InterceptEventView extends FrameLayout {
    DetailFragment detailFragment;
    public InterceptEventView(@NonNull Context context, DetailFragment detailFragment_) {
        super(context);
        detailFragment = detailFragment_;
    }

    public InterceptEventView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptEventView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d("", "onFocusChange(),hasFocus="+ev);
//        ViewGroup view = (ViewGroup)getParent();
//        detailFragment.removeFragment();
//        view.removeView(this);
//        if (ev.getAction() == MotionEvent.ACTION_UP) {
//            detailFragment.removeFragment();
//        }
//        detailFragment.removeFragment();
//        getHandler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                detailFragment.removeFragment();
//            }
//        }, 1000);
        return super.dispatchTouchEvent(ev);
    }
}
