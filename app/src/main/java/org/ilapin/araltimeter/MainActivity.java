package org.ilapin.araltimeter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final GlView glView = (GlView) findViewById(R.id.view_gl);
		glView.setEGLContextClientVersion(2);
		glView.setRenderer(new GlRenderer());
	}
}
