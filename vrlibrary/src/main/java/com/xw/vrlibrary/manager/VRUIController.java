package com.xw.vrlibrary.manager;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.xw.vrlibrary.R;
import com.xw.vrlibrary.callback.UICallBack;
import com.xw.vrlibrary.util.UITools;

import java.util.Timer;
import java.util.TimerTask;

public class VRUIController implements View.OnClickListener {

    //toolbar布局
    private RelativeLayout controlToolbar;
    private ToggleButton gyroBtn;   // 陀螺仪控制按钮
    private ImageView backBtn;
    private ImageView screenshotBtn;    //截屏
    private TextView mVideoTitle;
    //底部布局
    private RelativeLayout progressToolbar;
    private SeekBar processSeekBar;                    // 播放进度条
    private TextView currTimeText;                  // 当前播放时间
    private TextView totalTimeText;             // 时间总长度
    private ToggleButton playBtn;        // 启动、暂停按钮
    //控制头部和底部的显示隐藏
    private boolean visible;

    private UICallBack uiCallback;
    //进度条touch判断
    private boolean seekBarTouched;
    //视频总长度(时间类型)
    private String lengthStr;
    //监听控制头部底部隐藏的线程
    private Timer hideControllerTimer;
    private HideControllerTimerTask hideControllerTimerTask;
    private boolean autoHideController;

    private Context mContext;
    private VR360ConfigBundle configBundle;

    public VRUIController(RelativeLayout controlToolbar, RelativeLayout progressToolbar, Context context, VR360ConfigBundle configBundle) {
        this.controlToolbar = controlToolbar;
        this.progressToolbar = progressToolbar;
        this.mContext = context;
        this.configBundle = configBundle;
        visible = true;
        initView();
    }

    private void initView() {
        //controlToolbar
        backBtn = (ImageView) controlToolbar.findViewById(R.id.back_btn);
        gyroBtn = (ToggleButton) controlToolbar.findViewById(R.id.gyro_btn);
        screenshotBtn = (ImageView) controlToolbar.findViewById(R.id.screenshot_btn);
        mVideoTitle = controlToolbar.findViewById(R.id.video_title);
        //progressToolbar
        processSeekBar = (SeekBar) progressToolbar.findViewById(R.id.progress_seek_bar);
        currTimeText = (TextView) progressToolbar.findViewById(R.id.txt_time_curr);
        totalTimeText = (TextView) progressToolbar.findViewById(R.id.txt_time_total);
        playBtn = (ToggleButton) progressToolbar.findViewById(R.id.play_btn);

        backBtn.setOnClickListener(this);
        //螺旋仪
        if (configBundle.isShowGyroBtn()) {
            gyroBtn.setVisibility(View.VISIBLE);
            gyroBtn.setOnClickListener(this);
        }
        //截图
        if (configBundle.isShowScreenshotBtn()) {
            screenshotBtn.setVisibility(View.VISIBLE);
            screenshotBtn.setOnClickListener(this);
        }
        //标题
        if (configBundle.isShowVideoTitle()){
            mVideoTitle.setText(Uri.parse(configBundle.getFilePath()).getLastPathSegment());
            mVideoTitle.setVisibility(View.VISIBLE);
        }

        //true-》图片或者直播 ，隐藏底部布局
        if (configBundle.isImageModeEnabled() || configBundle.isLive()){
            progressToolbar.setVisibility(View.GONE);
        }else{
            playBtn.setOnClickListener(this);
            seekBarTouched = false;
            processSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    cancelHideControllerTimer();
                    seekBarTouched = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    uiCallback.playerSeekTo(seekBar.getProgress());
                    seekBarTouched = false;
                    startHideControllerTimer();
                }
            });
        }

    }

    public void startHideControllerTimer() {
        if (!autoHideController) return;
        cancelHideControllerTimer();
        hideControllerTimer = new Timer();
        hideControllerTimerTask = new HideControllerTimerTask();
        hideControllerTimer.schedule(hideControllerTimerTask, 2666);
    }

    public void cancelHideControllerTimer() {
        if (hideControllerTimer != null) {
            hideControllerTimer.cancel();
        }
        if (hideControllerTimerTask != null) {
            hideControllerTimerTask.cancel();
        }
    }

    @Override
    public void onClick(View v) {

        startHideControllerTimer();
        if (v.getId() == R.id.gyro_btn) {
            uiCallback.changeInteractiveMode();
        }else if (v.getId() == R.id.screenshot_btn){
            uiCallback.requestScreenshot();
        }else if (v.getId() == R.id.back_btn){
            uiCallback.requestFinish();
        }else if (v.getId() == R.id.play_btn){
            uiCallback.changePlayingStatus();
        }
    }

    public class HideControllerTimerTask extends TimerTask {
        @Override
        public void run() {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hide();
                }
            });
        }
    }

    public void hide() {
        if (!visible) return;
        visible = false;
        progressToolbar.setVisibility(View.GONE);
        controlToolbar.setVisibility(View.GONE);
    }

    public void show() {
        if (visible) return;
        visible = true;
        if (!configBundle.isImageModeEnabled() && !configBundle.isLive()){
            progressToolbar.setVisibility(View.VISIBLE);
        }

        controlToolbar.setVisibility(View.VISIBLE);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setInfo() {
        processSeekBar.setProgress(0);
        int duration = uiCallback.getPlayerDuration();
        processSeekBar.setMax(duration);

        lengthStr = UITools.getShowTime(duration);
        currTimeText.setText("00:00:00");
        totalTimeText.setText(lengthStr);
    }

    public void setUiCallback(UICallBack uiCallback) {
        this.uiCallback = uiCallback;
    }


    public void updateProgress() {
        if (!seekBarTouched)
            handleProgress.sendEmptyMessage(0);
    }
    private Handler handleProgress = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://更新进度条
                    int position = uiCallback.getPlayerCurrentPosition();
                    if (position >= 0) {
                        processSeekBar.setProgress(position);
                        String cur = UITools.getShowTime(position);
                        currTimeText.setText(cur);
                    }
                    break;
            }
        }
    };

    public boolean isAutoHideController() {
        return autoHideController;
    }

    public void setAutoHideController(boolean autoHideController) {
        this.autoHideController = autoHideController;
    }
}
