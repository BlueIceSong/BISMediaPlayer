package com.song.bismediaplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AbsoluteLayout;

import com.song.bismediaplayer.R;
import com.squareup.picasso.Picasso;

import com.song.mplibrary.Utils;
import com.song.mplibrary.VideoPlayer;
import com.song.mplibrary.VideoPlayerStandard;

/**
 * Created by song on 16/10/13.
 */

public class WebViewActivity extends AppCompatActivity {
    WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle("AboutWebView");
        setContentView(R.layout.activity_webview);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JCCallBack(), "jcvd");
        mWebView.loadUrl("file:///android_asset/jcvd.html");
    }

    public class JCCallBack {

        @JavascriptInterface
        public void adViewJieCaoVideoPlayer(final int width, final int height, final int top, final int left, final int index) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (index == 0) {
                        VideoPlayerStandard webVieo = new VideoPlayerStandard(WebViewActivity.this);
                        webVieo.setUp("http://video.jiecao.fm/11/16/c/68Tlrc9zNi3JomXpd-nUog__.mp4",
                                VideoPlayer.SCREEN_LAYOUT_LIST, "example1");
                        Picasso.with(WebViewActivity.this)
                                .load("http://img4.jiecaojingxuan.com/2016/11/16/1d935cc5-a1e7-4779-bdfa-20fd7a60724c.jpg@!640_360")
                                .into(webVieo.thumbImageView);
                        ViewGroup.LayoutParams ll = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(ll);
                        layoutParams.y = Utils.dip2px(WebViewActivity.this, top);
                        layoutParams.x = Utils.dip2px(WebViewActivity.this, left);
                        layoutParams.height = Utils.dip2px(WebViewActivity.this, height);
                        layoutParams.width = Utils.dip2px(WebViewActivity.this, width);
                        mWebView.addView(webVieo, layoutParams);
                    } else {
                        VideoPlayerStandard webVieo = new VideoPlayerStandard(WebViewActivity.this);
                        webVieo.setUp("http://video.jiecao.fm/11/14/xin/%E5%90%B8%E6%AF%92.mp4",
                                VideoPlayer.SCREEN_LAYOUT_LIST, "example2");
                        Picasso.with(WebViewActivity.this)
                                .load("http://img4.jiecaojingxuan.com/2016/11/14/a019ffc1-556c-4a85-b70c-b1b49811d577.jpg@!640_360")
                                .into(webVieo.thumbImageView);
                        ViewGroup.LayoutParams ll = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(ll);
                        layoutParams.y = Utils.dip2px(WebViewActivity.this, top);
                        layoutParams.x = Utils.dip2px(WebViewActivity.this, left);
                        layoutParams.height = Utils.dip2px(WebViewActivity.this, height);
                        layoutParams.width = Utils.dip2px(WebViewActivity.this, width);
                        mWebView.addView(webVieo, layoutParams);
                    }

                }
            });

        }
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
