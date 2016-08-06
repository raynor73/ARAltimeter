package org.ilapin.araltimeter.sensors;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RawCompassSensor implements Sensor {
	private final SensorManager mSensorManager;

	private float mAzimuth;
	private float mPitch;
	private float mRoll;

	private final float[] mMagneticFiledVector = new float[3];
	private final float[] mAccelerometerVector = new float[3];

	private final float[] mR = new float[9];
	private final float[] mI = new float[9];
	private final float[] mOrientation = new float[3];
	private final float[] mCameraRotation = new float[9];

	private final SensorEventListener mMagneticFieldListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(final SensorEvent sensorEvent) {
			System.arraycopy(sensorEvent.values, 0, mMagneticFiledVector, 0, 3);
			calculateAngles();
		}

		@Override
		public void onAccuracyChanged(final android.hardware.Sensor sensor, final int i) {
			// do nothing
		}
	};

	private final SensorEventListener mAccelerometerListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(final SensorEvent sensorEvent) {
			System.arraycopy(sensorEvent.values, 0, mAccelerometerVector, 0, 3);
			calculateAngles();
		}

		@Override
		public void onAccuracyChanged(final android.hardware.Sensor sensor, final int i) {
			// do nothing
		}
	};

	public RawCompassSensor(final SensorManager sensorManager) {
		mSensorManager = sensorManager;
	}

	public void start() {
		mSensorManager.registerListener(
				mMagneticFieldListener,
				mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_GAME
		);

		mSensorManager.registerListener(
				mAccelerometerListener,
				mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME
		);
	}

	public void stop() {
		mSensorManager.unregisterListener(mMagneticFieldListener);
		mSensorManager.unregisterListener(mAccelerometerListener);
	}

	public float getAzimuth() {
		return mAzimuth;
	}

	public float getPitch() {
		return mPitch;
	}

	public float getRoll() {
		return mRoll;
	}

	private void calculateAngles() {
		if (SensorManager.getRotationMatrix(mR, mI, mAccelerometerVector, mMagneticFiledVector)) {
			SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_X, SensorManager.AXIS_Z, mCameraRotation);
			SensorManager.getOrientation(mCameraRotation, mOrientation);

			onRawAnglesCalculated(mOrientation[0], mOrientation[1], mOrientation[2]);
		}
	}

	protected final void setCalculatedAngles(final float azimuth, final float pitch, final float roll) {
		mAzimuth = azimuth;
		mPitch = pitch;
		mRoll = roll;
	}

	protected void onRawAnglesCalculated(final float azimuth, final float pitch, final float roll) {
		setCalculatedAngles(azimuth, pitch, roll);
	}
}
