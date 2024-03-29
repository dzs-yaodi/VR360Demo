package com.xw.vrlibrary.manager;

import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;

import com.xw.vrlibrary.callback.OnTextureSizeChangedCallback;
import com.xw.vrlibrary.callback.RenderCallBack;
import com.xw.vrlibrary.util.StatusHelper;
import com.xw.vrlibrary.util.constant.PanoStatus;

import java.io.IOException;

public class VRMediaPlayerWrapper implements
        SurfaceTexture.OnFrameAvailableListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener{

    private StatusHelper statusHelper;

    private RenderCallBack renderCallBack;

    private SurfaceTexture mSurfaceTexture;

    private MediaPlayer mMediaPlayer;

    private PlayerCallback playerCallback;

    private OnTextureSizeChangedCallback onTextureSizeChangedCallback;

    public VRMediaPlayerWrapper() {
        mMediaPlayer=new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
    }

    public void setRenderCallBack(RenderCallBack renderCallBack){
        this.renderCallBack=renderCallBack;
    }

    public void setSurface(int mTextureID){
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        Surface surface = new Surface(mSurfaceTexture);
        mMediaPlayer.setSurface(surface);
        surface.release();
    }

    public void doTextureUpdate(float[] mSTMatrix){
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
    }

    public void openRemoteFile(String path){
        try{
            mMediaPlayer.setDataSource(path);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setMediaPlayerFromUri(Uri uri){
        try{
            mMediaPlayer.setDataSource(statusHelper.getContext(),uri);
        }catch (IOException e){
            e.printStackTrace();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(true);
    }

    public void setMediaPlayerFromAssets(AssetFileDescriptor assetFileDescriptor){
        try{
            mMediaPlayer.setDataSource(
                    assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getDeclaredLength()
            );
        }catch (IOException e){
            e.printStackTrace();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(true);
    }

    public void setStatusHelper(StatusHelper statusHelper){
        this.statusHelper=statusHelper;
    }

    public void prepare(){
        if (statusHelper.getPanoStatus()== PanoStatus.IDLE || statusHelper.getPanoStatus()==PanoStatus.STOPPED){
            try {
                mMediaPlayer.prepareAsync();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void start(){
        PanoStatus panoStatus=statusHelper.getPanoStatus();
        if (panoStatus==PanoStatus.PREPARED || panoStatus==PanoStatus.PAUSED || panoStatus==PanoStatus.PAUSED_BY_USER) {
            mMediaPlayer.start();
            statusHelper.setPanoStatus(PanoStatus.PLAYING);
        }
    }

    public void pause(){
        PanoStatus panoStatus=statusHelper.getPanoStatus();
        if (panoStatus==PanoStatus.PLAYING){
            mMediaPlayer.pause();
            statusHelper.setPanoStatus(PanoStatus.PAUSED);
        }
    }

    public void pauseByUser(){
        PanoStatus panoStatus=statusHelper.getPanoStatus();
        if (panoStatus==PanoStatus.PLAYING) {
            mMediaPlayer.pause();
            statusHelper.setPanoStatus(PanoStatus.PAUSED_BY_USER);
        }
    }

    public void stop(){
        PanoStatus panoStatus=statusHelper.getPanoStatus();
        if (panoStatus==PanoStatus.PLAYING
                || panoStatus==PanoStatus.PREPARED
                || panoStatus==PanoStatus.PAUSED
                || panoStatus==PanoStatus.PAUSED_BY_USER){
            mMediaPlayer.stop();
            statusHelper.setPanoStatus(PanoStatus.STOPPED);
        }
    }

    public void releaseResource(){
        if(mMediaPlayer!=null){
            mMediaPlayer.setSurface(null);
            if (mSurfaceTexture!=null) mSurfaceTexture=null;
            stop();
            mMediaPlayer.release();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        statusHelper.setPanoStatus(PanoStatus.COMPLETE);
        if (playerCallback!=null){
            playerCallback.requestFinish();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        renderCallBack.renderImmediately();
        if (playerCallback!=null){
            playerCallback.updateProgress();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        statusHelper.setPanoStatus(PanoStatus.PREPARED);
        if (playerCallback!=null){
            playerCallback.updateInfo();
        }
        start();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if(onTextureSizeChangedCallback!=null)
            onTextureSizeChangedCallback.notifyTextureSizeChanged(width,height);
    }

    public void seekTo(int pos){
        if (mMediaPlayer!=null){
            PanoStatus panoStatus=statusHelper.getPanoStatus();
            if (panoStatus==PanoStatus.PLAYING
                || panoStatus==PanoStatus.PAUSED
                    || panoStatus== PanoStatus.PAUSED_BY_USER)
                mMediaPlayer.seekTo(pos);
        }
    }

    public int getDuration(){
        if (mMediaPlayer!=null){
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition(){
        if (mMediaPlayer!=null){
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public interface PlayerCallback{
        void updateProgress();
        void updateInfo();
        void requestFinish();
    }

    public void setPlayerCallback(PlayerCallback playerCallback) {
        this.playerCallback = playerCallback;
    }

    public void setOnTextureSizeChangedCallback(OnTextureSizeChangedCallback onTextureSizeChangedCallback) {
        this.onTextureSizeChangedCallback = onTextureSizeChangedCallback;
    }
}
