package org.ilapin.araltimeter;

import org.ilapin.araltimeter.math.Coordinate3D;

public class Camera {

	private Coordinate3D mPosition = new Coordinate3D();
	private Coordinate3D mAngles = new Coordinate3D();
	private int mWidth;
	private int mHeight;

	public Camera() {}

	public Camera(final int width, final int height) {
		mWidth = width;
		mHeight = height;
	}

	public void setPosition(final Coordinate3D position) {
		mPosition = new Coordinate3D(position);
	}

	public void setAngles(final Coordinate3D angles) {
		mAngles = new Coordinate3D(angles);
	}

	public Coordinate3D getPosition() {
		return new Coordinate3D(mPosition);
	}

	public Coordinate3D getAngles() {
		return new Coordinate3D(mAngles);
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
