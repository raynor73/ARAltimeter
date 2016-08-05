package org.ilapin.araltimeter.graphics;

import android.content.Context;
import android.opengl.GLES20;
import com.google.common.base.Preconditions;
import org.ilapin.araltimeter.Controller;
import org.ilapin.araltimeter.GlView;
import org.ilapin.araltimeter.math.Coordinate3D;

import java.util.ArrayList;
import java.util.List;

public class Scene implements Renderable, WithShaders {

	private final Camera mActiveCamera;

	private final List<Renderable> mRenderables = new ArrayList<>();
	private final List<WithShaders> mWithShaders = new ArrayList<>();

	public Scene(final Context context, final GlView view) {
		mActiveCamera = new Camera();

		final WireframeRectangle rectangle = new WireframeRectangle(context, this, 1, 1, new Color(0, 0.5f, 0, 1));
		final WireframeCompassArrow compassArrow = new WireframeCompassArrow(context, this, 1);

		mRenderables.add(rectangle);
		mRenderables.add(compassArrow);
		mWithShaders.add(rectangle);
		mWithShaders.add(compassArrow);

		mActiveCamera.getPosition().setZ(2);

		view.setController(new Controller() {

			@Override
			public void moveAlongZ(final float amount) {
				final Coordinate3D currentPosition = mActiveCamera.getPosition();
				currentPosition.setZ(currentPosition.getZ() + amount);
			}
		});
	}

	@Override
	public void initShaders() {
		for (final WithShaders withShaders : mWithShaders) {
			withShaders.initShaders();
		}
	}

	@Override
	public void render() {
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
