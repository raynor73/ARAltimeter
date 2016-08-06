package org.ilapin.araltimeter;

import android.content.Context;
import android.opengl.GLES20;

import com.google.common.base.Preconditions;

import org.ilapin.araltimeter.graphics.Camera;
import org.ilapin.araltimeter.graphics.Color;
import org.ilapin.araltimeter.graphics.Renderable;
import org.ilapin.araltimeter.graphics.WithShaders;

import java.util.ArrayList;
import java.util.List;

public class Scene implements Renderable, WithShaders {

	private final Camera mActiveCamera;

	private final List<Renderable> mRenderables = new ArrayList<>();
	private final List<WithShaders> mWithShaders = new ArrayList<>();
	private final WireframeCompassArrow mCompassArrow;
	private final Model mModel;

	public Scene(final Context context, final Model model) {
		mModel = model;
		mActiveCamera = new Camera();

		final WireframeRectangle rectangle = new WireframeRectangle(context, this, 1, 1, new Color(0, 0.5f, 0, 1));
		mCompassArrow = new WireframeCompassArrow(context, this, 1);

		mRenderables.add(rectangle);
		mRenderables.add(mCompassArrow);
		mWithShaders.add(rectangle);
		mWithShaders.add(mCompassArrow);

		mActiveCamera.getPosition().setZ(2);
	}

	@Override
	public void initShaders() {
		for (final WithShaders withShaders : mWithShaders) {
			withShaders.initShaders();
		}
	}

	@Override
	public void render() {
		mCompassArrow.getRotation().setCoordinate(mModel.getRawCompassArrowRotation());

		final int viewportWidth = mActiveCamera.getWidth();
		final int viewportHeight = mActiveCamera.getHeight();

		Preconditions.checkState(viewportWidth > 0 && viewportHeight > 0);

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		for (final Renderable renderable : mRenderables) {
			renderable.render();
		}
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
