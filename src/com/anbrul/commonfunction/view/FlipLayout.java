package com.anbrul.commonfunction.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class FlipLayout extends LinearLayout implements OnGestureListener{

	private static final String TAG = "FlipLayout";
	
	private GestureDetector detector;
	private ViewFlipper viewFlipper;
	private OnFlingListener flingListener;

	public FlipLayout(Context context) {
		super(context);
	}
	
	public FlipLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		detector = new GestureDetector(this);
	}
	
	public void setOnFlingListener(OnFlingListener listener){
		flingListener = listener;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		detector.onTouchEvent(ev);
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		
		if(Math.abs(velocityX) < 800.0f || Math.abs(velocityX) < Math.abs(velocityY)){
			return false;
		}
		
		if (e1.getX() - e2.getX() > 120) {
			if (flingListener != null){
				flingListener.onFlingLeft();
			}
			return true;
		} else if (e1.getX() - e2.getX() < -120) {
			if (flingListener != null){
				flingListener.onFlingRight();
			}
			return true;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	
	public static interface OnFlingListener{
		public abstract boolean onFlingLeft();
		public abstract boolean onFlingRight();
		
	}
}
