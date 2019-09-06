package com.xw.vrlibrary.util;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;

import com.xw.vrlibrary.R;

import java.util.concurrent.TimeUnit;

public class UITools {

    public static String getShowTime(long milliseconds){
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    public static void startImageAnim(ImageView Img, int anim) {
        Img.setVisibility(View.VISIBLE);
        try {
            Img.setImageResource(anim);
            AnimationDrawable animationDrawable = (AnimationDrawable) Img.getDrawable();
            animationDrawable.start();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public static void stopImageAnim(ImageView Img) {
        try {
            AnimationDrawable animationDrawable = (AnimationDrawable) Img.getDrawable();
            animationDrawable.stop();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        Img.setVisibility(View.GONE);
    }


    //缓冲动画控制
    public static void setBufferVisibility(ImageView imgBuffer, boolean Visible) {
        if (Visible) {
            imgBuffer.setVisibility(View.VISIBLE);
            startImageAnim(imgBuffer, R.drawable.loading);
        } else {
            stopImageAnim(imgBuffer);
            imgBuffer.setVisibility(View.GONE);
        }
    }
}
