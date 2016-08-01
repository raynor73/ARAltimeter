package org.ilapin.araltimeter.graphics;

import android.opengl.Matrix;

import org.ilapin.araltimeter.math.Coordinate3D;

public class Camera {

	private Coordinate3D mPosition = new Coordinate3D();
	private Coordinate3D mRotation = new Coordinate3D();
	private int mWidth;
	private int mHeight;
	private float[] mProjectionMatrix;

	public Camera() {}

	public Camera(final int width, final int height) {
		mWidth = width;
		mHeight = height;
	}

	public void updateProjectionMatrix() {
		final float aspectRatio = (float) mWidth / mHeight;
		mProjectionMatrix = new float[16];
		Matrix.frustumM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1, 1, 1, 1000);
	}

	public float[] getProjectionMatrix() {
		return mProjectionMatrix;
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
