package org.ilapin.araltimeter.graphics;

import android.opengl.Matrix;
import org.ilapin.araltimeter.math.Coordinate3D;

public class Camera {

	private Coordinate3D mPosition = new Coordinate3D();
	private Coordinate3D mRotation = new Coordinate3D();
	private int mWidth;
	private int mHeight;
	private final float[] mFrustumProjectionMatrix = new float[16];
	private final float[] mOrthoProjectionMatrix = new float[16];

	public Camera() {}

	public void updateProjectionMatrix() {
		final float aspectRatio = (float) mWidth / mHeight;

		Matrix.frustumM(mFrustumProjectionMatrix, 0, -aspectRatio, aspectRatio, -1, 1, 1, 1000);
		Matrix.orthoM(mOrthoProjectionMatrix, 0, -aspectRatio, aspectRatio, -1, 1, -1, 1);
	}

	public float[] getFrustumProjectionMatrix() {
		return mFrustumProjectionMatrix;
	}

	public float[] getOrthoProjectionMatrix() {
		return mOrthoProjectionMatrix;
	}

	public void setPosition(final Coordinate3D position) {
		mPosition = position;
	}

	public void setRotation(final Coordinate3D rotation) {
		mRotation = rotation;
	}

	public Coordinate3D getPosition() {
		return mPosition;
	}

	public Coordinate3D getRotation() {
		return mRotation;
	}

	public int getWidth() {
		return mWidth;
	}

	public void setWidth(final int width) {
		mWidth = width;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setHeight(final int height) {
		mHeight = height;
	}
}
