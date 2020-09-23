package com.example.detailmodule.mplay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.lang.ref.SoftReference;

import static com.example.detailmodule.mplay.IMediaPbService.*;

public class MediaPbService extends Service {
    private MultiPlayer mPlayer;
    private AudioManager mAudioManager;
    private boolean mServiceInUse = false;
    private int mServiceStartId = -1;
    private String mFileToPlay;
    private boolean mPausedByTransientLossOfFocus = false;
    private boolean mIsSupposedToBePlaying = false;
    private static final int FOCUSCHANGE  = 1;

    public MediaPbService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mPlayer = new MultiPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStartId = startId;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mPlayer.release();
        mPlayer = null;
        mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        super.onDestroy();
        mServiceInUse = false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mServiceInUse = false;
        stopSelf(mServiceStartId);
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        mServiceInUse = true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mServiceInUse = true;
        return mBinder;
    }

    private Handler mMediaPbHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case FOCUSCHANGE: {
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                            try {
                                if (isPlaying()) {
                                    mPausedByTransientLossOfFocus = false;
                                }
                                pause();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            break;

                        case AudioManager.AUDIOFOCUS_GAIN:
                            try {
                                if (!isPlaying() && mPausedByTransientLossOfFocus) {
                                    mPausedByTransientLossOfFocus = false;
                                    play();
                                }

                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            }
        }
    };

    private AudioFocusRequest mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(new FocusChangeListener())
            .build();

    private static class FocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        private final Object mLock = new Object();

        @Override
        public void onAudioFocusChange(int focusChange) {

        }
    }
    private class MultiPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener,
            MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
        private final HandlerThread mPrepareHandlerThread;
        private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();
        private boolean mIsInitialized = false;
        private boolean mIsCompleted = false;

        public MultiPlayer() {
            mPrepareHandlerThread = new HandlerThread("prepare");
            mPrepareHandlerThread.start();
        }

        public void setDataSource(String path) {
            try {
                if (isPlaying()) {
                    mCurrentMediaPlayer.stop();
                    mCurrentMediaPlayer.release();
                    mCurrentMediaPlayer.reset();
                }
                if (path.startsWith("content://")) {
                    mCurrentMediaPlayer.setDataSource(MediaPbService.this, Uri.parse(path));
                } else {
                    mCurrentMediaPlayer.setDataSource(path);
                }
//                mCurrentMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mCurrentMediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
                mCurrentMediaPlayer.prepare();
            } catch (IOException e) {
                mIsInitialized = false;
                e.printStackTrace();
                return;
            } catch (IllegalArgumentException | IllegalStateException e) {
                mIsInitialized = false;
                e.printStackTrace();
                return;
            } catch (RuntimeException re) {
                mIsInitialized = false;
                re.printStackTrace();
                return;
            }
            mCurrentMediaPlayer.setOnPreparedListener(this::onPrepared);
            mCurrentMediaPlayer.setOnCompletionListener(this::onCompletion);
            mCurrentMediaPlayer.setOnErrorListener(this::onError);
            mIsInitialized = true;
        }

        public void start() {
            mCurrentMediaPlayer.start();
            mIsCompleted = false;
        }

        public void stop() {
            mCurrentMediaPlayer.reset();
            mIsInitialized = false;
        }

        public void pause() {
            mCurrentMediaPlayer.pause();
        }
        public void release() {
            stop();
            mCurrentMediaPlayer.release();
        }

        public boolean isInitialized() {
            return mIsInitialized;
        }

        public boolean isCompleted() {
            return mIsCompleted;
        }
        @Override
        public void onCompletion(MediaPlayer mp) {
            mIsCompleted = true;
            release();
        }

        @Override
        public void onPrepared(MediaPlayer mp) {

        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            release();
            return false;
        }
    }
    // playback state machine.
    public void openFile(String path) throws RemoteException {
        synchronized (this) {
            if (null == path) {
                return;
            }

            mFileToPlay = path;
            mPlayer.setDataSource(mFileToPlay);
            if (mPlayer.isInitialized()) {
                return;
            }
            stop();
        }
    }

    public void play() throws RemoteException {
        if (null == mAudioManager) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        mAudioManager.requestAudioFocus(mAudioFocusRequest);
        if (mPlayer.isInitialized()) {
            mPlayer.start();
            if (!mIsSupposedToBePlaying) {
                mIsSupposedToBePlaying = true;
            }
        }
    }

    public void stop() throws RemoteException {
        mIsSupposedToBePlaying = false;
        if (mPlayer != null && mPlayer.isInitialized()) {
            mPlayer.stop();
        }
        mFileToPlay = null;
    }

    public void pause() throws RemoteException {
        synchronized (this) {
            if (isPlaying()) {
                mPlayer.pause();
                mIsSupposedToBePlaying = false;
            }
        }
    }

    public boolean isPlaying() throws RemoteException {
        return mIsSupposedToBePlaying;
    }

    public boolean isComplete() throws RemoteException {
        return false;
    }

    private final IBinder mBinder = new ServiceStub(this);
    static class ServiceStub extends IMediaPbService.Stub{
        SoftReference<MediaPbService> mService;

        ServiceStub(MediaPbService mediaPbService) {
            mService = new SoftReference<MediaPbService>(mediaPbService);
        }
        @Override
        public void openFile(String path) throws RemoteException {
            mService.get().openFile(path);
        }

        @Override
        public void play() throws RemoteException {
            mService.get().play();
        }

        @Override
        public void stop() throws RemoteException {
            mService.get().stop();
        }

        @Override
        public void pause() throws RemoteException {
            mService.get().pause();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.get().isPlaying();
        }

        @Override
        public boolean isComplete() throws RemoteException {
            return mService.get().isComplete();
        }
    }
}
