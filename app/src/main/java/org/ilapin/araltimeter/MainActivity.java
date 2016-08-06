package org.ilapin.araltimeter;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.ilapin.araltimeter.graphics.Camera;
import org.ilapin.araltimeter.math.Coordinate3D;

public class MainActivity extends AppCompatActivity {

	private RawCompass mRawCompass;
	private final Coordinate3D mRawCompassAngles = new Coordinate3D();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRawCompass = new RawCompass((SensorManager) getSystemService(SENSOR_SERVICE));

		final GlView glView = (GlView) findViewById(R.id.view_gl);
		glView.setEGLContextClientVersion(2);
		final GlRenderer renderer = new GlRenderer();
		glView.setRenderer(renderer);

		final Scene scene = new Scene(this, new Model() {

			@Override
			public Coordinate3D getRawCompassArrowRotation() {
				mRawCompassAngles.setX((float) Math.toDegrees(mRawCompass.getPitch()) - 90);
				mRawCompassAngles.setY((float) Math.toDegrees(-mRawCompass.getRoll()));
				mRawCompassAngles.setZ((float) Math.toDegrees(mRawCompass.getAzimuth()));
				return mRawCompassAngles;
			}
		});
		glView.setController(new Controller() {

			private final Camera mCamera = scene.getActiveCamera();

			@Override
			public void moveAlongZ(final float amount) {
				final Coordinate3D cameraPosition = mCamera.getPosition();
				cameraPosition.setZ(cameraPosition.getZ() + amount);
			}
		});

		renderer.setScene(scene);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mRawCompass.start();
	}

	@Override
	protected void onPause() {
		super.onPause();

		mRawCompass.stop();
	}
}
