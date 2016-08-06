package org.ilapin.araltimeter;

import android.content.Context;
import android.hardware.SensorManager;

import org.ilapin.araltimeter.graphics.Renderable;
import org.ilapin.araltimeter.graphics.WithShaders;
import org.ilapin.araltimeter.math.Coordinate3D;
import org.ilapin.araltimeter.sensors.RawCompassSensor;
import org.ilapin.araltimeter.sensors.Sensor;

public class RawCompass implements Renderable, WithShaders, Sensor {

	private final RawCompassSensor mRawCompassSensor;
	private final WireframeCompassArrow mCompassArrow;

	public RawCompass(final Context context) {
		mRawCompassSensor = new RawCompassSensor((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
		mCompassArrow = new WireframeCompassArrow(context, 1);
	}

	@Override
	public void render(final Scene scene) {
		final Coordinate3D arrowRotation = mCompassArrow.getRotation();
		arrowRotation.setX((float) Math.toDegrees(mRawCompassSensor.getPitch()) - 90);
		arrowRotation.setY((float) Math.toDegrees(-mRawCompassSensor.getRoll()));
		arrowRotation.setZ((float) Math.toDegrees(mRawCompassSensor.getAzimuth()));

		mCompassArrow.render(scene);
	}

	@Override
	public void initShaders() {
		mCompassArrow.initShaders();
	}

	@Override
	public void start() {
		mRawCompassSensor.start();
	}

	@Override
	public void stop() {
		mRawCompassSensor.stop();
	}
}
