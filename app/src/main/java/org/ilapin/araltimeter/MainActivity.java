package org.ilapin.araltimeter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.ilapin.araltimeter.graphics.Camera;
import org.ilapin.araltimeter.math.Coordinate3D;

public class MainActivity extends AppCompatActivity {

	private RawCompass mRawCompass;
	private AverageCompass mAverageCompass;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		final GlView glView = (GlView) findViewById(R.id.view_gl);
		glView.setEGLContextClientVersion(2);
		final GlRenderer renderer = new GlRenderer();
		glView.setRenderer(renderer);

		mRawCompass = new RawCompass(this);
		mAverageCompass = new AverageCompass(this);
		final CompassScene scene = new CompassScene(this, mRawCompass, mAverageCompass);
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
		mAverageCompass.start();
	}

	@Override
	protected void onPause() {
		super.onPause();

		mRawCompass.stop();
		mAverageCompass.stop();
	}
}
