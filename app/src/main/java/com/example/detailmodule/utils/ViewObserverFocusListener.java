package com.example.detailmodule.utils;

import android.view.ViewTreeObserver;

public interface ViewObserverFocusListener extends ViewTreeObserver.OnWindowFocusChangeListener {
    void onViewWindowFocusChanged(boolean hasFocus);

}
