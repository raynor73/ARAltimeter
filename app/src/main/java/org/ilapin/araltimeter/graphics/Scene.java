package org.ilapin.araltimeter.graphics;

import android.content.Context;
import android.opengl.GLES20;

import com.google.common.base.Preconditions;

public class Scene implements Renderable, WithShaders {

	private final Camera mActiveCamera;

	private final WireframeRectangle mRectangle;

	public Scene(final Context context) {
		mActiveCamera = new Camera();

		mRectangle = new WireframeRectangle(context, this, 1, 1, new Color(0, 0.5f, 0, 1));
		mActiveCamera.getPosition().setZ(2);
	}

	@Override
	public void initShaders() {
		mRectangle.initShaders();
	}

	@Override
	public void render() {
		final int viewportWidth = mActiveCamera.getWidth();
		final int viewportHeight = mActiveCamera.getHeight();

		Preconditions.checkState(viewportWidth > 0 && viewportHeight > 0);

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		mRectangle.render();
	}

	public void setViewportSize(final int width, final int height) {
		mActiveCamera.setWidth(width);
		mActiveCamera.setHeight(height);
		mActiveCamera.updateProjectionMatrix();
	}

	public Camera getActiveCamera() {
		return mActiveCamera;
	}
}
