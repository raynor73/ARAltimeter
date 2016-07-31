package org.ilapin.araltimeter.graphics;

public class Color {

	private float mRed;
	private float mGreen;
	private float mBlue;
	private float mAlpha;

	public Color(final float red, final float green, final float blue, final float alpha) {
		mRed = red;
		mGreen = green;
		mBlue = blue;
		mAlpha = alpha;
	}

	public Color(final Color color) {
		mRed = color.getRed();
		mGreen = color.getGreen();
		mBlue = color.getBlue();
		mAlpha = color.getAlpha();
	}

	public Color() {
		mRed = 0;
		mGreen = 0;
		mBlue = 0;
		mAlpha = 1;
	}

	public float getRed() {
		return mRed;
	}

	public float getGreen() {
		return mGreen;
	}

	public float getBlue() {
		return mBlue;
	}

	public float getAlpha() {
		return mAlpha;
	}

	public void setRed(final float red) {
		mRed = red;
	}

	public void setGreen(final float green) {
		mGreen = green;
	}

	public void setBlue(final float blue) {
		mBlue = blue;
	}

	public void setAlpha(final float alpha) {
		mAlpha = alpha;
	}
}
