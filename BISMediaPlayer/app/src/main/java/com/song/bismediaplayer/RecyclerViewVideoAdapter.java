package com.song.bismediaplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import com.song.mplibrary.VideoPlayer;
import com.song.mplibrary.VideoPlayerStandard;

public class RecyclerViewVideoAdapter extends RecyclerView.Adapter<RecyclerViewVideoAdapter.MyViewHolder> {

    int[] videoIndexs = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private Context context;
    public static final String TAG = "RecyclerViewVideoAdapter";

    public RecyclerViewVideoAdapter(Context context) {
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                context).inflate(R.layout.item_videoview, parent,
                false));
        return holder;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder [" + holder.jcVideoPlayer.hashCode() + "] position=" + position);

        holder.jcVideoPlayer.setUp(
                VideoConstant.videoUrls[0][position], VideoPlayer.SCREEN_LAYOUT_LIST,
                VideoConstant.videoTitles[0][position]);
        Picasso.with(holder.jcVideoPlayer.getContext())
                .load(VideoConstant.videoThumbs[0][position])
                .into(holder.jcVideoPlayer.thumbImageView);
    }

    @Override
    public int getItemCount() {
        return videoIndexs.length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        VideoPlayerStandard jcVideoPlayer;

        public MyViewHolder(View itemView) {
            super(itemView);
            jcVideoPlayer = (VideoPlayerStandard) itemView.findViewById(R.id.videoplayer);
        }
    }

}
