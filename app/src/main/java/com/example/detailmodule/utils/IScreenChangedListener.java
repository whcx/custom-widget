package com.example.detailmodule.utils;

public interface IScreenChangedListener {
    void setScreenChangedListener(IScreenChanged screenChanged);
    interface IScreenChanged{
        void setCurrentPortrait(boolean portrait);
    }
}
