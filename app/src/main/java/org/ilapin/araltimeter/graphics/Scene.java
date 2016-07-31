package org.ilapin.araltimeter.graphics;

import android.content.Context;

import com.google.common.base.Preconditions;

public class Scene implements Renderable {

	private final Camera mActiveCamera;

	private final WireframeRectangle mRectangle;

	public Scene(final Context context) {
		mActiveCamera = new Camera();

		mRectangle = new WireframeRectangle(context, this, 1, 1, new Color(0, 0.5f, 0, 1));
		mActiveCamera.getPosition().setZ(2);
	}

	@Override
	public void render() {
		final int viewportWidth = mActiveCamera.getWidth();
		final int viewportHeight = mActiveCamera.getHeight();

		Preconditions.checkState(viewportWidth > 0 && viewportHeight > 0);

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
