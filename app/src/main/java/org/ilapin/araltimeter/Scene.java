package org.ilapin.araltimeter;

import com.google.common.base.Preconditions;

public class Scene implements Renderable {

	private final Camera mActiveCamera;

	public Scene() {
		mActiveCamera = new Camera();
	}

	@Override
	public void render() {
		final int viewportWidth = mActiveCamera.getWidth();
		final int viewportHeight = mActiveCamera.getHeight();

		Preconditions.checkState(viewportWidth > 0 && viewportHeight > 0);

	}

	public void setViewportSize(final int width, final int height) {
		mActiveCamera.setWidth(width);
		mActiveCamera.setHeight(height);
	}
}
