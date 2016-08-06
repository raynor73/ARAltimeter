package org.ilapin.araltimeter.sensors;

import android.hardware.SensorManager;
import android.opengl.Matrix;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class AverageCompassSensor extends RawCompassSensor {
	private static final int STATISTICS_WINDOW_SIZE = 100;

	private final DescriptiveStatistics mAzimuthYStatistics = new DescriptiveStatistics(STATISTICS_WINDOW_SIZE);
	private final DescriptiveStatistics mAzimuthXStatistics = new DescriptiveStatistics(STATISTICS_WINDOW_SIZE);

	private final DescriptiveStatistics mPitchYStatistics = new DescriptiveStatistics(STATISTICS_WINDOW_SIZE);
	private final DescriptiveStatistics mPitchXStatistics = new DescriptiveStatistics(STATISTICS_WINDOW_SIZE);

	private final DescriptiveStatistics mRollYStatistics = new DescriptiveStatistics(STATISTICS_WINDOW_SIZE);
	private final DescriptiveStatistics mRollXStatistics = new DescriptiveStatistics(STATISTICS_WINDOW_SIZE);

	private final float[] mRotationMatrix = new float[16];

	private final float[] mAzimuthRotatedVector = new float[4];
	private final float[] mPitchRotatedVector = new float[4];
	private final float[] mRollRotatedVector = new float[4];

	private final float[] mGaugeVector = new float[] {1, 0, 0, 1};

	public AverageCompassSensor(final SensorManager sensorManager) {
		super(sensorManager);
	}

	@Override
	protected void onRawAnglesCalculated(final float azimuth, final float pitch, final float roll) {
		rotateVector(mAzimuthRotatedVector, mGaugeVector, azimuth, 0, 0, 1);
		rotateVector(mPitchRotatedVector, mGaugeVector, pitch, 0, 0, 1);
		rotateVector(mRollRotatedVector, mGaugeVector, roll, 0, 0, 1);

		addValue(mAzimuthXStatistics, mAzimuthYStatistics, mAzimuthRotatedVector);
		addValue(mPitchXStatistics, mPitchYStatistics, mPitchRotatedVector);
		addValue(mRollXStatistics, mRollYStatistics, mRollRotatedVector);

		setCalculatedAngles(
				getMean(mAzimuthXStatistics, mAzimuthYStatistics),
				getMean(mPitchXStatistics, mPitchYStatistics),
				getMean(mRollXStatistics, mRollYStatistics)
		);
	}

	private float getMean(
			final DescriptiveStatistics statisticsX,
			final DescriptiveStatistics statisticsY) {
		return (float) Math.atan2(statisticsY.getMean(), statisticsX.getMean());
	}

	private void addValue(
			final DescriptiveStatistics statisticsX,
			final DescriptiveStatistics statisticsY,
			final float[] vector) {
		statisticsX.addValue(vector[0]);
		statisticsY.addValue(vector[1]);
	}

	private void rotateVector(final float[] rotatedVector, final float[] vector, final float angleRad,
										 final float x, final float y, final float z) {
		Matrix.setIdentityM(mRotationMatrix, 0);
		Matrix.rotateM(mRotationMatrix, 0, (float) Math.toDegrees(angleRad), x, y, z);
		Matrix.multiplyMV(rotatedVector, 0, mRotationMatrix, 0, vector, 0);
	}
}
