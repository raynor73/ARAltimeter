package org.ilapin.araltimeter.math;

public class Size {

	private float mWidth;
	private float mHeight;

	public Size() {}

	public Size(final float width, final float height) {
		mWidth = width;
		mHeight = height;
	}

	public Size(final Size size) {
		mWidth = size.getWidth();
		mHeight = size.getHeight();
	}

	public float getWidth() {
		return mWidth;
	}

	public void setWidth(final float width) {
		mWidth = width;
	}

	public float getHeight() {
		return mHeight;
	}

	public void setHeight(final float height) {
		mHeight = height;
	}
}
