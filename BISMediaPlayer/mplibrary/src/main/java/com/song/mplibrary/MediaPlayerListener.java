package com.song.mplibrary;

/**
 * Created by Nathen on 16/7/26.
 */
public interface MediaPlayerListener {
    void onPrepared();

    void onCompletion();

    void onAutoCompletion();

    void onBufferingUpdate(int percent);

    void onSeekComplete();

    void onError(int what, int extra);

    void onInfo(int what, int extra);

    void onVideoSizeChanged();

    void goBackOnThisFloor();

    boolean downStairs();

    void onScrollChange();

    int getScreenType();

    String getUrl();

    int getState();

    void autoFullscreen(float x);

    void autoQuitFullscreen();

}
