package org.ilapin.araltimeter;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.Surface;
import android.widget.Toast;
import org.ilapin.araltimeter.graphics.GraphicsUtils;
import org.ilapin.araltimeter.graphics.Renderable;
import org.ilapin.araltimeter.graphics.WithShaders;
import org.ilapin.araltimeter.sensors.Sensor;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraPreview implements Renderable, WithShaders, Sensor {

	private static final int NUMBER_OF_VERTICES = 4;
	private static final int NUMBER_OF_VERTEX_DIMENSIONS = 3;
	private static final int NUMBER_OF_TEXTURE_DIMENSIONS = 2;

	private final Activity mActivity;

	private int mShaderProgramLocation;
	private int mVertexBufferLocation;
	private int mIndexBufferLocation;
	private int mPositionAttributeLocation;
	private int mTextureUniformLocation;
	private int mTextureCoordinateAttributeLocation;
	private int mTextureLocation;
	private int mProjectionUniformLocation;

	private final float[] mVertices =
			new float[NUMBER_OF_VERTICES * (NUMBER_OF_VERTEX_DIMENSIONS + NUMBER_OF_TEXTURE_DIMENSIONS)];
	private final int[] mIndices = new int[] {
			0, 1, 2,
			2, 3, 0
	};
	private android.hardware.Camera mCamera;
	private SurfaceTexture mSurfaceTexture;

	private boolean mIsVericesRecalculationRequired;

	public CameraPreview(final Activity activity) {
		mActivity = activity;
	}

	private final float[] mIdentityMatrix = new float[GraphicsUtils.NUMBER_OF_MATRIX_ELEMENTS];
	{
		Matrix.setIdentityM(mIdentityMatrix, 0);
	}
	@Override
	public void render(final Scene scene) {
		mSurfaceTexture.updateTexImage();

		if (mIsVericesRecalculationRequired) {
			mIsVericesRecalculationRequired = false;

			recalculateVertices();
			mVertexBufferLocation = GraphicsUtils.initFloatBufferObject(GLES20.GL_ARRAY_BUFFER, mVertices);
			mIndexBufferLocation = GraphicsUtils.initIntBufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndices);
		}

		GLES20.glUseProgram(mShaderProgramLocation);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexBufferLocation);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferLocation);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureLocation);
		GLES20.glUniform1i(mTextureUniformLocation, 0);

		GLES20.glEnableVertexAttribArray(mPositionAttributeLocation);
		GLES20.glEnableVertexAttribArray(mTextureCoordinateAttributeLocation);

		GLES20.glVertexAttribPointer(
				mPositionAttributeLocation,
				NUMBER_OF_VERTEX_DIMENSIONS,
				GLES20.GL_FLOAT,
				false,
				(NUMBER_OF_VERTEX_DIMENSIONS + NUMBER_OF_TEXTURE_DIMENSIONS) * GraphicsUtils.BYTES_IN_FLOAT,
				0
		);
		GLES20.glVertexAttribPointer(
				mTextureCoordinateAttributeLocation,
				NUMBER_OF_TEXTURE_DIMENSIONS,
				GLES20.GL_FLOAT,
				false,
				(NUMBER_OF_VERTEX_DIMENSIONS + NUMBER_OF_TEXTURE_DIMENSIONS) * GraphicsUtils.BYTES_IN_FLOAT,
				NUMBER_OF_VERTEX_DIMENSIONS * GraphicsUtils.BYTES_IN_FLOAT
		);
		GLES20.glUniformMatrix4fv(
				mProjectionUniformLocation,
				1,
				false,
				mIdentityMatrix,//scene.getActiveCamera().getOrthoProjectionMatrix(),
				0
		);

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndices.length, GLES20.GL_UNSIGNED_INT, 0);

		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glDisableVertexAttribArray(mTextureCoordinateAttributeLocation);
		GLES20.glDisableVertexAttribArray(mPositionAttributeLocation);
	}

	@Override
	public void initShaders() {
		mShaderProgramLocation = initShadersProgram();

		mPositionAttributeLocation = GLES20.glGetAttribLocation(mShaderProgramLocation, "position");
		mTextureCoordinateAttributeLocation = GLES20.glGetAttribLocation(mShaderProgramLocation, "textureCoordinate");
		mTextureUniformLocation = GLES20.glGetUniformLocation(mShaderProgramLocation, "texture");
		mProjectionUniformLocation = GLES20.glGetUniformLocation(mShaderProgramLocation, "projection");

		mTextureLocation = initTexture();

		mSurfaceTexture = new SurfaceTexture(mTextureLocation);

		GraphicsUtils.checkLocation(mPositionAttributeLocation, "Can't acquire position attribute");
		GraphicsUtils.checkLocation(mTextureCoordinateAttributeLocation, "Can't acquire texture coordinate attribute");
		GraphicsUtils.checkLocation(mTextureUniformLocation, "Can't acquire texture uniform");
		GraphicsUtils.checkLocation(mTextureLocation, "Can't acquire texture");
		GraphicsUtils.checkLocation(mProjectionUniformLocation, "Can't acquire projection uniform");
	}

	@Override
	public void start() {
		mCamera = android.hardware.Camera.open();

		if (mCamera == null) {
			Toast.makeText(mActivity, "Can't open camera", Toast.LENGTH_SHORT).show();
			return;
		}

		final Camera.Parameters cameraParameters = mCamera.getParameters();
		final List<Camera.Size> supportedPreviewSizes = cameraParameters.getSupportedPreviewSizes();
		Camera.Size selectedSize = null;
		for (final Camera.Size size : supportedPreviewSizes) {
			if (selectedSize == null || (size.width > selectedSize.width && size.height > selectedSize.height)) {
				selectedSize = size;
			}
		}
		cameraParameters.setPreviewSize(selectedSize.width, selectedSize.height);
		mCamera.setParameters(cameraParameters);

		try {
			mCamera.setPreviewTexture(mSurfaceTexture);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		mIsVericesRecalculationRequired = true;

		mCamera.startPreview();
	}

	@Override
	public void stop() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	private int initShadersProgram() {
		final String vertexShaderSource = GraphicsUtils.loadShaderSource(mActivity, R.raw.camera_preview_vertex_shader);
		final String fragmentShaderSource =
				GraphicsUtils.loadShaderSource(mActivity, R.raw.camera_preview_fragment_shader);

		final int vertexShaderHandle = GraphicsUtils.compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
		final int fragmentShaderHandle = GraphicsUtils.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);

		final int shaderProgramLocation = GLES20.glCreateProgram();
		GLES20.glAttachShader(shaderProgramLocation, vertexShaderHandle);
		GLES20.glAttachShader(shaderProgramLocation, fragmentShaderHandle);
		GLES20.glLinkProgram(shaderProgramLocation);

		final IntBuffer intBuffer = IntBuffer.allocate(1);
		GLES20.glGetProgramiv(shaderProgramLocation, GLES20.GL_LINK_STATUS, intBuffer);
		if (intBuffer.get() == GLES20.GL_FALSE) {
			throw new RuntimeException("Failed to link shader program");
		}

		return shaderProgramLocation;
	}

	private int initTexture() {
		final IntBuffer intBuffer = IntBuffer.allocate(1);
		GLES20.glGenTextures(1, intBuffer);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, intBuffer.get(0));
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

		return intBuffer.get(0);
	}

	private void recalculateVertices() {
		final android.hardware.Camera.Size previewSize = mCamera.getParameters().getPictureSize();
		final float halfWidth = 1f;
		final float halfHeight = 1f; //(float) previewSize.height / previewSize.width / 2;

		// top left
		mVertices[0] = -halfWidth;
		mVertices[1] = halfHeight;
		mVertices[2] = 0.0f;
		// bottom left
		mVertices[5] = -halfWidth;
		mVertices[6] = -halfHeight;
		mVertices[7] = 0.0f;
		// bottom right
		mVertices[10] = halfWidth;
		mVertices[11] = -halfHeight;
		mVertices[12] = 0.0f;
		// top right
		mVertices[15] = halfWidth;
		mVertices[16] = halfHeight;
		mVertices[17] = 0.0f;

		final int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
		switch (rotation) {
			case Surface.ROTATION_0:
				// top left
				mVertices[3] = 0.0f;
				mVertices[4] = 1.0f;
				// bottom left
				mVertices[8] = 1.0f;
				mVertices[9] = 1.0f;
				// bottom right
				mVertices[13] = 1.0f;
				mVertices[14] = 0.0f;
				// top right
				mVertices[18] = 0.0f;
				mVertices[19] = 0.0f;
				break;

			case Surface.ROTATION_90:
				// top left
				mVertices[3] = 0.0f;
				mVertices[4] = 0.0f;
				// bottom left
				mVertices[8] = 0.0f;
				mVertices[9] = 1.0f;
				// bottom right
				mVertices[13] = 1.0f;
				mVertices[14] = 1.0f;
				// top right
				mVertices[18] = 1.0f;
				mVertices[19] = 0.0f;
				break;

			case Surface.ROTATION_180:
				// top left
				mVertices[3] = 1.0f;
				mVertices[4] = 0.0f;
				// bottom left
				mVertices[8] = 0.0f;
				mVertices[9] = 0.0f;
				// bottom right
				mVertices[13] = 0.0f;
				mVertices[14] = 1.0f;
				// top right
				mVertices[18] = 1.0f;
				mVertices[19] = 1.0f;
				break;

			case Surface.ROTATION_270:
				// top left
				mVertices[3] = 1.0f;
				mVertices[4] = 1.0f;
				// bottom left
				mVertices[8] = 1.0f;
				mVertices[9] = 0.0f;
				// bottom right
				mVertices[13] = 0.0f;
				mVertices[14] = 0.0f;
				// top right
				mVertices[18] = 0.0f;
				mVertices[19] = 1.0f;
				break;

			default:
				Toast.makeText(mActivity, "Unknown orientation", Toast.LENGTH_SHORT).show();
				// top left
				mVertices[3] = 0.0f;
				mVertices[4] = 1.0f;
				// bottom left
				mVertices[8] = 1.0f;
				mVertices[9] = 1.0f;
				// bottom right
				mVertices[13] = 1.0f;
				mVertices[14] = 0.0f;
				// top right
				mVertices[18] = 0.0f;
				mVertices[19] = 0.0f;
		}
	}
}
