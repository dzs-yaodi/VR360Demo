package com.xw.vrlibrary.callback;

/**
 * 播放页面 回调接口
 */
public interface UICallBack {

    //截图
    void requestScreenshot();

    //关闭/返回按钮
    void requestFinish();

    //全景360度 和 平面图片切换
    void changeInteractiveMode();

    //改变视频播放状态
    void changePlayingStatus();

    //设置进度条
    void playerSeekTo(int pos);

    //视频总长度
    int getPlayerDuration();

    //视频当前播放进度
    int getPlayerCurrentPosition();
}
