package org.ilapin.araltimeter;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class GlView extends GLSurfaceView {

	private final float mTouchSlop;
	private final float mDisplayPixelDensity;

	private State mState = State.IDLE;
	private float mLastY;

	private Controller mController;

	public GlView(final Context context) {
		this(context, null);
	}

	public GlView(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mDisplayPixelDensity = context.getResources().getDisplayMetrics().density;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		switch (mState) {
			case IDLE:
				processIdleEven(event);
				break;

			case SCROLLING:
				processScrollingEvent(event);
				break;
		}

		return true;
	}

	public void setController(final Controller controller) {
		mController = controller;
	}

	private void processScrollingEvent(final MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				if (mController != null) {
					final float eventY = event.getY();
					mController.moveAlongZ((mLastY - eventY) / mDisplayPixelDensity/ 100);
					mLastY = eventY;
				}
				break;

			default:
				mState = State.IDLE;
		}
	}

	private void processIdleEven(final MotionEvent event) {
		final float eventY = event.getY();

		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastY = eventY;
				break;

			case MotionEvent.ACTION_MOVE:
				if (Math.abs(mLastY - eventY) >= mTouchSlop) {
					mState = State.SCROLLING;
					mLastY = eventY;
				}
				break;
		}
	}

	public enum State {
		IDLE, SCROLLING
	}
}
