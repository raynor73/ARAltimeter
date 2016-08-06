package org.ilapin.araltimeter;

import android.hardware.SensorManager;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class AverageCompass extends RawCompass {
	private static final int STATISTICS_WINDOW_SIZE = 100;

	private final DescriptiveStatistics mAzimuthStatistics = new DescriptiveStatistics(STATISTICS_WINDOW_SIZE);
	private final DescriptiveStatistics mPitchStatistics = new DescriptiveStatistics(STATISTICS_WINDOW_SIZE);
	private final DescriptiveStatistics mRollStatistics = new DescriptiveStatistics(STATISTICS_WINDOW_SIZE);

	public AverageCompass(final SensorManager sensorManager) {
		super(sensorManager);
	}

	@Override
	protected void onRawAnglesCalculated(final float azimuth, final float pitch, final float roll) {
		mAzimuthStatistics.addValue(Math.toDegrees(azimuth));
		mPitchStatistics.addValue(Math.toDegrees(pitch));
		mRollStatistics.addValue(Math.toDegrees(roll));

		setCalculatedAngles(
				(float) mAzimuthStatistics.getMean(),
				(float) mPitchStatistics.getMean(),
				(float) mRollStatistics.getMean()
		);
	}
}
