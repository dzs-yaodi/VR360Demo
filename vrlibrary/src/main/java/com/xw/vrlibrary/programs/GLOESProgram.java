package com.xw.vrlibrary.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.xw.vrlibrary.util.ShaderUtils;

public class GLOESProgram extends GLAbsProgram{

    private int uMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int uTextureSamplerHandle;

    public GLOESProgram(Context context){
        super(context, "filter/vsh/oes.glsl","filter/fsh/oes.glsl");
    }

    @Override
    public void create(){
        super.create();
        muSTMatrixHandle = GLES20.glGetUniformLocation(getProgramId(), "uSTMatrix");
        ShaderUtils.checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }

        uTextureSamplerHandle= GLES20.glGetUniformLocation(getProgramId(),"sTexture");
        ShaderUtils.checkGlError("glGetUniformLocation uniform samplerExternalOES sTexture");

        uMVPMatrixHandle= GLES20.glGetUniformLocation(getProgramId(),"uMVPMatrix");
        ShaderUtils.checkGlError("glGetUniformLocation uMVPMatrix");
    }

    public int getMuSTMatrixHandle() {
        return muSTMatrixHandle;
    }

    public int getUTextureSamplerHandle() { return uTextureSamplerHandle; }

    public int getMVPMatrixHandle() {
        return uMVPMatrixHandle;
    }
}
