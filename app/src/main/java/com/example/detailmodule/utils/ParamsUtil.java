package com.example.detailmodule.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ParamsUtil {
    private static final String TAG = ParamsUtil.class.getSimpleName();
    private static Context mContext = null;
    private static int mScreenWidth = 0;
    private static int mScreenHeight = 0;
    public static String DETAIL_TITLE="";
    public static String DETAIL_DESCRIPTION="";
    public static String DETAIL_BITMAP_URL="";
    public static String DETAIL_AUDIO_URL="";
    public static String DETAIL_PANORAMA_URL="";
    public static String IMG_FILE_DIRS = "/sdcard/Assets/drawable/";
    public static float SCAN_SPEED= 3.5f;

    public ParamsUtil(Context context) {
        mContext = context;
    }

    public static String getAppFileDirs(Context context) {
        IMG_FILE_DIRS = "/sdcard/Android/data/"+context.getPackageName()+"/files/";
        return IMG_FILE_DIRS;
    }

    public static int getScreenWidth(Context context) {
        if (mScreenWidth != 0)
            return mScreenWidth;
        Resources resources = context.getResources();
        if (resources == null)
            return 0;
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (displayMetrics == null)
            return 0;
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        if (mScreenHeight != 0)
            return mScreenHeight;
        Resources resources = context.getResources();
        if (resources == null)
            return 0;
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (displayMetrics == null)
            return 0;
        return displayMetrics.heightPixels;
    }

    public static boolean screenPortrait(Activity activity) {
        WindowManager windowManager = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getRealSize(point);
        return point.x < point.y;
    }
}
