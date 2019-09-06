package com.xw.vrlibrary.filters.vr;

import android.opengl.GLES20;
import android.opengl.Matrix;


import com.xw.vrlibrary.filters.base.AbsFilter;
import com.xw.vrlibrary.object.Sphere;
import com.xw.vrlibrary.programs.GLPassThroughProgram;
import com.xw.vrlibrary.util.StatusHelper;
import com.xw.vrlibrary.util.TextureUtils;
import com.xw.vrlibrary.util.constant.PanoMode;

import java.util.List;

public class Sphere2DPlugin extends AbsFilter {
    private Sphere sphere;
    private GLPassThroughProgram glSphereProgram;
    private SensorEventHandler sensorEventHandler;
    private StatusHelper statusHelper;

    private float[] rotationMatrix = new float[16];

    //Sphere/touch/sensor
    private float[] modelMatrix = new float[16];
    //gluLookAt
    private float[] viewMatrix = new float[16];
    //perspective/scaling
    private float[] projectionMatrix = new float[16];

    private float[] modelViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private float ratio;

    //Touch Control
    private float mDeltaX;
    private float mDeltaY;
    private float mScale;

    private OrientationHelper orientationHelper;
    private List<AbsHotspot> hotspotList;

    public Sphere2DPlugin(StatusHelper statusHelper) {
        this.statusHelper = statusHelper;
        mDeltaX = -90;
        mDeltaY = 0;
        mScale = 1;
        sphere = new Sphere(18, 75, 150);
        sensorEventHandler = new SensorEventHandler();
        sensorEventHandler.setStatusHelper(statusHelper);
        sensorEventHandler.setSensorHandlerCallback(new SensorEventHandler.SensorHandlerCallback() {
            @Override
            public void updateSensorMatrix(float[] sensorMatrix) {
                System.arraycopy(sensorMatrix, 0, rotationMatrix, 0, 16);
            }
        });
        sensorEventHandler.init();

        glSphereProgram = new GLPassThroughProgram(statusHelper.getContext());
        initMatrix();

        orientationHelper = new OrientationHelper();
    }

    @Override
    public void init() {
        glSphereProgram.create();
        for (AbsHotspot hotSpot : hotspotList)
            hotSpot.init();
    }

    @Override
    public void destroy() {
        glSphereProgram.onDestroy();
        for (AbsHotspot hotSpot : hotspotList)
            hotSpot.destroy();
    }

    @Override
    public void onDrawFrame(int textureId) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        glSphereProgram.use();
        sphere.uploadTexCoordinateBuffer(glSphereProgram.getTextureCoordinateHandle());
        sphere.uploadVerticesBuffer(glSphereProgram.getPositionHandle());

        float currentDegree = (float) (Math.toDegrees(Math.atan(mScale)) * 2);
        Matrix.perspectiveM(projectionMatrix, 0, currentDegree, ratio, 1f, 500f);

        Matrix.setIdentityM(modelMatrix, 0);
        if (statusHelper.getPanoInteractiveMode() == PanoMode.MOTION) {
            orientationHelper.recordRotation(rotationMatrix);
            System.arraycopy(rotationMatrix, 0, modelMatrix, 0, 16);
            orientationHelper.revertRotation(modelMatrix);
        } else {
            Matrix.rotateM(modelMatrix, 0, mDeltaY, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(modelMatrix, 0, mDeltaX, 0.0f, 1.0f, 0.0f);
        }
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

        GLES20.glUniformMatrix4fv(glSphereProgram.getMVPMatrixHandle(), 1, false, mMVPMatrix, 0);

        TextureUtils.bindTexture2D(textureId, GLES20.GL_TEXTURE0, glSphereProgram.getTextureSamplerHandle(), 0);

        onPreDrawElements();

        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
        sphere.draw();
        drawHotSpot();

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onFilterChanged(int width, int height) {
        super.onFilterChanged(width, height);
        ratio = (float) width / height;
        for (AbsHotspot hotSpot : hotspotList)
            hotSpot.onFilterChanged(width, height);
    }


    private void initMatrix() {
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(modelMatrix, 0);

        Matrix.setIdentityM(projectionMatrix, 0);
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.setLookAtM(viewMatrix, 0,
                0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 1.0f, 0.0f);
    }

    public SensorEventHandler getSensorEventHandler() {
        return sensorEventHandler;
    }

    public float getDeltaX() {
        return mDeltaX;
    }

    public void setDeltaX(float mDeltaX) {
        this.mDeltaX = mDeltaX;
    }

    public float getDeltaY() {
        return mDeltaY;
    }

    public void setDeltaY(float mDeltaY) {
        this.mDeltaY = mDeltaY;
    }

    public void updateScale(float scaleFactor) {
        mScale = mScale + (1.0f - scaleFactor);
        mScale = Math.max(0.122f, Math.min(1.0f, mScale));
    }

    public OrientationHelper getOrientationHelper() {
        return orientationHelper;
    }

    //FIXME:code about hotspot is temporary
    private void drawHotSpot() {
        for (AbsHotspot hotSpot : hotspotList) {
            hotSpot.setModelMatrix(modelMatrix);
            hotSpot.setViewMatrix(viewMatrix);
            hotSpot.setProjectionMatrix(projectionMatrix);
            hotSpot.onDrawFrame(0);
        }
    }

    public void setHotspotList(List<AbsHotspot> hotspotList) {
        this.hotspotList = hotspotList;
    }
}
