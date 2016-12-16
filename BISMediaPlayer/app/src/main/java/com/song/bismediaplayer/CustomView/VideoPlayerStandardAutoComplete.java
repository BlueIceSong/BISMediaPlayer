package com.song.bismediaplayer.CustomView;

import android.content.Context;
import android.util.AttributeSet;

import com.song.mplibrary.VideoPlayerStandard;

/**
 * Created by Nathen on 2016/11/26.
 */

public class VideoPlayerStandardAutoComplete extends VideoPlayerStandard {
    public VideoPlayerStandardAutoComplete(Context context) {
        super(context);
    }

    public VideoPlayerStandardAutoComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onAutoCompletion() {
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            setUiWitStateAndScreen(CURRENT_STATE_AUTO_COMPLETE);
        } else {
            super.onAutoCompletion();
        }

    }
}
