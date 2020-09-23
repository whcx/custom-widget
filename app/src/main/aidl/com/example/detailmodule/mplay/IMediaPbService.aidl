// IMediaPbService.aidl
package com.example.detailmodule.mplay;

// Declare any non-default types here with import statements

interface IMediaPbService {
    void openFile(String path);
    void play();
    void stop();
    void pause();
    boolean isPlaying();
    boolean isComplete();
}
