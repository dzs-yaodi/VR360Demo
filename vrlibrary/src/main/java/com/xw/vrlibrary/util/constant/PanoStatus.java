package com.xw.vrlibrary.util.constant;

/**
 * 播放状态
 */
public enum PanoStatus {
    /**
     * 空闲
     */
    IDLE,
    /**
     * 准备
     */
    PREPARED,

    BUFFERING,
    /**
     * 播放
     */
    PLAYING,
    /**
     * 人为暂停
     */
    PAUSED_BY_USER,
    /**
     * 暂停
     */
    PAUSED,
    /**
     * 结束
     */
    STOPPED,
    /**
     * 完成
     */
    COMPLETE,
    /**
     * 错误
     */
    ERROR
}

