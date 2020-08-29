package com.example.detailmodule.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PanoramaGLUtils {
    public static final String SOURCE_DEFAULT_NAME_FRAGMENT = ""+
        "precision mediump float;\n"+
        "varying vec2 vTextureCoord;\n"+
        "uniform sampler2D uTexture;\n"+
        " \n"+
        "void main()\n"+
        "{\n"+
        "    gl_FragColor = texture2D(uTexture, vTextureCoord);\n"+
        "}";
    public static final String SOURCE_DEFAULT_NAME_VERTEX = ""+
        "attribute vec4 aPosition;\n"+
        "attribute vec2 aTextureCoord;\n"+
        "uniform mat4 uProjectMatrix;\n"+
        "varying vec2 vTextureCoord;\n"+
        " \n"+
        "void main()\n"+
        "{\n"+
        "    gl_Position = uProjectMatrix * aPosition;\n"+
        "    vTextureCoord = aTextureCoord;\n"+
        "}";

    public PanoramaGLUtils() {
    }

    public static int getProgram(Context context) {
        return getProgram(SOURCE_DEFAULT_NAME_VERTEX, SOURCE_DEFAULT_NAME_FRAGMENT);
    }

    public static int getProgram(String vertexStr, String fragmentStr) {
        int program = GLES20.glCreateProgram();
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexStr);
        GLES20.glShaderSource(fragmentShader, fragmentStr);
        GLES20.glCompileShader(vertexShader);
        GLES20.glCompileShader(fragmentShader);
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        return program;
    }

    public static String getShaderSource(Context context, String sourceName) {
        StringBuffer shaderSource = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(sourceName)));
            String tempStr = null;
            while (null != (tempStr = br.readLine())) {
                shaderSource.append(tempStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return shaderSource.toString();
    }

    protected static int bindTexture(Context context, int drawable) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inScaled = false;
        return bindTexture(BitmapFactory.decodeResource(context.getResources(), drawable, option));
    }

    protected static int bindTexture(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textures[0];
    }

    public static void checkGlError(String op) {
        int error;
        if ((error = GLES20.glGetError()) != 0) {
            Log.e("ES20_ERROR", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    public static int initTexture(Context context, int drawableId) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        InputStream is = context.getResources().openRawResource(drawableId);
        Bitmap bitmapTmp;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        try {
            bitmapTmp = BitmapFactory.decodeStream(is, null,options);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapTmp, 0);
        bitmapTmp.recycle();
        return textureId;
    }

    public static int initTexture(Context context, String filePath) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        InputStream is = null;
        Bitmap bitmapTmp = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            is = new FileInputStream(filePath);
            bitmapTmp = BitmapFactory.decodeStream(is, null,options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapTmp, 0);
        bitmapTmp.recycle();
        return textureId;
    }
}
