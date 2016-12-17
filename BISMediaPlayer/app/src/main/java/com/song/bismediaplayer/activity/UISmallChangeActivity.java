package com.song.bismediaplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.song.bismediaplayer.R;
import com.squareup.picasso.Picasso;

import com.song.mplibrary.VideoPlayer;
import com.song.bismediaplayer.customView.VideoPlayerStandardAutoComplete;
import com.song.bismediaplayer.customView.VideoPlayerStandardShowShareButtonAfterFullscreen;
import com.song.bismediaplayer.customView.VideoPlayerStandardShowTextureViewAfterAutoComplete;
import com.song.bismediaplayer.customView.VideoPlayerStandardShowTitleAfterFullscreen;

/**
 * Created by Nathen on 16/7/31.
 */
public class UISmallChangeActivity extends AppCompatActivity {
    VideoPlayerStandardShowShareButtonAfterFullscreen jcVideoPlayerStandardWithShareButton;
    VideoPlayerStandardShowTitleAfterFullscreen videoPlayerStandardShowTitleAfterFullscreen;
    VideoPlayerStandardShowTextureViewAfterAutoComplete videoPlayerStandardShowTextureViewAfterAutoComplete;
    VideoPlayerStandardAutoComplete videoPlayerStandardAutoComplete;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle("SmallChangeUI");
        setContentView(R.layout.activity_ui_small_change);

        jcVideoPlayerStandardWithShareButton = (VideoPlayerStandardShowShareButtonAfterFullscreen) findViewById(R.id.custom_videoplayer_standard_with_share_button);
        jcVideoPlayerStandardWithShareButton.setUp("http://video.jiecao.fm/11/17/c/fxkR4gylyIZKeljem8xTvA__.mp4", VideoPlayer.SCREEN_LAYOUT_NORMAL
                , "example1");
        Picasso.with(this)
                .load("http://img4.jiecaojingxuan.com/2016/11/17/6fc2ae91-36e2-44c5-bb10-29ae5d5c678c.png@!640_360")
                .into(jcVideoPlayerStandardWithShareButton.thumbImageView);


        videoPlayerStandardShowTitleAfterFullscreen = (VideoPlayerStandardShowTitleAfterFullscreen) findViewById(R.id.custom_videoplayer_standard_show_title_after_fullscreen);
        videoPlayerStandardShowTitleAfterFullscreen.setUp("http://video.jiecao.fm/11/18/xu/%E6%91%87%E5%A4%B4.mp4", VideoPlayer.SCREEN_LAYOUT_NORMAL
                , "example2");
        Picasso.with(this)
                .load("http://img4.jiecaojingxuan.com/2016/11/18/f03cee95-9b78-4dd5-986f-d162c06c385c.png@!640_360")
                .into(videoPlayerStandardShowTitleAfterFullscreen.thumbImageView);

        videoPlayerStandardShowTextureViewAfterAutoComplete = (VideoPlayerStandardShowTextureViewAfterAutoComplete) findViewById(R.id.custom_videoplayer_standard_show_textureview_aoto_complete);
        videoPlayerStandardShowTextureViewAfterAutoComplete.setUp("http://video.jiecao.fm/11/18/c/I-KpaMJ-HMDfAy6tX2Jfag__.mp4", VideoPlayer.SCREEN_LAYOUT_NORMAL
                , "example3");
        Picasso.with(this)
                .load("http://img4.jiecaojingxuan.com/2016/11/18/e7ea659f-c3d2-4979-9ea5-f993b05e5930.png@!640_360")
                .into(videoPlayerStandardShowTextureViewAfterAutoComplete.thumbImageView);

        videoPlayerStandardAutoComplete = (VideoPlayerStandardAutoComplete) findViewById(R.id.custom_videoplayer_standard_aoto_complete);
        videoPlayerStandardAutoComplete.setUp("http://video.jiecao.fm/8/17/%E6%8A%AB%E8%90%A8.mp4", VideoPlayer.SCREEN_LAYOUT_NORMAL
                , "example4");
        Picasso.with(this)
                .load("http://img4.jiecaojingxuan.com/2016/8/17/f2dbd12e-b1cb-4daf-aff1-8c6be2f64d1a.jpg")
                .into(videoPlayerStandardAutoComplete.thumbImageView);
    }

    @Override
    public void onBackPressed() {
        if (VideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VideoPlayer.releaseAllVideos();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
