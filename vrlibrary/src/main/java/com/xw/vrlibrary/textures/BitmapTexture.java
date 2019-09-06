package com.xw.vrlibrary.textures;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.xw.vrlibrary.util.BitmapUtils;
import com.xw.vrlibrary.util.TextureUtils;

public class BitmapTexture {
    private int imageTextureId;
    private int imageSize[];

    public BitmapTexture() {
        imageSize=new int[2];
    }

    public BitmapTexture loadWithAssetFile(Context context, String filePath){
        return loadBitmap(BitmapUtils.loadBitmapFromAssets(context,filePath));
    }

    public BitmapTexture loadBitmap(Bitmap bitmap){
        imageTextureId= TextureUtils.getTextureFromBitmap(bitmap,imageSize);
        return this;
    }

    public int getImageTextureId() {
        return imageTextureId;
    }

    public int getImageWidth(){
        return imageSize[0];
    }

    public int getImageHeight(){
        return imageSize[1];
    }

    public void destroy() {
        GLES20.glDeleteTextures(1, new int[]{imageTextureId}, 0);
    }
}
