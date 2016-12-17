package com.song.mplibrary;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by song on 16/10/30.
 */
public abstract class VideoPlayer extends FrameLayout implements MediaPlayerListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {

    public static final String TAG = "JieCaoVideoPlayer";

    @IdRes
    public static final int FULLSCREEN_ID = 33797;
    @IdRes
    public static final int TINY_ID = 33798;
    public static final int THRESHOLD = 80;
    public static final int FULL_SCREEN_NORMAL_DELAY = 200;

    public static boolean ACTION_BAR_EXIST = true;
    public static boolean TOOL_BAR_EXIST = true;
    public static boolean WIFI_TIP_DIALOG_SHOWED = false;
    public static int FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
    public static int NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    public static long CLICK_QUIT_FULLSCREEN_TIME = 0;

    public static final int SCREEN_LAYOUT_NORMAL = 0;
    public static final int SCREEN_LAYOUT_LIST = 1;
    public static final int SCREEN_WINDOW_FULLSCREEN = 2;
    public static final int SCREEN_WINDOW_TINY = 3;

    public static final int CURRENT_STATE_NORMAL = 0;
    public static final int CURRENT_STATE_PREPARING = 1;
    public static final int CURRENT_STATE_PLAYING = 2;
    public static final int CURRENT_STATE_PLAYING_BUFFERING_START = 3;
    public static final int CURRENT_STATE_PAUSE = 5;
    public static final int CURRENT_STATE_AUTO_COMPLETE = 6;
    public static final int CURRENT_STATE_ERROR = 7;

    public int currentState = -1;
    public int currentScreen = -1;


    public String url = "";
    public Object[] objects = null;
    public int seekToInAdvance = -1;

    public ImageView startButton;

    public SeekBar progressBar;
    public ImageView fullscreenButton;
    public TextView currentTimeTextView, totalTimeTextView;
    public ViewGroup textureViewContainer;
    public ViewGroup topContainer, bottomContainer;

    protected static UserAction JC_USER_EVENT;
    protected static Timer UPDATE_PROGRESS_TIMER;

    protected int mScreenWidth;
    protected int mScreenHeight;
    protected AudioManager mAudioManager;
    protected Handler mHandler;
    protected ProgressTimerTask mProgressTimerTask;

    protected boolean mTouchingProgressBar;
    protected float mDownX;
    protected float mDownY;
    protected boolean mChangeVolume;
    protected boolean mChangePosition;
    protected int mDownPosition;
    protected int mGestureDownVolume;
    protected int mSeekTimePosition;

    public VideoPlayer(Context context) {
        super(context);
        init(context);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        View.inflate(context, getLayoutId(), this);
        startButton = (ImageView) findViewById(R.id.start);
        fullscreenButton = (ImageView) findViewById(R.id.fullscreen);
        progressBar = (SeekBar) findViewById(R.id.progress);
        currentTimeTextView = (TextView) findViewById(R.id.current);
        totalTimeTextView = (TextView) findViewById(R.id.total);
        bottomContainer = (ViewGroup) findViewById(R.id.layout_bottom);
        textureViewContainer = (ViewGroup) findViewById(R.id.surface_container);
        topContainer = (ViewGroup) findViewById(R.id.layout_top);

        startButton.setOnClickListener(this);
        fullscreenButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener(this);
        bottomContainer.setOnClickListener(this);
        textureViewContainer.setOnClickListener(this);
        textureViewContainer.setOnTouchListener(this);

        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mHandler = new Handler();
    }

    public void setUp(String url, int screen, Object... objects) {
        if (!TextUtils.isEmpty(this.url) && TextUtils.equals(this.url, url)) {
            return;
        }
//        if (((System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) < FULL_SCREEN_NORMAL_DELAY)) {
//            if ((screen != SCREEN_WINDOW_TINY)) {
//                return;
//            }
//        }
        this.url = url;
        this.objects = objects;
        this.currentScreen = screen;
        VideoPlayerManager.putFirstFloor(this);
//        if (JCVideoPlayerManager.getCurrentJcvdOnSecondFloor() != null &&
//                JCVideoPlayerManager.getCurrentJcvdOnSecondFloor().getUrl() == url &&
//                JCVideoPlayerManager.getCurrentJcvdOnSecondFloor().getScreenType() == SCREEN_WINDOW_TINY) {//setUp时候退出tiny
//            backPress();
//            return;
//        } else if (isCurrentMediaListenerOnFirstFloor()) {//setUp的时候进入tiny
//            onScrollChange();
//            setUiWitStateAndScreen(CURRENT_STATE_NORMAL);
//            return;
//        }
        setUiWitStateAndScreen(CURRENT_STATE_NORMAL);
//        if (currentState == CURRENT_STATE_NORMAL && isCurrenPlayingUrl()) {
//            JCMediaManager.instance().releaseMediaPlayer();
//        }
    }

    @Override
    public int getScreenType() {
        return currentScreen;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public int getState() {
        return currentState;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start) {
            Log.i(TAG, "onClick start [" + this.hashCode() + "] ");
            if (TextUtils.isEmpty(url)) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentState == CURRENT_STATE_NORMAL || currentState == CURRENT_STATE_ERROR) {
                if (!url.startsWith("file") && !Utils.isWifiConnected(getContext()) && !WIFI_TIP_DIALOG_SHOWED) {
                    showWifiDialog();
                    return;
                }
                prepareVideo();
                onEvent(currentState != CURRENT_STATE_ERROR ? UserAction.ON_CLICK_START_ICON : UserAction.ON_CLICK_START_ERROR);
            } else if (currentState == CURRENT_STATE_PLAYING) {
                onEvent(UserAction.ON_CLICK_PAUSE);
                Log.d(TAG, "pauseVideo [" + this.hashCode() + "] ");
                MediaManager.instance().simpleExoPlayer.setPlayWhenReady(false);
                setUiWitStateAndScreen(CURRENT_STATE_PAUSE);
            } else if (currentState == CURRENT_STATE_PAUSE) {
                onEvent(UserAction.ON_CLICK_RESUME);
                MediaManager.instance().simpleExoPlayer.setPlayWhenReady(true);
                setUiWitStateAndScreen(CURRENT_STATE_PLAYING);
            } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
                onEvent(UserAction.ON_CLICK_START_AUTO_COMPLETE);
                prepareVideo();
            }
        } else if (i == R.id.fullscreen) {
            Log.i(TAG, "onClick fullscreen [" + this.hashCode() + "] ");
            if (currentState == CURRENT_STATE_AUTO_COMPLETE) return;
            if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                //quit fullscreen
                backPress();
            } else {
                Log.d(TAG, "toFullscreenActivity [" + this.hashCode() + "] ");
                onEvent(UserAction.ON_ENTER_FULLSCREEN);
                startWindowFullscreen();
            }
        } else if (i == R.id.surface_container && currentState == CURRENT_STATE_ERROR) {
            Log.i(TAG, "onClick surfaceContainer State=Error [" + this.hashCode() + "] ");
            prepareVideo();
        }
    }

    public void prepareVideo() {
        VideoPlayerManager.completeAll();
        Log.d(TAG, "prepareVideo [" + this.hashCode() + "] ");
        initTextureView();
        addTextureView();
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        Utils.scanForActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MediaManager.CURRENT_PLAYING_URL = url;
        setUiWitStateAndScreen(CURRENT_STATE_PREPARING);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int id = v.getId();
        if (id == R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG, "onTouch surfaceContainer actionDown [" + this.hashCode() + "] ");
                    mTouchingProgressBar = true;

                    mDownX = x;
                    mDownY = y;
                    mChangeVolume = false;
                    mChangePosition = false;
                    /////////////////////
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i(TAG, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ");
                    float deltaX = x - mDownX;
                    float deltaY = y - mDownY;
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);
                    if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                        if (!mChangePosition && !mChangeVolume) {
                            if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                                cancelProgressTimer();
                                if (absDeltaX >= THRESHOLD) {
                                    // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                                    // 否则会因为mediaplayer的状态非法导致App Crash
                                    if (currentState != CURRENT_STATE_ERROR) {
                                        mChangePosition = true;
                                        mDownPosition = getCurrentPositionWhenPlaying();
                                    }
                                } else {
                                    mChangeVolume = true;
                                    mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                }
                            }
                        }
                    }
                    if (mChangePosition) {
                        int totalTimeDuration = getDuration();
                        mSeekTimePosition = (int) (mDownPosition + deltaX * totalTimeDuration / mScreenWidth);
                        if (mSeekTimePosition > totalTimeDuration)
                            mSeekTimePosition = totalTimeDuration;
                        String seekTime = Utils.stringForTime(mSeekTimePosition);
                        String totalTime = Utils.stringForTime(totalTimeDuration);

                        showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
                    }
                    if (mChangeVolume) {
                        deltaY = -deltaY;
                        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0);
                        int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);

                        showVolumeDialog(-deltaY, volumePercent);
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "onTouch surfaceContainer actionUp [" + this.hashCode() + "] ");
                    mTouchingProgressBar = false;
                    dismissProgressDialog();
                    dismissVolumeDialog();
                    if (mChangePosition) {
                        onEvent(UserAction.ON_TOUCH_SCREEN_SEEK_POSITION);
                        MediaManager.instance().simpleExoPlayer.seekTo(mSeekTimePosition);
                        int duration = getDuration();
                        int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
                        progressBar.setProgress(progress);
                    }
                    if (mChangeVolume) {
                        onEvent(UserAction.ON_TOUCH_SCREEN_SEEK_VOLUME);
                    }
                    startProgressTimer();
                    break;
            }
        }
        return false;
    }

    public void initTextureView() {
        removeTextureView();
        MediaManager.textureView = new ResizeTextureView(getContext());
        MediaManager.textureView.setSurfaceTextureListener(MediaManager.instance());
    }

    public void addTextureView() {
        Log.d(TAG, "addTextureView [" + this.hashCode() + "] ");
        LayoutParams layoutParams =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        textureViewContainer.addView(MediaManager.textureView, layoutParams);
    }

    public void removeTextureView() {
        MediaManager.savedSurfaceTexture = null;
        if (MediaManager.textureView != null && MediaManager.textureView.getParent() != null) {
            ((ViewGroup) MediaManager.textureView.getParent()).removeView(MediaManager.textureView);
        }
    }

    public void setUiWitStateAndScreen(int state) {
        currentState = state;
        switch (currentState) {
            case CURRENT_STATE_NORMAL:
                if (isCurrentMediaListenerOnFirstFloor()) {//这个if是无法取代的，否则进入全屏的时候会releaseMediaPlayer
                    cancelProgressTimer();
                    MediaManager.instance().releaseMediaPlayer();
                }
                break;
            case CURRENT_STATE_PREPARING:
                resetProgressAndTime();
                break;
            case CURRENT_STATE_PLAYING:
            case CURRENT_STATE_PAUSE:
            case CURRENT_STATE_PLAYING_BUFFERING_START:
                startProgressTimer();
                break;
            case CURRENT_STATE_ERROR:
                cancelProgressTimer();
                if (isCurrentMediaListenerOnFirstFloor()) {
                    MediaManager.instance().releaseMediaPlayer();
                }
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                cancelProgressTimer();
                progressBar.setProgress(100);
                currentTimeTextView.setText(totalTimeTextView.getText());
                break;
        }
    }

    public void startProgressTimer() {
        cancelProgressTimer();
        UPDATE_PROGRESS_TIMER = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, 300);
    }

    public void cancelProgressTimer() {
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    @Override
    public void onPrepared() {
        Log.i(TAG, "onPrepared " + " [" + this.hashCode() + "] ");

        if (currentState != CURRENT_STATE_PREPARING) return;
        if (seekToInAdvance != -1) {
            MediaManager.instance().simpleExoPlayer.seekTo(seekToInAdvance);
            seekToInAdvance = -1;
        }
        startProgressTimer();
        setUiWitStateAndScreen(CURRENT_STATE_PLAYING);
    }

    public void clearFullscreenLayout() {
        ViewGroup vp = (ViewGroup) (Utils.scanForActivity(getContext()))//.getWindow().getDecorView();
                .findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(FULLSCREEN_ID);
        View oldT = vp.findViewById(TINY_ID);
        if (oldF != null) {
            vp.removeView(oldF);
        }
        if (oldT != null) {
            vp.removeView(oldT);
        }
        showSupportActionBar(getContext());
    }

    @Override
    public void onAutoCompletion() {
        Log.i(TAG, "onAutoCompletion " + " [" + this.hashCode() + "] ");
        onEvent(UserAction.ON_AUTO_COMPLETE);
        dismissVolumeDialog();
        dismissProgressDialog();
        setUiWitStateAndScreen(CURRENT_STATE_AUTO_COMPLETE);

        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            backPress();
        }
    }

    @Override
    public void onCompletion() {
        Log.i(TAG, "onCompletion " + " [" + this.hashCode() + "] ");
        setUiWitStateAndScreen(CURRENT_STATE_NORMAL);
//        if (currentState == CURRENT_STATE_NORMAL && isCurrenPlayingUrl()) {
//            JCMediaManager.instance().releaseMediaPlayer();
//        }
        // 清理缓存变量
        textureViewContainer.removeView(MediaManager.textureView);
        MediaManager.instance().currentVideoWidth = 0;
        MediaManager.instance().currentVideoHeight = 0;

        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        Utils.scanForActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        clearFullscreenLayout();
        Utils.getAppCompActivity(getContext()).setRequestedOrientation(NORMAL_ORIENTATION);

    }

    @Override
    public boolean downStairs() {//这里这个名字这么写并不对,这是在回退的时候gotoother,如果直接gotoother就不叫这个名字
        Log.i(TAG, "downStairs " + " [" + this.hashCode() + "] ");
        Utils.getAppCompActivity(getContext()).setRequestedOrientation(NORMAL_ORIENTATION);
        showSupportActionBar(getContext());

        if (currentScreen == VideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN
                || currentScreen == VideoPlayerStandard.SCREEN_WINDOW_TINY) {
//            if (currentScreen == JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN) {
//                final Animation ra = AnimationUtils.loadAnimation(getContext(), R.anim.quit_fullscreen);
//                startAnimation(ra);
//            }
            textureViewContainer.removeView(MediaManager.textureView);
            ViewGroup vp = (ViewGroup) (Utils.scanForActivity(getContext()))//.getWindow().getDecorView();
                    .findViewById(Window.ID_ANDROID_CONTENT);
            vp.removeView(this);

            onEvent(currentScreen == VideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN ?
                    UserAction.ON_QUIT_FULLSCREEN :
                    UserAction.ON_QUIT_TINYSCREEN);

            if (VideoPlayerManager.getCurrentJcvdOnFirtFloor() == this) {//如果这是直接全屏
                VideoPlayerManager.completeAll();
                MediaManager.instance().releaseMediaPlayer();
                MediaManager.CURRENT_PLAYING_URL = null;
                return true;
            }

            VideoPlayerManager.putSecondFloor(null);
            MediaManager.instance().lastState = currentState;//save state
            if (VideoPlayerManager.getCurrentJcvdOnFirtFloor() != null) {
                VideoPlayerManager.getCurrentJcvdOnFirtFloor().goBackOnThisFloor();
                CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
            } else {
                VideoPlayerManager.completeAll();
            }
            return true;
        }

        return false;
    }

    public static long lastAutoFullscreenTime = 0;

    @Override
    public void autoFullscreen(float x) {
        if (isCurrentMediaListenerOnFirstFloor()
                && currentState == CURRENT_STATE_PLAYING
                && currentScreen != SCREEN_WINDOW_FULLSCREEN
                && currentScreen != SCREEN_WINDOW_TINY) {
            if (VideoPlayerManager.getCurrentJcvdOnSecondFloor() != null &&
                    VideoPlayerManager.getCurrentJcvdOnSecondFloor().getScreenType() == SCREEN_WINDOW_FULLSCREEN)
                return;
            if (x > 0) {
                Utils.getAppCompActivity(getContext()).setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                Utils.getAppCompActivity(getContext()).setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
            startWindowFullscreen();
        }
    }

    @Override
    public void autoQuitFullscreen() {
        if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000
                && isCurrentMediaListenerOnFirstFloor()
                && currentState == CURRENT_STATE_PLAYING
                && currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            lastAutoFullscreenTime = System.currentTimeMillis();
            backPress();
        }
    }

    @Override
    public void goBackOnThisFloor() {
        Log.i(TAG, "goBackOnThisFloor " + " [" + this.hashCode() + "] ");
        currentState = MediaManager.instance().lastState;
        setUiWitStateAndScreen(currentState);
        addTextureView();
    }

    @Override
    public void onBufferingUpdate(int percent) {
//        if (currentState != CURRENT_STATE_NORMAL && currentState != CURRENT_STATE_PREPARING) {
//            Log.v(TAG, "onBufferingUpdate " + percent + " [" + this.hashCode() + "] ");
//            setTextAndProgress(percent);
//        }
    }

    @Override
    public void onSeekComplete() {
    }

    @Override
    public void onError(int what, int extra) {
        Log.e(TAG, "onError " + what + " - " + extra + " [" + this.hashCode() + "] ");
        if (what != 38 && what != -38) {
            setUiWitStateAndScreen(CURRENT_STATE_ERROR);
        }
    }


    @Override
    public void onInfo(int what, int extra) {
//        Log.d(TAG, "onInfo what - " + what + " extra - " + extra);
//        if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
//            JCMediaManager.instance().backUpBufferState = currentState;
//            setUiWitStateAndScreen(CURRENT_STATE_PLAYING_BUFFERING_START);
//            Log.d(TAG, "MEDIA_INFO_BUFFERING_START");
//        } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
//            if (JCMediaManager.instance().backUpBufferState != -1) {
//                setUiWitStateAndScreen(JCMediaManager.instance().backUpBufferState);
//                JCMediaManager.instance().backUpBufferState = -1;
//            }
//            Log.d(TAG, "MEDIA_INFO_BUFFERING_END");
//        } else if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
//            JCMediaManager.instance().videoRotation = extra;
//            JCMediaManager.textureView.setRotation(extra);
//            cacheImageView.setRotation(JCMediaManager.instance().videoRotation);
//            Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED");
//
//
//        }
    }

    @Override
    public void onVideoSizeChanged() {
        Log.i(TAG, "onVideoSizeChanged " + " [" + this.hashCode() + "] ");
        MediaManager.textureView.setVideoSize(MediaManager.instance().getVideoSize());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "bottomProgress onStartTrackingTouch [" + this.hashCode() + "] ");
        cancelProgressTimer();
        ViewParent vpdown = getParent();
        while (vpdown != null) {
            vpdown.requestDisallowInterceptTouchEvent(true);
            vpdown = vpdown.getParent();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "bottomProgress onStopTrackingTouch [" + this.hashCode() + "] ");
        onEvent(UserAction.ON_SEEK_POSITION);
        startProgressTimer();
        ViewParent vpup = getParent();
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false);
            vpup = vpup.getParent();
        }
        if (currentState != CURRENT_STATE_PLAYING &&
                currentState != CURRENT_STATE_PAUSE) return;
        int time = seekBar.getProgress() * getDuration() / 100;
        MediaManager.instance().simpleExoPlayer.seekTo(time);
        Log.i(TAG, "seekTo " + time + " [" + this.hashCode() + "] ");
    }

    public static boolean backPress() {
        Log.i(TAG, "backPress");
        if ((System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) < FULL_SCREEN_NORMAL_DELAY)
            return false;
        if (VideoPlayerManager.getCurrentJcvd() != null) {
            return VideoPlayerManager.getCurrentJcvd().downStairs();
        }
        return false;
    }

    public void startWindowFullscreen() {
        Log.i(TAG, "startWindowFullscreen " + " [" + this.hashCode() + "] ");
        hideSupportActionBar(getContext());
        Utils.getAppCompActivity(getContext()).setRequestedOrientation(FULLSCREEN_ORIENTATION);

        ViewGroup vp = (ViewGroup) (Utils.scanForActivity(getContext()))//.getWindow().getDecorView();
                .findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(FULLSCREEN_ID);
        if (old != null) {
            vp.removeView(old);
        }
        textureViewContainer.removeView(MediaManager.textureView);
        try {
            Constructor<VideoPlayer> constructor = (Constructor<VideoPlayer>) VideoPlayer.this.getClass().getConstructor(Context.class);
            VideoPlayer videoPlayer = constructor.newInstance(getContext());
            videoPlayer.setId(FULLSCREEN_ID);
            LayoutParams lp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            vp.addView(videoPlayer, lp);
            videoPlayer.setUp(url, VideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN, objects);
            videoPlayer.setUiWitStateAndScreen(currentState);
            videoPlayer.addTextureView();
            VideoPlayerManager.putSecondFloor(videoPlayer);
//            final Animation ra = AnimationUtils.loadAnimation(getContext(), R.anim.start_fullscreen);
//            jcVideoPlayer.setAnimation(ra);
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void startWindowTiny() {
        Log.i(TAG, "startWindowTiny " + " [" + this.hashCode() + "] ");
        onEvent(UserAction.ON_ENTER_TINYSCREEN);
        if (currentState == CURRENT_STATE_NORMAL || currentState == CURRENT_STATE_ERROR) return;
        ViewGroup vp = (ViewGroup) (Utils.scanForActivity(getContext()))//.getWindow().getDecorView();
                .findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(TINY_ID);
        if (old != null) {
            vp.removeView(old);
        }
        textureViewContainer.removeView(MediaManager.textureView);

        try {
            Constructor<VideoPlayer> constructor = (Constructor<VideoPlayer>) VideoPlayer.this.getClass().getConstructor(Context.class);
            VideoPlayer videoPlayer = constructor.newInstance(getContext());
            videoPlayer.setId(TINY_ID);
            LayoutParams lp = new LayoutParams(400, 400);
            lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            vp.addView(videoPlayer, lp);
            videoPlayer.setUp(url, VideoPlayerStandard.SCREEN_WINDOW_TINY, objects);
            videoPlayer.setUiWitStateAndScreen(currentState);
            videoPlayer.addTextureView();
            VideoPlayerManager.putSecondFloor(videoPlayer);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE || currentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
                int position = getCurrentPositionWhenPlaying();
                int duration = getDuration();
//                Log.v(TAG, "onProgressUpdate " + position + "/" + duration + " [" + this.hashCode() + "] ");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setTextAndProgress();
                    }
                });
            }
        }
    }

    public int getCurrentPositionWhenPlaying() {
        int position = 0;
        if (currentState == CURRENT_STATE_PLAYING ||
                currentState == CURRENT_STATE_PAUSE ||
                currentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
            try {
//                if(JCMediaManager.instance().simpleExoPlayer!=null) {
                position = (int) MediaManager.instance().simpleExoPlayer.getCurrentPosition();
//                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    public int getDuration() {
        int duration = 0;
        try {
//            if(JCMediaManager.instance().simpleExoPlayer!=null) {
            duration = (int) MediaManager.instance().simpleExoPlayer.getDuration();
//            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    public void setTextAndProgress() {
        int position = getCurrentPositionWhenPlaying();
        int duration = getDuration();
        int progress = position * 100 / (duration == 0 ? 1 : duration);
        long secProgress = MediaManager.instance().simpleExoPlayer.getBufferedPosition();
        setProgressAndTime(progress, progressBarValue(secProgress), position, duration);
    }

    public void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime) {
        if (!mTouchingProgressBar) {
            if (progress != 0) progressBar.setProgress(progress);
        }
        if (secProgress > 95) secProgress = 100;
        if (secProgress != 0) progressBar.setSecondaryProgress(secProgress);
        if (currentTime != 0) currentTimeTextView.setText(Utils.stringForTime(currentTime));
        totalTimeTextView.setText(Utils.stringForTime(totalTime));
    }

    public void resetProgressAndTime() {
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(0);
        currentTimeTextView.setText(Utils.stringForTime(0));
        totalTimeTextView.setText(Utils.stringForTime(0));
    }

    private int progressBarValue(long position) {
        long duration = MediaManager.instance().simpleExoPlayer == null ?
                C.TIME_UNSET : MediaManager.instance().simpleExoPlayer.getDuration();
        return duration == C.TIME_UNSET || duration == 0 ? 0
                : (int) ((position * 100) / duration);
    }

    public static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseAllVideos();
                    Log.d(TAG, "AUDIOFOCUS_LOSS [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (MediaManager.instance().simpleExoPlayer != null &&
                            MediaManager.instance().simpleExoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) {
                        MediaManager.instance().simpleExoPlayer.setPlayWhenReady(false);
                    }
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    public void release() {
        if (url.equals(MediaManager.CURRENT_PLAYING_URL) &&
                (System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) > FULL_SCREEN_NORMAL_DELAY) {
            //如果正在全屏播放就不能手动调用release
            if (VideoPlayerManager.getCurrentJcvdOnFirtFloor() != null &&
                    VideoPlayerManager.getCurrentJcvdOnFirtFloor().getScreenType() != SCREEN_WINDOW_FULLSCREEN) {
                Log.d(TAG, "release [" + this.hashCode() + "]");
                releaseAllVideos();
            }
        }
    }

    //isCurrentMediaListenerOnFirstFloor and isCurrenPlayUrl should be two logic methods,isCurrentMediaListenerOnFirstFloor is for different jcvd with same
    //url when fullscreen or tiny screen. isCurrenPlayUrl is to find where is myself when back from tiny screen.
    //Sometimes they are overlap.
    public boolean isCurrentMediaListenerOnFirstFloor() {//虽然看这个函数很不爽，但是干不掉
        return VideoPlayerManager.getCurrentJcvdOnFirtFloor() != null
                && VideoPlayerManager.getCurrentJcvdOnFirtFloor() == this;
    }

//    public boolean isCurrenPlayingUrl() {
//        return url.equals(JCMediaManager.CURRENT_PLAYING_URL);
//    }

    public static void releaseAllVideos() {
        if ((System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) > FULL_SCREEN_NORMAL_DELAY) {
            Log.d(TAG, "releaseAllVideos");
            VideoPlayerManager.completeAll();
            MediaManager.instance().releaseMediaPlayer();
        }
    }

    public static void setJcUserAction(UserAction jcUserEvent) {
        JC_USER_EVENT = jcUserEvent;
    }

    public void onEvent(int type) {
        if (JC_USER_EVENT != null && isCurrentMediaListenerOnFirstFloor()) {
            JC_USER_EVENT.onEvent(type, url, currentScreen, objects);
        }
    }

    @Override
    public void onScrollChange() {//这里需要自己判断自己是 进入小窗,退出小窗,暂停还是播放
        //如果正在播放的是小窗,择机退出小窗
//        if (isShown()) {//已经显示,就退出小窗
//            backPress();
//        } else if (!isShown()) {//已经隐藏          //如果正在播放的不是小窗,择机进入小窗
//            if (currentState != CURRENT_STATE_PLAYING) {
////                releaseAllVideos();
//            } else {
//                if (JCVideoPlayerManager.getCurrentJcvdOnSecondFloor() == null) {
//                    startWindowTiny();
//                }
//            }
//        }
    }

    public static void onScroll() {//这里的本质就是全屏的时候第一层listener不接受scroll消息
        if (VideoPlayerManager.getCurrentJcvdOnFirtFloor() != null && VideoPlayerManager.getCurrentJcvdOnFirtFloor() != null &&
                VideoPlayerManager.getCurrentJcvdOnFirtFloor().getUrl() == MediaManager.CURRENT_PLAYING_URL) {
            if ((VideoPlayerManager.getCurrentJcvdOnSecondFloor() != null && VideoPlayerManager.getCurrentJcvdOnSecondFloor().getScreenType() != SCREEN_WINDOW_FULLSCREEN) ||
                    VideoPlayerManager.getCurrentJcvdOnSecondFloor() == null) {
                MediaPlayerListener mediaPlayerListener = VideoPlayerManager.getCurrentJcvdOnFirtFloor();
                if (mediaPlayerListener.getState() != CURRENT_STATE_ERROR &&
                        mediaPlayerListener.getState() != CURRENT_STATE_AUTO_COMPLETE) {
                    mediaPlayerListener.onScrollChange();
                }
            }
        }
    }

    public static void startFullscreen(Context context, Class _class, String url, Object... objects) {

        hideSupportActionBar(context);
        Utils.getAppCompActivity(context).setRequestedOrientation(FULLSCREEN_ORIENTATION);
        ViewGroup vp = (ViewGroup) (Utils.scanForActivity(context))//.getWindow().getDecorView();
                .findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(VideoPlayer.FULLSCREEN_ID);
        if (old != null) {
            vp.removeView(old);
        }
        try {
            Constructor<VideoPlayer> constructor = _class.getConstructor(Context.class);
            final VideoPlayer videoPlayer = constructor.newInstance(context);
            videoPlayer.setId(VideoPlayer.FULLSCREEN_ID);
            LayoutParams lp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            vp.addView(videoPlayer, lp);

//            final Animation ra = AnimationUtils.loadAnimation(context, R.anim.start_fullscreen);
//            jcVideoPlayer.setAnimation(ra);

            videoPlayer.setUp(url, VideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN, objects);
//            jcVideoPlayer.addTextureView();
//            JCVideoPlayerManager.putFirstFloor(jcVideoPlayer);
//            final Animation ra = AnimationUtils.loadAnimation(getContext(), R.anim.start_fullscreen);
//            jcVideoPlayer.setAnimation(ra);
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
            videoPlayer.startButton.performClick();
            VideoPlayerManager.FIRST_FLOOR_LIST.put(url, new WeakReference<>((MediaPlayerListener) videoPlayer));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideSupportActionBar(Context context) {
        if (ACTION_BAR_EXIST) {
            ActionBar ab = Utils.getAppCompActivity(context).getSupportActionBar();
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false);
                ab.hide();
            }
        }
        if (TOOL_BAR_EXIST) {
            Utils.getAppCompActivity(context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static void showSupportActionBar(Context context) {
        if (ACTION_BAR_EXIST) {
            ActionBar ab = Utils.getAppCompActivity(context).getSupportActionBar();
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false);
                ab.show();
            }
        }
        if (TOOL_BAR_EXIST) {
            Utils.getAppCompActivity(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static class JCAutoFullscreenListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {//可以得到传感器实时测量出来的变化值
            final float x = event.values[SensorManager.DATA_X];
            float y = event.values[SensorManager.DATA_Y];
            float z = event.values[SensorManager.DATA_Z];
            //过滤掉用力过猛会有一个反向的大数值
            if (((x > -15 && x < -10) || (x < 15 && x > 10)) && Math.abs(y) < 1.5) {
                if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000) {
                    if (VideoPlayerManager.getCurrentJcvdOnFirtFloor() != null) {
                        VideoPlayerManager.getCurrentJcvdOnFirtFloor().autoFullscreen(x);
                    }
                    lastAutoFullscreenTime = System.currentTimeMillis();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public void showWifiDialog() {
    }

    public void showProgressDialog(float deltaX,
                                   String seekTime, int seekTimePosition,
                                   String totalTime, int totalTimeDuration) {
    }

    public void dismissProgressDialog() {

    }

    public void showVolumeDialog(float deltaY, int volumePercent) {

    }

    public void dismissVolumeDialog() {

    }


    public abstract int getLayoutId();


}
