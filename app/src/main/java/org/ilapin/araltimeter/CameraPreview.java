package org.ilapin.araltimeter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import org.ilapin.araltimeter.graphics.Camera;
import org.ilapin.araltimeter.graphics.GraphicsUtils;
import org.ilapin.araltimeter.graphics.Renderable;
import org.ilapin.araltimeter.graphics.WithShaders;
import org.ilapin.araltimeter.math.Coordinate3D;
import org.ilapin.araltimeter.sensors.Sensor;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@SuppressWarnings("deprecation")
public class CameraPreview implements Renderable, WithShaders, Sensor {

	private static final int NUMBER_OF_VERTICES = 4;
	private static final int NUMBER_OF_VERTEX_DIMENSIONS = 3;
	private static final int NUMBER_OF_TEXTURE_DIMENSIONS = 2;
	private static final int NUMBER_OF_TRIANGLE_VERTICES = 3;

	private final Context mContext;
	private final FloatBuffer mModelViewMatrixBuffer;
	private final FloatBuffer mProjectionMatrixBuffer;
	private Coordinate3D mPosition;
	private Coordinate3D mRotation;
	private float mHeight;
	private float mWidth;

	private int mShaderProgramLocation;
	private int mVertexBufferLocation;
	private int mIndexBufferLocation;
	private int mPositionAttributeLocation;
	private int mProjectionUniformLocation;
	private int mModelViewUniformLocation;
	private int mTextureUniformLocation;
	private int mTextureCoordinateAttributeLocation;
	private int mTextureLocation;

	private final float[] mVertices =
			new float[NUMBER_OF_VERTICES * (NUMBER_OF_VERTEX_DIMENSIONS + NUMBER_OF_TEXTURE_DIMENSIONS)];
	private final int[] mIndices = new int[] {
			0, 1, 2,
			2, 3, 0
	};
	private final float[] mModelViewMatrix = new float[GraphicsUtils.NUMBER_OF_MATRIX_ELEMENTS];
	private android.hardware.Camera mCamera;
	private SurfaceTexture mSurfaceTexture;
	private final SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {

		@Override
		public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
			// do nothing
		}
	};

	public CameraPreview(final Context context, final float width, final float height) {
		mContext = context;

		mWidth = width;
		mHeight = height;
		mPosition = new Coordinate3D();
		mRotation = new Coordinate3D();

		recalculateVertices();

		mModelViewMatrixBuffer = GraphicsUtils.createFloatBuffer(GraphicsUtils.NUMBER_OF_MATRIX_ELEMENTS);
		mProjectionMatrixBuffer = GraphicsUtils.createFloatBuffer(GraphicsUtils.NUMBER_OF_MATRIX_ELEMENTS);
	}

	@Override
	public void render(final Scene scene) {
		final Camera camera = scene.getActiveCamera();
		final Coordinate3D cameraPosition = camera.getPosition();

		GLES20.glUseProgram(mShaderProgramLocation);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexBufferLocation);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferLocation);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureLocation);
		GLES20.glUniform1i(mTextureLocation, 0);

		GLES20.glEnableVertexAttribArray(mPositionAttributeLocation);
		GLES20.glEnableVertexAttribArray(mTextureCoordinateAttributeLocation);

		Matrix.setIdentityM(mModelViewMatrix, 0);
		Matrix.translateM(
				mModelViewMatrix,
				0,
				mPosition.getX() - cameraPosition.getX(),
				mPosition.getY() - cameraPosition.getY(),
				mPosition.getZ() - cameraPosition.getZ()
		);
		Matrix.rotateM(mModelViewMatrix, 0, mRotation.getX(), 1, 0, 0);
		Matrix.rotateM(mModelViewMatrix, 0, mRotation.getY(), 0, 1, 0);
		Matrix.rotateM(mModelViewMatrix, 0, mRotation.getZ(), 0, 0, 1);
		GraphicsUtils.copyDataToBuffer(mModelViewMatrix, mModelViewMatrixBuffer);

		GraphicsUtils.copyDataToBuffer(camera.getProjectionMatrix(), mProjectionMatrixBuffer);

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
		GLES20.glUniformMatrix4fv(mProjectionUniformLocation, 1, false, mProjectionMatrixBuffer);
		GLES20.glUniformMatrix4fv(mModelViewUniformLocation, 1, false, mModelViewMatrixBuffer);

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndices.length, GLES20.GL_UNSIGNED_INT, 0);

		GLES20.glDisableVertexAttribArray(mTextureCoordinateAttributeLocation);
		GLES20.glDisableVertexAttribArray(mPositionAttributeLocation);
	}

	@Override
	public void initShaders() {
		mShaderProgramLocation = initShadersProgram();

		mVertexBufferLocation = GraphicsUtils.initFloatBufferObject(GLES20.GL_ARRAY_BUFFER, mVertices);
		mIndexBufferLocation = GraphicsUtils.initIntBufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndices);

		mPositionAttributeLocation = GLES20.glGetAttribLocation(mShaderProgramLocation, "position");
		mTextureCoordinateAttributeLocation = GLES20.glGetAttribLocation(mShaderProgramLocation, "textureCoordinate");
		mTextureUniformLocation = GLES20.glGetUniformLocation(mShaderProgramLocation, "texture");
		mProjectionUniformLocation = GLES20.glGetUniformLocation(mShaderProgramLocation, "projection");
		mModelViewUniformLocation = GLES20.glGetUniformLocation(mShaderProgramLocation, "modelView");

		mTextureLocation = initTexture();

		mSurfaceTexture = new SurfaceTexture(mTextureLocation);
		mSurfaceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);

		GraphicsUtils.checkLocation(mPositionAttributeLocation, "Can't acquire position attribute");
		GraphicsUtils.checkLocation(mProjectionUniformLocation, "Can't acquire projection uniform");
		GraphicsUtils.checkLocation(mModelViewUniformLocation, "Can't acquire modelView uniform");
		GraphicsUtils.checkLocation(mTextureCoordinateAttributeLocation, "Can't acquire texture coordinate attribute");
		GraphicsUtils.checkLocation(mTextureUniformLocation, "Can't acquire texture uniform");
	}

	public float getWidth() {
		return mWidth;
	}

	public void setWidth(final float width) {
		mWidth = width;
		recalculateVertices();
	}

	@Override
	public void start() {
		mCamera = android.hardware.Camera.open();
		try {
			mCamera.setPreviewTexture(mSurfaceTexture);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		mCamera.release();
	}

	public float getHeight() {
		return mHeight;
	}

	public void setHeight(final float height) {
		mHeight = height;
		recalculateVertices();
	}

	public Coordinate3D getPosition() {
		return mPosition;
	}

	public void setPosition(final Coordinate3D position) {
		mPosition = position;
	}

	public Coordinate3D getRotation() {
		return mRotation;
	}

	public void setRotation(final Coordinate3D rotation) {
		mRotation = rotation;
	}

	private int initShadersProgram() {
		final String vertexShaderSource = GraphicsUtils.loadShaderSource(mContext, R.raw.camera_preview_vertex_shader);
		final String fragmentShaderSource =
				GraphicsUtils.loadShaderSource(mContext, R.raw.camera_preview_fragment_shader);

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
		final float halfWidth = mWidth / 2;
		final float halfHeight = mHeight / 2;
		// top left
		mVertices[0] = -halfWidth;
		mVertices[1] = halfHeight;
		mVertices[2] = 0.0f;
		mVertices[3] = 0.0f;
		mVertices[4] = 0.0f;
		// bottom left
		mVertices[5] = -halfWidth;
		mVertices[6] = -halfHeight;
		mVertices[7] = 0.0f;
		mVertices[8] = 0.0f;
		mVertices[9] = 1.0f;
		// bottom right
		mVertices[10] = halfWidth;
		mVertices[11] = -halfHeight;
		mVertices[12] = 0.0f;
		mVertices[13] = 1.0f;
		mVertices[14] = 1.0f;
		// top right
		mVertices[15] = halfWidth;
		mVertices[16] = halfHeight;
		mVertices[17] = 0.0f;
		mVertices[18] = 1.0f;
		mVertices[19] = 0.0f;
	}
}
