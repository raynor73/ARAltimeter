package org.ilapin.araltimeter.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GraphicsUtils {
	public static final int NUMBER_OF_MATRIX_ELEMENTS = 16;
	public static final int BYTES_IN_FLOAT = Float.SIZE / Byte.SIZE;
	public static final int BYTES_IN_INTEGER = Integer.SIZE / Byte.SIZE;

	public static int compileShader(final int type, final String source) {
		final int shaderHandle = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shaderHandle, source);
		GLES20.glCompileShader(shaderHandle);

		final IntBuffer intBuffer = IntBuffer.allocate(1);
		GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, intBuffer);
		if (intBuffer.get() == GLES20.GL_FALSE) {
			throw new RuntimeException("Failed to compile shader");
		}

		return shaderHandle;
	}

	public static BitmapData loadBitmap(final Context context, final int resourceId) {
		final byte[] imageBytes = loadRawResource(context, resourceId).toByteArray();

		final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bitmap.getByteCount());
		byteBuffer.order(ByteOrder.nativeOrder());
		bitmap.copyPixelsToBuffer(byteBuffer);
		byteBuffer.position(0);

		return new BitmapData(bitmap.getWidth(), bitmap.getHeight(), byteBuffer);
	}

	public static String loadShaderSource(final Context context, final int resourceId) {
		try {
			return loadRawResource(context, resourceId).toString("UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static ByteArrayOutputStream loadRawResource(final Context context, final int resourceId) {
		final InputStream in = new BufferedInputStream(context.getResources().openRawResource(resourceId));
		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			int readBytesCount;
			final byte[] buffer = new byte[1024];
			while ((readBytesCount = in.read(buffer)) >= 0) {
				out.write(buffer, 0, readBytesCount);
			}
			in.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		return out;
	}

	public static int initFloatBufferObject(final int target, final float[] data) {
		final IntBuffer bufferLocation = IntBuffer.allocate(1);

		final ByteBuffer dataByteBuffer = ByteBuffer.allocateDirect(data.length * BYTES_IN_FLOAT);
		dataByteBuffer.order(ByteOrder.nativeOrder());

		final FloatBuffer dataFloatBuffer = dataByteBuffer.asFloatBuffer();
		dataFloatBuffer.put(data);
		dataFloatBuffer.position(0);

		GLES20.glGenBuffers(1, bufferLocation);
		GLES20.glBindBuffer(target, bufferLocation.get(0));
		GLES20.glBufferData(
				target,
				data.length * BYTES_IN_FLOAT,
				dataFloatBuffer,
				GLES20.GL_STATIC_DRAW
		);

		return bufferLocation.get(0);
	}

	public static int initIntBufferObject(final int target, final int[] data) {
		final IntBuffer bufferLocation = IntBuffer.allocate(1);

		final ByteBuffer dataByteBuffer = ByteBuffer.allocateDirect(data.length * BYTES_IN_INTEGER);
		dataByteBuffer.order(ByteOrder.nativeOrder());

		final IntBuffer dataIntBuffer = dataByteBuffer.asIntBuffer();
		dataIntBuffer.put(data);
		dataIntBuffer.position(0);

		GLES20.glGenBuffers(1, bufferLocation);
		GLES20.glBindBuffer(target, bufferLocation.get(0));
		GLES20.glBufferData(
				target,
				data.length * BYTES_IN_INTEGER,
				dataIntBuffer,
				GLES20.GL_STATIC_DRAW
		);

		return bufferLocation.get(0);
	}

	public static void checkLocation(final int location, final String exceptionMessage) {
		if (location == GLES20.GL_INVALID_OPERATION || location < 0) {
			throw new RuntimeException(exceptionMessage);
		}
	}

	public static FloatBuffer createFloatBuffer(final int length) {
		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length * GraphicsUtils.BYTES_IN_FLOAT);
		byteBuffer.order(ByteOrder.nativeOrder());
		return byteBuffer.asFloatBuffer();
	}

	public static void copyDataToBuffer(final float[] data, final FloatBuffer buffer) {
		buffer.put(data);
		buffer.position(0);
	}
}
