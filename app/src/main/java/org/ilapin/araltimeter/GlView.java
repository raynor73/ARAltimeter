package org.ilapin.araltimeter;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class GlView extends GLSurfaceView {

	public GlView(final Context context) {
		super(context);
	}

	public GlView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		return super.onTouchEvent(event);
	}
}
