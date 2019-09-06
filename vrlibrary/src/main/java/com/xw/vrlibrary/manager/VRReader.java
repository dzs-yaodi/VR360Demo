package com.xw.vrlibrary.manager;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.xw.vrlibrary.callback.OnTextureSizeChangedCallback;
import com.xw.vrlibrary.filters.base.AbsFilter;
import com.xw.vrlibrary.filters.base.DrawImageFilter;
import com.xw.vrlibrary.filters.base.FBO;
import com.xw.vrlibrary.filters.base.FilterGroup;
import com.xw.vrlibrary.filters.base.OESFilter;
import com.xw.vrlibrary.filters.base.OrthoFilter;
import com.xw.vrlibrary.filters.base.PassThroughFilter;
import com.xw.vrlibrary.filters.vr.Sphere2DPlugin;
import com.xw.vrlibrary.util.BitmapUtils;
import com.xw.vrlibrary.util.StatusHelper;
import com.xw.vrlibrary.util.constant.AdjustingMode;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VRReader implements GLSurfaceView.Renderer {

    public static final int RENDER_SIZE_TEXTURE=0x0005;

    private StatusHelper statusHelper;
    private VRMediaPlayerWrapper vrMediaPlayerWrapper;
    private Sphere2DPlugin spherePlugin;
    private FilterGroup filterGroup;
    private AbsFilter firstPassFilter;
    private int surfaceWidth, surfaceHeight;
    private int textureWidth, textureHeight;

    private FBO fbo;
    private PassThroughFilter screenDrawer;

    private boolean imageMode;
    private boolean saveImg;
    private FilterGroup customizedFilters;
    private Bitmap bitmap;
    private int renderSizeType;
    private int resolvedWidth, resolvedHeight;

    public VRReader() {
    }

    public VRReader init(){

        saveImg=false;
        filterGroup=new FilterGroup();
        customizedFilters=new FilterGroup();

        if(!imageMode) {//视频
            firstPassFilter = new OESFilter(statusHelper.getContext());
        }else{//图片
            DrawImageFilter drawImageFilter=new DrawImageFilter(
                    statusHelper.getContext(),
                    bitmap,
                    AdjustingMode.ADJUSTING_MODE_STRETCH);
            firstPassFilter=drawImageFilter;
            drawImageFilter.setOnTextureSizeChangedCallback(new OnTextureSizeChangedCallback() {
                @Override
                public void notifyTextureSizeChanged(int width, int height) {
                    onTextureSizeChanged(width,height);
                }
            });
        }

        filterGroup.addFilter(firstPassFilter);
        spherePlugin=new Sphere2DPlugin(statusHelper);

        final OrthoFilter orthoFilter=new OrthoFilter(statusHelper,
                AdjustingMode.ADJUSTING_MODE_FIT_TO_SCREEN);

        if (vrMediaPlayerWrapper != null){
            vrMediaPlayerWrapper.setOnTextureSizeChangedCallback(new OnTextureSizeChangedCallback() {
                @Override
                public void notifyTextureSizeChanged(int width, int height) {
                    onTextureSizeChanged(width,height);
                    orthoFilter.updateProjection(width,height);
                }
            });
        }

        filterGroup.addFilter(spherePlugin);
        customizedFilters.addFilter(new PassThroughFilter(statusHelper.getContext()));
        filterGroup.addFilter(customizedFilters);
        screenDrawer=new PassThroughFilter(statusHelper.getContext());
        return this;
    }

    private void onTextureSizeChanged(int width, int height) {
        this.textureWidth = width;
        this.textureHeight = height;
        alignRenderingAreaWithTexture();
    }

    private void alignRenderingAreaWithTexture() {
        if(surfaceWidth==0 && textureWidth==0) throw new RuntimeException();
        else if(surfaceWidth==0 || textureWidth==0) return;
        if(renderSizeType == VRReader.RENDER_SIZE_TEXTURE) {
            double ratio=(double)textureWidth/surfaceWidth;
            resolvedWidth=textureWidth;
            resolvedHeight=(int) (surfaceHeight*ratio);
        }else{
            resolvedWidth =surfaceWidth;
            resolvedHeight =surfaceHeight;
        }
        filterGroup.addPreDrawTask(new Runnable() {
            @Override
            public void run() {
                fbo=FBO.newInstance().create(resolvedWidth, resolvedHeight);
                filterGroup.onFilterChanged(resolvedWidth, resolvedHeight);
            }
        });

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        filterGroup.init();
        screenDrawer.init();
        if(!imageMode)
            vrMediaPlayerWrapper.setSurface(((OESFilter)firstPassFilter).getGlOESTexture().getTextureId());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.surfaceWidth = width;
        this.surfaceHeight = height;
        screenDrawer.onFilterChanged(surfaceWidth,surfaceHeight);
        alignRenderingAreaWithTexture();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glFrontFace(GLES20.GL_CW);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        if(!imageMode){
            vrMediaPlayerWrapper.doTextureUpdate(((OESFilter)firstPassFilter).getSTMatrix());
        }
        filterGroup.drawToFBO(0,fbo);
        if(fbo!=null)
            screenDrawer.onDrawFrame(fbo.getFrameBufferTextureId());

        if (saveImg){
            BitmapUtils.sendImage(surfaceWidth, surfaceHeight,statusHelper.getContext());
            saveImg=false;
        }

        GLES20.glDisable(GLES20.GL_CULL_FACE);
    }

    public void saveImg(){
        saveImg=true;
    }

    public Sphere2DPlugin getSpherePlugin() {
        return spherePlugin;
    }

    public FilterGroup getFilterGroup() {
        return filterGroup;
    }

    public VRReader setStatusHelper(StatusHelper statusHelper) {
        this.statusHelper = statusHelper;
        return this;
    }

    public VRReader setPanoMediaPlayerWrapper(VRMediaPlayerWrapper vrMediaPlayerWrapper) {
        this.vrMediaPlayerWrapper = vrMediaPlayerWrapper;
        return this;
    }

    public VRReader setImageMode(boolean imageMode) {
        this.imageMode = imageMode;
        return this;
    }

    public VRReader setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    public static VRReader newInstance(){
        return new VRReader();
    }

    public VRReader setRenderSizeType(int renderSizeType) {
        this.renderSizeType = renderSizeType;
        return this;
    }
}
