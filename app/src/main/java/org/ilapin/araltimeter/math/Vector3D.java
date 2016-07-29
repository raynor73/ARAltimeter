package org.ilapin.araltimeter.math;

public class Vector3D extends Coordinate3D {

	public Vector3D() {}

	public Vector3D(final Coordinate3D coordinate) {
		super(coordinate);
	}

	public Vector3D(final float x, final float y, final float z) {
		super(x, y, z);
	}

	public static Vector3D normalize(final Vector3D vector) {
		final float length = (float) Math.sqrt(
				vector.getX() * vector.getX() +
				vector.getY() * vector.getY() +
				vector.getZ() * vector.getZ()
		);
		return new Vector3D(vector.getX() / length, vector.getY() / length, vector.getZ() / length);
	}

	public static float dotProduct(final Vector3D a, final Vector3D b) {
		return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
	}
}
