package com.song.bismediaplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import com.song.bismediaplayer.R;
import com.song.bismediaplayer.adapter.VideoListAdapter;
import com.song.mplibrary.VideoPlayer;

/**
 * Created by Nathen on 16/8/23.
 */
public class AutoTinyListActivity extends AppCompatActivity {

    ListView listView;
    VideoListAdapter adapterVideoList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle("AutoTinyList");
        setContentView(R.layout.activity_listview_content);

        listView = (ListView) findViewById(R.id.listview);
        adapterVideoList = new VideoListAdapter(this);
        listView.setAdapter(adapterVideoList);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                VideoPlayer.onScroll();
            }
        });
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
