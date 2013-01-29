package com.anbrul.commonfunction.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class PageIndicatorView extends View {
	private static final String TAG = "PageIndicatorView";

	private static final int DEFAULT_COUNT = 5;
	private static final int DEFAULT_INDEX = 2;
	private static final int DEFAULT_SPAN = 5;

	private Bitmap mBitmapNormal;
	private Bitmap mBitmapCurrent;
	private int totalCount = DEFAULT_COUNT;
	private int currentIndex = DEFAULT_INDEX;
	private int span = DEFAULT_SPAN;

	public PageIndicatorView(Context context) {
		this(context, null);
	}

	public PageIndicatorView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PageIndicatorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}
	
	public void setResource(int resIdNormal, int resIdCurrent){
		mBitmapNormal = BitmapFactory.decodeResource(getResources(),	resIdNormal);
		mBitmapCurrent = BitmapFactory.decodeResource(getResources(),	resIdCurrent);
		if(getParent() != null){
//			getParent().requestLayout();
		}
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(mBitmapNormal != null && mBitmapCurrent != null){
			for (int i = 0; i < totalCount; i++) {
				int x = i * (span + mBitmapCurrent.getWidth());
				if (i == currentIndex) {
					canvas.drawBitmap(mBitmapCurrent, x, 0, null);
				} else {
					canvas.drawBitmap(mBitmapNormal, x, 0, null);
				}
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width = 0;
		int height = 0;
		
		if (mBitmapCurrent == null || mBitmapNormal == null){
			setMeasuredDimension(width, height);
			return;
		}

		if (widthMode != MeasureSpec.AT_MOST) {
			width = widthSize;
		} else {
			width = mBitmapCurrent.getWidth() * totalCount + span
					* (totalCount - 1);
		}

		if (heightMode != MeasureSpec.AT_MOST) {
			height = heightSize;
		} else {
			height = mBitmapCurrent.getHeight();
		}

		setMeasuredDimension(width, height);
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
		invalidate();
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
		invalidate();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if(mBitmapCurrent != null && !mBitmapCurrent.isRecycled()){
			mBitmapCurrent.recycle();
		}
		
		if(mBitmapNormal != null && !mBitmapNormal.isRecycled()){
			mBitmapNormal.recycle();
		}
	}
}
