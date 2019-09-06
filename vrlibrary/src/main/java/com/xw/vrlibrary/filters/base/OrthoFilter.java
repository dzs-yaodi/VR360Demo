package com.xw.vrlibrary.filters.base;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.xw.vrlibrary.object.Plane;
import com.xw.vrlibrary.programs.GLPassThroughProgram;
import com.xw.vrlibrary.util.MatrixUtils;
import com.xw.vrlibrary.util.StatusHelper;
import com.xw.vrlibrary.util.TextureUtils;
import com.xw.vrlibrary.util.constant.PanoMode;


public class OrthoFilter extends AbsFilter {

    private int adjustingMode;

    private GLPassThroughProgram glPassThroughProgram;
    private Plane plane;

    private float[] projectionMatrix = new float[16];

    private int videoWidth, videoHeight;

    private StatusHelper statusHelper;

    public OrthoFilter(StatusHelper statusHelper, int adjustingMode) {
        this.statusHelper = statusHelper;
        glPassThroughProgram = new GLPassThroughProgram(statusHelper.getContext());
        plane = new Plane(true);
        Matrix.setIdentityM(projectionMatrix, 0);
        this.adjustingMode = adjustingMode;
    }

    @Override
    public void init() {
        glPassThroughProgram.create();
    }

    @Override
    public void onPreDrawElements() {
        super.onPreDrawElements();
        int targetSurfaceWidth = surfaceWidth;
        MatrixUtils.updateProjection(
                videoWidth,
                videoHeight,
                targetSurfaceWidth,
                surfaceHeight,
                adjustingMode,
                projectionMatrix);
        glPassThroughProgram.use();
        plane.uploadTexCoordinateBuffer(glPassThroughProgram.getTextureCoordinateHandle());
        plane.uploadVerticesBuffer(glPassThroughProgram.getPositionHandle());
        GLES20.glUniformMatrix4fv(glPassThroughProgram.getMVPMatrixHandle(), 1, false, projectionMatrix, 0);
    }

    @Override
    public void destroy() {
        glPassThroughProgram.onDestroy();
    }

    @Override
    public void onDrawFrame(int textureId) {
        onPreDrawElements();
        TextureUtils.bindTexture2D(textureId, GLES20.GL_TEXTURE0, glPassThroughProgram.getTextureSamplerHandle(), 0);

        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
        plane.draw();
    }

    public void updateProjection(int videoWidth, int videoHeight) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
    }

}
