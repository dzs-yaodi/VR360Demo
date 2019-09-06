package com.xw.vrlibrary;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xw.vrlibrary.callback.UICallBack;
import com.xw.vrlibrary.manager.VR360ConfigBundle;
import com.xw.vrlibrary.manager.VRMediaPlayerWrapper;
import com.xw.vrlibrary.manager.VRUIController;
import com.xw.vrlibrary.manager.VRViewWrapper;
import com.xw.vrlibrary.util.UITools;
import com.xw.vrlibrary.util.constant.MimeType;
import com.xw.vrlibrary.util.constant.PanoMode;
import com.xw.vrlibrary.util.constant.PanoStatus;

public class VRPlayActivity extends AppCompatActivity {

    public static final String CONFIG_BUNDLE = "configBundle";

    private VRUIController uiController;
    private VR360ConfigBundle configBundle;
    private VRViewWrapper viewWrapper;
    private ImageView mImgBufferAnim;
    private GLSurfaceView glSurfaceView;
    private Bitmap bitmap=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_vrplay);

        init();
    }

    private void init() {
        configBundle = (VR360ConfigBundle) getIntent().getSerializableExtra(CONFIG_BUNDLE);

        if(configBundle==null){
            throw new RuntimeException("config can't be null");
        }

        mImgBufferAnim = (ImageView) findViewById(R.id.activity_imgBuffer);
        UITools.setBufferVisibility(mImgBufferAnim, !configBundle.isImageModeEnabled());

        uiController = new VRUIController((RelativeLayout)findViewById(R.id.player_toolbar_control),
                (RelativeLayout)findViewById(R.id.player_toolbar_progress),this,configBundle);

        glSurfaceView = (GLSurfaceView) findViewById(R.id.surface_view);
        if(configBundle.getMimeType() == MimeType.LOCAL_FILE_BITMAP || configBundle.getMimeType() == MimeType.ONLINE_BITMAP){
            bitmap=getIntent().getParcelableExtra("bitmap");
        }

        viewWrapper = VRViewWrapper.with(this)
                .setConfig(configBundle)
                .setGlSurfaceView(glSurfaceView)
                .setBitmap(bitmap)
                .init();

        if (configBundle.isRemoveHotspot())viewWrapper.removeDefaultHotSpot();

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                uiController.startHideControllerTimer();
                return viewWrapper.handleTouchEvent(event);
            }
        });

        uiController.setAutoHideController(true);
        uiController.setUiCallback(new UICallBack() {
            @Override
            public void requestScreenshot() {
                viewWrapper.getTouchHelper().shotScreen();
            }

            @Override
            public void requestFinish() {
                finish();
            }

            @Override
            public void changeInteractiveMode() {
                if (viewWrapper.getStatusHelper().getPanoInteractiveMode()==PanoMode.MOTION)
                    viewWrapper.getStatusHelper().setPanoInteractiveMode(PanoMode.TOUCH);
                else
                    viewWrapper.getStatusHelper().setPanoInteractiveMode(PanoMode.MOTION);
            }

            @Override
            public void changePlayingStatus() {
                if (viewWrapper.getStatusHelper().getPanoStatus()== PanoStatus.PLAYING){
                    viewWrapper.getMediaPlayer().pauseByUser();
                }else if (viewWrapper.getStatusHelper().getPanoStatus()== PanoStatus.PAUSED_BY_USER){
                    viewWrapper.getMediaPlayer().start();
                }
            }

            @Override
            public void playerSeekTo(int pos) {
                viewWrapper.getMediaPlayer().seekTo(pos);
            }

            @Override
            public int getPlayerDuration() {
                return viewWrapper.getMediaPlayer().getDuration();
            }

            @Override
            public int getPlayerCurrentPosition() {
                return viewWrapper.getMediaPlayer().getCurrentPosition();
            }
        });

        viewWrapper.getTouchHelper().setPanoUIController(uiController);
        if (!configBundle.isImageModeEnabled()){
            viewWrapper.getMediaPlayer().setPlayerCallback(new VRMediaPlayerWrapper.PlayerCallback() {
                @Override
                public void updateProgress() {
                    uiController.updateProgress();
                }

                @Override
                public void updateInfo() {
                    UITools.setBufferVisibility(mImgBufferAnim,false);
                    uiController.startHideControllerTimer();
                    uiController.setInfo();
                }

                @Override
                public void requestFinish() {
                    finish();
                }
            });
        }else{
            uiController.startHideControllerTimer();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        viewWrapper.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        viewWrapper.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        viewWrapper.releaseResources();
    }
}
