package org.ilapin.araltimeter;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import org.ilapin.araltimeter.graphics.Scene;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlRenderer implements GLSurfaceView.Renderer {

	private Scene mScene;
	private int mWidth, mHeight;

	@Override
	public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glClearDepthf(1.0f);
		/*GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);*/
	}

	@Override
	public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
		mWidth = width;
		mHeight = height;

		if (mScene != null) {
			initScene();
		}
	}

	@Override
	public void onDrawFrame(final GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		if (mScene != null) {
			mScene.render();
		}
	}

	public void setScene(final Scene scene) {
		mScene = scene;
		if (mWidth > 0 && mHeight > 0) {
			initScene();
		}
	}

	private void initScene() {
		mScene.setViewportSize(mWidth, mHeight);
		mScene.initShaders();
	}
}
