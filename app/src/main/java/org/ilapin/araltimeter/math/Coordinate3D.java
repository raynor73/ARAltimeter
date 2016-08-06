package org.ilapin.araltimeter.math;

public class Coordinate3D {
	private float mX;
	private float mY;
	private float mZ;

	public Coordinate3D() {}

	public Coordinate3D(final float x, final float y, final float z) {
		mX = x;
		mY = y;
		mZ = z;
	}

	public Coordinate3D(final Coordinate3D coordinate) {
		mX = coordinate.getX();
		mY = coordinate.getY();
		mZ = coordinate.getZ();
	}

	public float getX() {
		return mX;
	}

	public void setX(final float x) {
		mX = x;
	}

	public float getY() {
		return mY;
	}

	public void setY(final float y) {
		mY = y;
	}

	public float getZ() {
		return mZ;
	}

	public void setZ(final float z) {
		mZ = z;
	}

	public void setCoordinate(final Coordinate3D coordinate) {
		mX = coordinate.getX();
		mY = coordinate.getY();
		mZ = coordinate.getZ();
	}
}
