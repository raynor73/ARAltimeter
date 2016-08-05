package org.ilapin.araltimeter.graphics;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import org.ilapin.araltimeter.R;
import org.ilapin.araltimeter.math.Coordinate3D;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class WireframeCompassArrow implements Renderable, WithShaders {

	private static final int NUMBER_OF_VERTICES = 4;
	private static final int NUMBER_OF_VERTEX_DIMENSIONS = 3;
	private static final int NUMBER_OF_COLOR_COMPONENTS = 4;
	private static final float WIDTH = 0.25f; // Percentage of length

	private float mLength;
	private final Color mNorthColor = new Color(0, 0, 1, 1);
	private final Color mSouthColor = new Color(1, 0, 0, 1);
	private Coordinate3D mPosition;
	private Coordinate3D mRotation;

	private final Context mContext;
	private final Scene mScene;

	private int mShaderProgramLocation;
	private int mVertexBufferLocation;
	private int mNorthIndexBufferLocation;
	private int mSouthIndexBufferLocation;
	private int mPositionAttributeLocation;
	private int mColorUniformLocation;
	private int mProjectionUniformLocation;
	private int mModelViewUniformLocation;

	private final float[] mModelViewMatrix = new float[GraphicsUtils.NUMBER_OF_MATRIX_ELEMENTS];
	private final FloatBuffer mModelViewMatrixBuffer;
	private final FloatBuffer mProjectionMatrixBuffer;

	private final float[] mVertices = new float[NUMBER_OF_VERTICES * NUMBER_OF_VERTEX_DIMENSIONS];
	private final int[] mNorthIndices = new int[] {
			3, 0,
			0, 1
	};
	private final int[] mSouthIndices = new int[] {
			1, 2,
			2, 3
	};

	private final float[] mColorData = new float[NUMBER_OF_COLOR_COMPONENTS];

	public WireframeCompassArrow(final Context context, final Scene scene, final float length) {
		mContext = context;
		mScene = scene;

		mLength = length;
		mPosition = new Coordinate3D();
		mRotation = new Coordinate3D();

		recalculateVertices();

		mModelViewMatrixBuffer = GraphicsUtils.createFloatBuffer(GraphicsUtils.NUMBER_OF_MATRIX_ELEMENTS);
		mProjectionMatrixBuffer = GraphicsUtils.createFloatBuffer(GraphicsUtils.NUMBER_OF_MATRIX_ELEMENTS);
	}

	@Override
	public void render() {
		final Camera camera = mScene.getActiveCamera();
		final Coordinate3D cameraPosition = camera.getPosition();

		GLES20.glUseProgram(mShaderProgramLocation);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexBufferLocation);

		GLES20.glEnableVertexAttribArray(mPositionAttributeLocation);

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
				0,
				0
		);
		GLES20.glUniformMatrix4fv(mProjectionUniformLocation, 1, false, mProjectionMatrixBuffer);
		GLES20.glUniformMatrix4fv(mModelViewUniformLocation, 1, false, mModelViewMatrixBuffer);

		mColorData[0] = mNorthColor.getRed();
		mColorData[1] = mNorthColor.getGreen();
		mColorData[2] = mNorthColor.getBlue();
		mColorData[3] = mNorthColor.getAlpha();
		GLES20.glUniform4fv(mColorUniformLocation, 1, mColorData, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mNorthIndexBufferLocation);
		GLES20.glDrawElements(GLES20.GL_LINES, mNorthIndices.length, GLES20.GL_UNSIGNED_INT, 0);

		mColorData[0] = mSouthColor.getRed();
		mColorData[1] = mSouthColor.getGreen();
		mColorData[2] = mSouthColor.getBlue();
		mColorData[3] = mSouthColor.getAlpha();
		GLES20.glUniform4fv(mColorUniformLocation, 1, mColorData, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mSouthIndexBufferLocation);
		GLES20.glDrawElements(GLES20.GL_LINES, mSouthIndices.length, GLES20.GL_UNSIGNED_INT, 0);

		GLES20.glDisableVertexAttribArray(mPositionAttributeLocation);
	}

	@Override
	public void initShaders() {
		mShaderProgramLocation = initShadersProgram();

		mVertexBufferLocation = GraphicsUtils.initFloatBufferObject(GLES20.GL_ARRAY_BUFFER, mVertices);
		mNorthIndexBufferLocation = GraphicsUtils.initIntBufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, mNorthIndices);
		mSouthIndexBufferLocation = GraphicsUtils.initIntBufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, mSouthIndices);

		mPositionAttributeLocation = GLES20.glGetAttribLocation(mShaderProgramLocation, "position");
		mColorUniformLocation = GLES20.glGetUniformLocation(mShaderProgramLocation, "color");
		mProjectionUniformLocation = GLES20.glGetUniformLocation(mShaderProgramLocation, "projection");
		mModelViewUniformLocation = GLES20.glGetUniformLocation(mShaderProgramLocation, "modelView");

		GraphicsUtils.checkLocation(mPositionAttributeLocation, "Can't acquire position attribute");
		GraphicsUtils.checkLocation(mColorUniformLocation, "Can't acquire color uniform");
		GraphicsUtils.checkLocation(mProjectionUniformLocation, "Can't acquire projection uniform");
		GraphicsUtils.checkLocation(mModelViewUniformLocation, "Can't acquire modelView uniform");
	}

	public float getLength() {
		return mLength;
	}

	public void setLength(final float length) {
		mLength = length;
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
		final String vertexShaderSource =
				GraphicsUtils.loadShaderSource(mContext, R.raw.wireframe_compass_arrow_vertex_shader);
		final String fragmentShaderSource =
				GraphicsUtils.loadShaderSource(mContext, R.raw.wireframe_compass_arrow_fragment_shader);

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

	private void recalculateVertices() {
		final float halfLength = mLength / 2;
		final float halfWidth = mLength * WIDTH / 2;
		// top left
		mVertices[0] = 0.0f;
		mVertices[1] = halfLength;
		mVertices[2] = 0.0f;
		// bottom left
		mVertices[3] = halfWidth;
		mVertices[4] = 0.0f;
		mVertices[5] = 0.0f;
		// bottom right
		mVertices[6] = 0.0f;
		mVertices[7] = -halfLength;
		mVertices[8] = 0.0f;
		// top right
		mVertices[9] = -halfWidth;
		mVertices[10] = 0.0f;
		mVertices[11] = 0.0f;
	}
}
