package org.ilapin.araltimeter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import org.ilapin.araltimeter.graphics.Camera;
import org.ilapin.araltimeter.math.Coordinate3D;

public class MainActivity extends AppCompatActivity {

	private RawCompass mRawCompass;
	private AverageCompass mAverageCompass;
	private CameraPreview mCameraPreview;

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
		mCameraPreview = new CameraPreview(this);
		final CompassScene scene = new CompassScene(this, mCameraPreview, mRawCompass, mAverageCompass);
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

	private final Handler mHandler = new Handler(Looper.getMainLooper());
	@Override
	protected void onResume() {
		super.onResume();

		mRawCompass.start();
		mHandler.postDelayed(() -> mCameraPreview.start(), 1000);
		mAverageCompass.start();
	}

	@Override
	protected void onPause() {
		super.onPause();

		mRawCompass.stop();
		mCameraPreview.stop();
		mAverageCompass.stop();
	}
}
