package com.xw.vrlibrary.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.xw.vrlibrary.VRPlayActivity;

import java.io.Serializable;

public class VR360ConfigBundle implements Serializable {
    //视频/图片地址
    private String filePath;
    //控制显示图片还是视频 true -> 图片 false-> 视频
    private boolean imageModeEnabled;
    private boolean removeHotspot;
    //视频标题
    private boolean showVideoTitle;
    //截图按钮
    private boolean showScreenshotBtn;
    //螺旋仪按钮
    private boolean showGyroBtn;
    //资源样式
    private int mimeType;
    //直播
    private boolean isLive;

    private VR360ConfigBundle() {
        filePath=null;
        imageModeEnabled=false;
        removeHotspot = false;
        showVideoTitle = false;
        showScreenshotBtn = false;
        showGyroBtn = false;
        isLive = true;
    }

    public static VR360ConfigBundle newInstance(){
        return new VR360ConfigBundle();
    }

    public void startEmbeddedActivity(Context context){
        Intent intent=new Intent(context, VRPlayActivity.class);
        intent.putExtra(VRPlayActivity.CONFIG_BUNDLE,this);
        context.startActivity(intent);
    }

    public void startEmbeddedActivityWithSpecifiedBitmap(Context context, Bitmap bitmap){
        Intent intent=new Intent(context,VRPlayActivity.class);
        intent.putExtra(VRPlayActivity.CONFIG_BUNDLE,this);
        intent.putExtra("bitmap",bitmap);
        context.startActivity(intent);
    }

    public String getFilePath() {
        return filePath;
    }

    public VR360ConfigBundle setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public boolean isImageModeEnabled() {
        return imageModeEnabled;
    }

    public VR360ConfigBundle setImageModeEnabled(boolean imageModeEnabled) {
        this.imageModeEnabled = imageModeEnabled;
        return this;
    }

    public boolean isRemoveHotspot() {
        return removeHotspot;
    }

    public VR360ConfigBundle setRemoveHotspot(boolean removeHotspot) {
        this.removeHotspot = removeHotspot;
        return this;
    }


    public VR360ConfigBundle setMimeType(int mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public int getMimeType() {
        return mimeType;
    }

    public boolean isShowVideoTitle() {
        return showVideoTitle;
    }

    public VR360ConfigBundle setShowVideoTitle(boolean showVideoTitle) {
        this.showVideoTitle = showVideoTitle;
        return this;
    }

    public boolean isShowScreenshotBtn() {
        return showScreenshotBtn;
    }

    public VR360ConfigBundle setShowScreenshotBtn(boolean showScreenshotBtn) {
        this.showScreenshotBtn = showScreenshotBtn;
        return this;
    }

    public boolean isShowGyroBtn() {
        return showGyroBtn;
    }

    public VR360ConfigBundle setShowGyroBtn(boolean showGyroBtn) {
        this.showGyroBtn = showGyroBtn;
        return this;
    }

    public boolean isLive() {
        return isLive;
    }

    public VR360ConfigBundle setLive(boolean live) {
        isLive = live;
        return this;
    }
}
