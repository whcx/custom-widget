package com.example.detailmodule.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.detailmodule.mplay.IMediaPbService;
import com.example.detailmodule.mplay.MediaPbService;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;

public class MediaUtils {

    public static IMediaPbService sService;
    private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

    public static class ServiceToken {
        ContextWrapper mContextWrapper;
        ServiceToken(ContextWrapper context) {
            mContextWrapper = context;
        }
    }

    private static class ServiceBinder implements ServiceConnection {
        final ServiceConnection mCallback;

        ServiceBinder(ServiceConnection calllback) {
            mCallback = calllback;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sService = IMediaPbService.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(name, service);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(name);
            }
            sService = null;
        }
    }

    public static ServiceToken bindToService(Activity context) {
        return bindToService(context, null);
    }

    public static ServiceToken bindToService(Activity context, ServiceConnection callBack) {
        ContextWrapper cw = new ContextWrapper(context);
        cw.startService(new Intent(cw, MediaPbService.class));
        ServiceBinder serviceBinder = new ServiceBinder(callBack);
        if (cw.bindService((new Intent()).setClass(cw, MediaPbService.class), serviceBinder, 0)) {
            sConnectionMap.put(cw, serviceBinder);
            return new ServiceToken(cw);
        }
        return null;
    }

    public static void unbindFromService(ServiceToken token) {
        if (token == null)
            return;
        ContextWrapper contextWrapper = token.mContextWrapper;
        ServiceBinder serviceBinder = sConnectionMap.remove(contextWrapper);
        if (serviceBinder == null)
            return;
        contextWrapper.unbindService(serviceBinder);
        if (sConnectionMap.isEmpty())
            sService = null;
    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        if (appProcessInfos == null) {
            return false;
        }

        final String pkgName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessInfos) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName.equalsIgnoreCase(pkgName)) {
                return true;
            }
        }
        return false;
    }
}
