package com.example.detailmodule.adapter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.detailmodule.utils.IScreenChangedListener;
import com.example.detailmodule.utils.PanoramaGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PanoramaBall implements GLSurfaceView.Renderer, IScreenChangedListener {
    private Context mContext;
    private int mProgram;
    private int mAPositionHandler;
    private int mUProjectMatrixHandler;
    private int mATextureCoordHandler;
    private final float[] mProjectMatrix = new float[16];
    private int mSize;
    private FloatBuffer mVertexBuff;
    private FloatBuffer mTextureBuff;
    private int mTextureID;
    private int mImgResId;
    private String mImgFilePath;
    public float xAngle = 0.0F;
    public float yAngle = 90.0F;
    public float zAngle;
    final float[] mCurrMatrix = new float[16];
    final float[] mMVPMatrix = new float[16];
    private IScreenChangedListener.IScreenChanged mIScreenChanged = null;
    public PanoramaBall(Context context, String filePath) {
        mContext = context;
        mImgFilePath = filePath;
        initCoordinate();
    }

    public PanoramaBall(Context context, int resID) {
        mContext = context;
        mImgResId = resID;
        initCoordinate();
    }

    @Override
    public void setScreenChangedListener(IScreenChangedListener.IScreenChanged screenChanged) {
        mIScreenChanged = screenChanged;
    }

    public void initCoordinate() {
        int perVertex = 36;
        double perRadius = Math.PI * 2 / (double) ((float) perVertex);
        double perW = (double) (1.0F / (float) perVertex);
        double perH = (double) (1.0F / (float) perVertex);
        ArrayList<Float> vertexList = new ArrayList();
        ArrayList<Float> textureList = new ArrayList();

        int i;
        for (int a = 0; a < perVertex; ++a) {
            for (i = 0; i < perVertex; ++i) {
                float w1 = (float) ((double) a * perH);
                float h1 = (float) ((double) i * perW);
                float w2 = (float) ((double) (a + 1) * perH);
                float h2 = (float) ((double) i * perW);
                float w3 = (float) ((double) (a + 1) * perH);
                float h3 = (float) ((double) (i + 1) * perW);
                float w4 = (float) ((double) a * perH);
                float h4 = (float) ((double) (i + 1) * perW);
                textureList.add(h1);
                textureList.add(w1);
                textureList.add(h2);
                textureList.add(w2);
                textureList.add(h3);
                textureList.add(w3);
                textureList.add(h3);
                textureList.add(w3);
                textureList.add(h4);
                textureList.add(w4);
                textureList.add(h1);
                textureList.add(w1);

                float x1 = (float) (Math.sin((double) a * perRadius / 2.0D) * Math.cos((double) i * perRadius));
                float z1 = (float) (Math.sin((double) a * perRadius / 2.0D) * Math.sin((double) i * perRadius));
                float y1 = (float) Math.cos((double) a * perRadius / 2.0D);
                float x2 = (float) (Math.sin((double) (a + 1) * perRadius / 2.0D) * Math.cos((double) i * perRadius));
                float z2 = (float) (Math.sin((double) (a + 1) * perRadius / 2.0D) * Math.sin((double) i * perRadius));
                float y2 = (float) Math.cos((double) (a + 1) * perRadius / 2.0D);
                float x3 = (float) (Math.sin((double) (a + 1) * perRadius / 2.0D) * Math.cos((double) (i + 1) * perRadius));
                float z3 = (float) (Math.sin((double) (a + 1) * perRadius / 2.0D) * Math.sin((double) (i + 1) * perRadius));
                float y3 = (float) Math.cos((double) (a + 1) * perRadius / 2.0D);
                float x4 = (float) (Math.sin((double) a * perRadius / 2.0D) * Math.cos((double) (i + 1) * perRadius));
                float z4 = (float) (Math.sin((double) a * perRadius / 2.0D) * Math.sin((double) (i + 1) * perRadius));
                float y4 = (float) Math.cos((double) a * perRadius / 2.0D);
                vertexList.add(x1);
                vertexList.add(y1);
                vertexList.add(z1);
                vertexList.add(x2);
                vertexList.add(y2);
                vertexList.add(z2);
                vertexList.add(x3);
                vertexList.add(y3);
                vertexList.add(z3);
                vertexList.add(x3);
                vertexList.add(y3);
                vertexList.add(z3);
                vertexList.add(x4);
                vertexList.add(y4);
                vertexList.add(z4);
                vertexList.add(x1);
                vertexList.add(y1);
                vertexList.add(z1);
            }
        }

        mSize = textureList.size() / 2;
        float[] texture = new float[mSize * 2];
        for (i = 0; i < texture.length; ++i) {
            texture[i] = (Float) textureList.get(i);
        }
        mTextureBuff = ByteBuffer.allocateDirect(texture.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureBuff.put(texture);
        mTextureBuff.position(0);

        mSize = vertexList.size() / 3;
        float[] vertex = new float[mSize * 3];
        for (i = 0; i < vertex.length; ++i) {
            vertex[i] = (Float) vertexList.get(i);
        }
        mVertexBuff = ByteBuffer.allocateDirect(vertex.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuff.put(vertex);
        mVertexBuff.position(0);
    }

    public void onDrawFrame(GL10 arg0) {
        Matrix.rotateM(mCurrMatrix, 0, -xAngle, 1.0F, 0.0F, 0.0F);
        Matrix.rotateM(mCurrMatrix, 0, -yAngle, 0.0F, 1.0F, 0.0F);
        Matrix.rotateM(mCurrMatrix, 0, -zAngle, 0.0F, 0.0F, 1.0F);
        GLES20.glClearColor(1.0F, 1.0F, 1.0F, 1.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
        GLES20.glUniformMatrix4fv(mUProjectMatrixHandler, 1, false, getFinalMVPMatrix(), 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mSize);
    }

    public float[] getFinalMVPMatrix() {
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mCurrMatrix, 0);
        Matrix.setIdentityM(mCurrMatrix, 0);
        return mMVPMatrix;
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        if (mIScreenChanged != null) {
            mIScreenChanged.setCurrentPortrait(width < height);
        }
        float ratio = 1f;
        if (width > height) {
            ratio = (float) height / (float) width;
            Matrix.frustumM(mProjectMatrix, 0, -1.0F, 1.0F, -ratio, ratio,  1.0F, 20.0F);
        } else {
            ratio = (float) width / (float) height;
            Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1.0F, 1.0F, 1.0F, 20.0F);
        }
        Matrix.setIdentityM(mCurrMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.translateM(mProjectMatrix, 0, 0.0F, 0.0F, -2.0F);
        Matrix.scaleM(mProjectMatrix, 0, 4.0F, 4.0F, 4.0F);

        mProgram = PanoramaGLUtils.getProgram(mContext);
        GLES20.glUseProgram(mProgram);
        mAPositionHandler = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mUProjectMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uProjectMatrix");
        mATextureCoordHandler = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        mTextureID = PanoramaGLUtils.initTexture(mContext, mImgResId);
//        mTextureID = PanoramaGLUtils.initTexture(mContext, mImgFilePath);
        GLES20.glVertexAttribPointer(mAPositionHandler, 3, GLES20.GL_FLOAT, false, 0, mVertexBuff);
        GLES20.glVertexAttribPointer(mATextureCoordHandler, 2, GLES20.GL_FLOAT, false, 0, mTextureBuff);
        GLES20.glEnableVertexAttribArray(mAPositionHandler);
        GLES20.glEnableVertexAttribArray(mATextureCoordHandler);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }
}
