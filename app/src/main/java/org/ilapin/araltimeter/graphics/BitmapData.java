package org.ilapin.araltimeter.graphics;

import java.nio.Buffer;

class BitmapData {
	private final int mWidth;
	private final int mHeight;
	private final Buffer mBitmapBuffer;

	BitmapData(final int width, final int height, final Buffer bitmapBuffer) {
		mWidth = width;
		mHeight = height;
		mBitmapBuffer = bitmapBuffer;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public Buffer getBitmapBuffer() {
		return mBitmapBuffer;
	}
}
