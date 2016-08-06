package org.ilapin.araltimeter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import org.ilapin.araltimeter.graphics.Camera;
import org.ilapin.araltimeter.graphics.Color;
import org.ilapin.araltimeter.graphics.GraphicsUtils;
import org.ilapin.araltimeter.graphics.Renderable;
import org.ilapin.araltimeter.graphics.WithShaders;
import org.ilapin.araltimeter.math.Coordinate3D;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class WireframeRectangle implements Renderable, WithShaders {

	private static final int NUMBER_OF_VERTICES = 4;
	private static final int NUMBER_OF_VERTEX_DIMENSIONS = 3;
	private static final int NUMBER_OF_COLOR_COMPONENTS = 4;

	private float mWidth;
	private float mHeight;
	private Color mColor;
	private Coordinate3D mPosition;
	private Coordinate3D mRotation;

	private final Context mContext;
	private final Scene mScene;

	private int mShaderProgramLocation;
	private int mVertexBufferLocation;
	private int mIndexBufferLocation;
	private int mPositionAttributeLocation;
	private int mColorUniformLocation;
	private int mProjectionUniformLocation;
	private int mModelViewUniformLocation;

	private final float[] mModelViewMatrix = new float[GraphicsUtils.NUMBER_OF_MATRIX_ELEMENTS];
	private final FloatBuffer mModelViewMatrixBuffer;
	private final FloatBuffer mProjectionMatrixBuffer;

	private final float[] mVertices = new float[NUMBER_OF_VERTICES * NUMBER_OF_VERTEX_DIMENSIONS];
	private final int[] mIndices = new int[] {
			0, 1,
			1, 2,
			2, 3,
			3, 0
	};

	private final float[] mColorData = new float[NUMBER_OF_COLOR_COMPONENTS];

	public WireframeRectangle(final Context context, final Scene scene, final float width, final float height,
							  final Color color) {
		mContext = context;
		mScene = scene;

		mWidth = width;
		mHeight = height;
		mColor = color;
		mPosition = new Coordinate3D();
		mRotation = new Coordinate3D();

		recalculateVertices();

		mModelViewMatrixBuffer = GraphicsUtils.createFloatBuffer(GraphicsUtils.NUMBER_OF_MATRIX_ELEMENTS);
		mProjectionMatrixBuffer = GraphicsUtils.createFloatBuffer(GraphicsUtils.NUMBER_OF_MATRIX_ELEMENTS);
	}

	@Override
	public void initShaders() {
		mShaderProgramLocation = initShadersProgram();

		mVertexBufferLocation = GraphicsUtils.initFloatBufferObject(GLES20.GL_ARRAY_BUFFER, mVertices);
		mIndexBufferLocation = GraphicsUtils.initIntBufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndices);

		mPositionAttributeLocation = GLES20.glGetAttribLocation(mShaderProgramLocation, "position");
		mColorUniformLocation = GLES20.glGetUniformLocation(mShaderProgramLocation, "color");
		mProjectionUniformLocation = GLES20.glGetUniformLocation(mShaderProgramLocation, "projection");
		mModelViewUniformLocation = GLES20.glGetUniformLocation(mShaderProgramLocation, "modelView");

		GraphicsUtils.checkLocation(mPositionAttributeLocation, "Can't acquire position attribute");
		GraphicsUtils.checkLocation(mColorUniformLocation, "Can't acquire color uniform");
		GraphicsUtils.checkLocation(mProjectionUniformLocation, "Can't acquire projection uniform");
		GraphicsUtils.checkLocation(mModelViewUniformLocation, "Can't acquire modelView uniform");
	}

	@Override
	public void render() {
		final Camera camera = mScene.getActiveCamera();
		final Coordinate3D cameraPosition = camera.getPosition();

		GLES20.glUseProgram(mShaderProgramLocation);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexBufferLocation);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferLocation);

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

		mColorData[0] = mColor.getRed();
		mColorData[1] = mColor.getGreen();
		mColorData[2] = mColor.getBlue();
		mColorData[3] = mColor.getAlpha();
		GLES20.glUniform4fv(mColorUniformLocation, 1, mColorData, 0);

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

		GLES20.glDrawElements(GLES20.GL_LINES, mIndices.length, GLES20.GL_UNSIGNED_INT, 0);

		GLES20.glDisableVertexAttribArray(mPositionAttributeLocation);
	}

	public float getWidth() {
		return mWidth;
	}

	public void setWidth(final float width) {
		mWidth = width;
		recalculateVertices();
	}

	public float getHeight() {
		return mHeight;
	}

	public void setHeight(final float height) {
		mHeight = height;
		recalculateVertices();
	}

	public Color getColor() {
		return mColor;
	}

	public void setColor(final Color color) {
		mColor = color;
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
				GraphicsUtils.loadShaderSource(mContext, R.raw.wireframe_rectangle_vertex_shader);
		final String fragmentShaderSource =
				GraphicsUtils.loadShaderSource(mContext, R.raw.wireframe_rectangle_fragment_shader);

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
		final float halfWidth = mWidth / 2;
		final float halfHeight = mHeight / 2;
		// top left
		mVertices[0] = -0.5f * halfWidth;
		mVertices[1] = 0.5f * halfHeight;
		mVertices[2] = 0.0f;
		// bottom left
		mVertices[3] = -0.5f * halfWidth;
		mVertices[4] = -0.5f * halfHeight;
		mVertices[5] = 0.0f;
		// bottom right
		mVertices[6] = 0.5f * halfWidth;
		mVertices[7] = -0.5f * halfHeight;
		mVertices[8] = 0.0f;
		// top right
		mVertices[9] = 0.5f * halfWidth;
		mVertices[10] = 0.5f * halfHeight;
		mVertices[11] = 0.0f;
	}
}
