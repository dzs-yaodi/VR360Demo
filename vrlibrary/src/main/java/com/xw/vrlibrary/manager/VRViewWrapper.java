package com.xw.vrlibrary.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.view.MotionEvent;

import com.xw.vrlibrary.callback.RenderCallBack;
import com.xw.vrlibrary.filters.vr.AbsHotspot;
import com.xw.vrlibrary.filters.vr.ImageHotspot;
import com.xw.vrlibrary.math.PositionOrientation;
import com.xw.vrlibrary.util.BitmapUtils;
import com.xw.vrlibrary.util.StatusHelper;
import com.xw.vrlibrary.util.TouchHelper;
import com.xw.vrlibrary.util.constant.MimeType;
import com.xw.vrlibrary.util.constant.PanoMode;
import com.xw.vrlibrary.util.constant.PanoStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VRViewWrapper {

    private VRReader mRenderer;
    private VRMediaPlayerWrapper mediaPlayerWrapper;
    private StatusHelper statusHelper;
    private GLSurfaceView glSurfaceView;
    private TouchHelper touchHelper;

    //控制 是加载视频还是图片
    private boolean imageMode;
    private Context context;
    private String filePath;
    private List<AbsHotspot> hotspotList;
    private int mimeType;
    private VR360ConfigBundle configBundle;
    private Bitmap bitmap;

    public VRViewWrapper(Context context) {
        this.context = context;
    }

    public VRViewWrapper init(){
        Uri uri=Uri.parse(filePath);
        init(context,uri);
        return this;
    }

    public VRViewWrapper setConfig(VR360ConfigBundle configBundle){
        this.configBundle=configBundle;
        filePath=configBundle.getFilePath();
        imageMode=configBundle.isImageModeEnabled();
        mimeType=configBundle.getMimeType();
        return this;
    }

    public void init(Context context, Uri uri){
        glSurfaceView.setEGLContextClientVersion(2);
        statusHelper=new StatusHelper(context);

        if(!imageMode){//视频
            mediaPlayerWrapper = new VRMediaPlayerWrapper();
            mediaPlayerWrapper.setStatusHelper(statusHelper);
            if(mimeType == MimeType.ASSETS_VIDEO) {
                try {
                    mediaPlayerWrapper.setMediaPlayerFromAssets(context.getAssets().openFd(uri.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(mimeType == MimeType.ONLINE_VIDEO) {
                mediaPlayerWrapper.openRemoteFile(uri.toString());
            }else{
                mediaPlayerWrapper.setMediaPlayerFromUri(uri);
            }
            mediaPlayerWrapper.setRenderCallBack(new RenderCallBack() {
                @Override
                public void renderImmediately() {
                    glSurfaceView.requestRender();
                }
            });
            statusHelper.setPanoStatus(PanoStatus.IDLE);
            mediaPlayerWrapper.prepare();
        }else{//图片
            if(mimeType == MimeType.ASSETS_PICTURE)
                bitmap= BitmapUtils.loadBitmapFromAssets(context,filePath);
            else if(mimeType == MimeType.LOCAL_FILE_BITMAP || mimeType == MimeType.ONLINE_BITMAP);

            else if((mimeType & MimeType.RAW_PICTURE)!=0)
                bitmap= BitmapUtils.loadBitmapFromRaw(context, Integer.valueOf(uri.getLastPathSegment()));
            else throw new RuntimeException("not implemented yet!");
        }

        mRenderer = VRReader.newInstance()
                .setStatusHelper(statusHelper)
                .setPanoMediaPlayerWrapper(mediaPlayerWrapper)
                .setImageMode(imageMode)
                .setBitmap(bitmap)
                .setRenderSizeType(VRReader.RENDER_SIZE_TEXTURE)
                .init();

        hotspotList =new ArrayList<>();

        hotspotList.add(ImageHotspot.with(statusHelper.getContext())
                .setPositionOrientation(
                        PositionOrientation.newInstance()
                                .setY(-15).setAngleX(-90).setAngleY(-90)
                )
                .setImagePath("imgs/ic_logo.png")
        );

        mRenderer.getSpherePlugin().setHotspotList(hotspotList);

        glSurfaceView.setRenderer(mRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        //使得onTouch能够监听ACTION_DOWN以外的事件
        //也可以写return panoVideoView.handleTouchEvent(event) || true;
        glSurfaceView.setClickable(true);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
            glSurfaceView.setPreserveEGLContextOnPause(true);
        }

        statusHelper.setPanoInteractiveMode(PanoMode.MOTION);

        touchHelper=new TouchHelper(statusHelper,mRenderer);
    }

    public void onPause(){
        glSurfaceView.onPause();
        if(mediaPlayerWrapper!=null && statusHelper.getPanoStatus()== PanoStatus.PLAYING){
            mediaPlayerWrapper.pause();
        }
    }

    public void onResume() {
        glSurfaceView.onResume();
        if (mediaPlayerWrapper!=null){
            if(statusHelper.getPanoStatus()==PanoStatus.PAUSED){
                mediaPlayerWrapper.start();
            }
        }
    }

    public void releaseResources(){
        if(mediaPlayerWrapper!=null){
            mediaPlayerWrapper.releaseResource();
            mediaPlayerWrapper=null;
        }
    }

    public VRMediaPlayerWrapper getMediaPlayer(){
        return mediaPlayerWrapper;
    }

    public VRReader getRenderer(){
        return mRenderer;
    }

    public StatusHelper getStatusHelper(){
        return statusHelper;
    }

    public boolean handleTouchEvent(MotionEvent event) {
        return touchHelper.handleTouchEvent(event);
    }

    public TouchHelper getTouchHelper() {
        return touchHelper;
    }

    public VRViewWrapper setGlSurfaceView(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        return this;
    }

    public boolean clearHotSpot(){
        if(hotspotList ==null) return false;
        hotspotList.clear();
        return true;
    }

    public VRViewWrapper setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    public VRViewWrapper removeDefaultHotSpot(){
        clearHotSpot();
        return this;
    }

    public static VRViewWrapper with(Context context){
        return new VRViewWrapper(context);
    }
}
