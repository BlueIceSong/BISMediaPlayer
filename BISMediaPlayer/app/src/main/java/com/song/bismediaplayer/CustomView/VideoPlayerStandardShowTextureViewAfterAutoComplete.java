package com.song.bismediaplayer.CustomView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.song.mplibrary.VideoPlayerStandard;

/**
 * Created by Nathen on 2016/11/6.
 */

public class VideoPlayerStandardShowTextureViewAfterAutoComplete extends VideoPlayerStandard {
    public VideoPlayerStandardShowTextureViewAfterAutoComplete(Context context) {
        super(context);
    }

    public VideoPlayerStandardShowTextureViewAfterAutoComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setUiWitStateAndScreen(int state) {
        super.setUiWitStateAndScreen(state);
        if (state == CURRENT_STATE_AUTO_COMPLETE) {
            thumbImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClickUiToggle() {
        super.onClickUiToggle();
        if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            thumbImageView.setVisibility(View.GONE);
        }
    }
}
