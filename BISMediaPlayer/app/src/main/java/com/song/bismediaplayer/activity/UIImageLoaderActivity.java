package com.song.bismediaplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.song.bismediaplayer.R;
import com.squareup.picasso.Picasso;

import com.song.mplibrary.VideoPlayer;
import com.song.mplibrary.VideoPlayerStandard;
import com.song.bismediaplayer.customView.VideoPlayerStandardFresco;

//import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * These load image library is too large, We should stand out this library is small, so remove other load image library
 * Created by song on 16/10/31.
 */
@Deprecated // this should in small change
public class UIImageLoaderActivity extends AppCompatActivity {
    VideoPlayerStandard videoController1;
    VideoPlayerStandardFresco videoController2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadimage);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle("LoadImageDemo");

        videoController1 = (VideoPlayerStandard) findViewById(R.id.videocontroller1);
        videoController1.setUp("http://2449.vod.myqcloud.com/2449_43b6f696980311e59ed467f22794e792.f20.mp4", VideoPlayer.SCREEN_LAYOUT_LIST,
                "example");
        Picasso.with(this)
                .load("http://n.sinaimg.cn/transform/20140317/anshist0237092.jpeg")
                .into(videoController1.thumbImageView);
        /** ImageLoader **/
//        ImageLoader.getInstance().displayImage("http://img4.jiecaojingxuan.com/2016/8/17/f2dbd12e-b1cb-4daf-aff1-8c6be2f64d1a.jpg",
//                videoController1.thumbImageView);
        /** Glide **/
//        Glide.with(this)
//                .load("http://img4.jiecaojingxuan.com/2016/8/17/f2dbd12e-b1cb-4daf-aff1-8c6be2f64d1a.jpg")
//                .into(videoController1.thumbImageView);
        /** volley **/
//        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
//        com.android.volley.toolbox.ImageLoader imageLoader = new com.android.volley.toolbox.ImageLoader(mQueue, new VolleyBitmapCache());
//        com.android.volley.toolbox.ImageLoader.ImageListener listener =
//                com.android.volley.toolbox.ImageLoader.getImageListener(videoController1.thumbImageView, R.mipmap.ic_launcher, R.mipmap.ic_launcher);
//        imageLoader.get("http://img4.jiecaojingxuan.com/2016/8/17/f2dbd12e-b1cb-4daf-aff1-8c6be2f64d1a.jpg", listener);
        /** Fresco **/
//        Fresco.initialize(this);
//        videoController2 = (JCVideoPlayerStandardFresco) findViewById(R.id.videocontroller2);
//        videoController2.setUp("http://video.jiecao.fm/8/17/%E6%8A%AB%E8%90%A8.mp4", JCVideoPlayer.SCREEN_LAYOUT_LIST,
//                "嫂子打电话");
//        Uri uri = Uri.parse("http://img4.jiecaojingxuan.com/2016/8/17/f2dbd12e-b1cb-4daf-aff1-8c6be2f64d1a.jpg");
//        videoController2.thumbImageView.setImageURI(uri);

    }

//    public class VolleyBitmapCache implements com.android.volley.toolbox.ImageLoader.ImageCache {
//        private LruCache<String, Bitmap> cache;
//
//        public VolleyBitmapCache() {
//            cache = new LruCache<String, Bitmap>(8 * 1024 * 1024) {
//                @Override
//                protected int sizeOf(String key, Bitmap bitmap) {
//                    return bitmap.getRowBytes() * bitmap.getHeight();
//                }
//            };
//        }
//
//        @Override
//        public Bitmap getBitmap(String url) {
//            return cache.get(url);
//        }
//
//        @Override
//        public void putBitmap(String url, Bitmap bitmap) {
//            cache.put(url, bitmap);
//        }
//    }

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
