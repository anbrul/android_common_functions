package com.anbrul.commonfunction.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Scroller;

/**
 * The workspace is a wide area to hold some views, and can slide between the views
 * A workspace is meant to be used with a fixed width only.
 */
public class SlipPage extends ViewGroup{
    private static final int INVALID_PAGE = -1;
    private static final String TAG = "SlipPage";

    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;
    
    /**
     * The velocity at which a fling gesture will cause us to snap to the next screen
     */
    private static final int SNAP_VELOCITY = 1000;

    private int mDefaultPage = 1;

    private boolean mFirstLayout = true;

    private int mCurrentScreen;
    private int mNextScreen = INVALID_PAGE;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    
    private float mLastMotionX;
    private float mLastMotionY;

    private int mTouchState = TOUCH_STATE_REST;

    private boolean mAllowLongPress;
    private boolean mLocked;

    private int mTouchSlop;
    private int mMaximumVelocity;

    final Rect mDrawerBounds = new Rect();
    final Rect mClipBounds = new Rect();
    int mDrawerContentHeight;
    int mDrawerContentWidth;
    
    private int lastScreen;
	private boolean mLoopLayout;
	
	/**This is the index of the current index of data for left most side View*/
	private int mCurrentDataIndex;
	
	/**Save the children view for loop mode, useless now*/
	private ArrayList<View> mViewsForLoopMode;
    
    private SlipPageAdapter mAdapter;
    private OnPageSwitchListener mOnPageSwitchListener;

    public SlipPage(Context context) {
		super(context);
	}

	/**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public SlipPage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     * @param defStyle Unused.
     */
    public SlipPage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLoopLayout = false;
        initWorkspace();
    }

    /**
     * Initializes various states for this workspace.
     */
    private void initWorkspace() {
        mScroller = new Scroller(getContext());
        mCurrentScreen = 0;

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
//        Log.d(TAG, "addView() child " + index + " = " + child );
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child) {
//        Log.d(TAG, "addView() child = " + child );
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
//        Log.d(TAG, "addView() child " + index + " = " + child );
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
//        Log.d(TAG, "addView() child = " + child );
        super.addView(child, width, height);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        Log.d(TAG, "addView() child = " + child );
        super.addView(child, params);
    }

    boolean isDefaultScreenShowing() {
        return mCurrentScreen == mDefaultPage;
    }

    /**
     * Returns the index of the currently displayed screen.
     *
     * @return The index of the currently displayed screen.
     */
    int getCurrentScreen() {
        return mCurrentScreen;
    }

    /**
     * Sets the current screen.
     *
     * @param currentScreen
     */
    void setCurrentScreen(int currentScreen) {
        mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
        scrollTo(mCurrentScreen * getWidth(), 0);
        invalidate();
    }
    
    @Override
    public void computeScroll() {
//	Log.d(TAG, "+ computeScroll(), mCurrentScreen = " + mCurrentScreen + "; mNextScreen = " + mNextScreen);
        if (mScroller.computeScrollOffset()) {
            int scrollX = mScroller.getCurrX();            
            int scrollY = mScroller.getCurrY();
            scrollTo(scrollX, scrollY);
//            Log.d(TAG, "mScrollX = " + getScrollX() + ", mScrollY = " + getScrollY());
            postInvalidate();
        } else if (mNextScreen != INVALID_PAGE) {
        	int scrollX = mScroller.getCurrX();            
            int scrollY = mScroller.getCurrY();
//            Log.d(TAG, "Scroll finished, mScrollX = " + getScrollX() + ":" + scrollX + ", mScrollY = " + getScrollY() + ":" + scrollY);
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            mNextScreen = INVALID_PAGE;
//            Log.d(TAG, "computeScroll() Scrool finished! curren = " + mCurrentScreen);
            if(lastScreen != mCurrentScreen){
//            	Log.d(TAG, "computeScroll() screen changed! last:" + lastScreen + "; current:" + mCurrentScreen);
            	lastScreen = mCurrentScreen;
            	updateChildren();
            }
            
            if(!mLoopLayout && mOnPageSwitchListener!= null){
            	mOnPageSwitchListener.onPageSwitched(mCurrentScreen);
            }
        }
//        Log.d(TAG, "- computeScroll()");
    }
    
	@Override
    protected void dispatchDraw(Canvas canvas) {
        boolean restore = false;

        // ViewGroup.dispatchDraw() supports many features we don't need:
        // clip to padding, layout animation, animation listener, disappearing
        // children, etc. The following implementation attempts to fast-track
        // the drawing dispatch by drawing only what we know needs to be drawn.

        boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mNextScreen == INVALID_PAGE;
        // If we are not scrolling or flinging, draw only the current screen
        if (fastDraw) {
            drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
        } else {
            final long drawingTime = getDrawingTime();
            // If we are flinging, draw only the current screen and the target screen
            if (mNextScreen >= 0 && mNextScreen < getChildCount() &&
                    Math.abs(mCurrentScreen - mNextScreen) == 1) {
                drawChild(canvas, getChildAt(mCurrentScreen), drawingTime);
                drawChild(canvas, getChildAt(mNextScreen), drawingTime);
            } else {
                // If we are scrolling, draw all of our children
                final int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    drawChild(canvas, getChildAt(i), drawingTime);
                }
            }
        }

        if (restore) {
            canvas.restore();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        // The children are given the same width and height as the workspace
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        if (mFirstLayout) {
            scrollTo(mCurrentScreen * width, 0);
            mFirstLayout = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    	layoutChildrenInNormalMode();
    }
    
    private void layoutChildrenInNormalMode(){
    	int childLeft = 0;
        final int count = getChildCount();
        Log.d(TAG, "onLayout(), child count:" + count);
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();
                child.layout(childLeft, 0, childLeft + childWidth, childHeight);
                childLeft += childWidth;
            }
        }
    }
    
    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        int screen = indexOfChild(child);
        if (screen != mCurrentScreen || !mScroller.isFinished()) {
            snapToScreen(screen);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (direction == View.FOCUS_LEFT) {
            if (getCurrentScreen() > 0) {
                snapToScreen(getCurrentScreen() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentScreen() < getChildCount() - 1) {
                snapToScreen(getCurrentScreen() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
    }
    
    void enableChildrenCache() {
        setChildrenDrawnWithCacheEnabled(true);
        setChildrenDrawingCacheEnabled(true);
    }
    
    void clearChildrenCache() {
    	setChildrenDrawnWithCacheEnabled(false);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mLocked) {
            return true;
        }

        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionX is set to the y value
                 * of the down event.
                 */
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > touchSlop;
                boolean yMoved = yDiff > touchSlop;
                
                if (xMoved || yMoved) {
                    
                    if (xMoved) {
                        // Scroll if the user moved far enough along the X axis
                        mTouchState = TOUCH_STATE_SCROLLING;
                        enableChildrenCache();
                    }
                    // Either way, cancel any pending longpress
                    if (mAllowLongPress) {
                        mAllowLongPress = false;
                        // Try canceling the long press. It could also have been scheduled
                        // by a distant descendant, so use the mAllowLongPress flag to block
                        // everything
                        final View currentScreen = getChildAt(mCurrentScreen);
                        currentScreen.cancelLongPress();
                    }
                }
                break;

            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;
                mAllowLongPress = true;

                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't.  mScroller.isFinished should be false when
                 * being flinged.
                 */
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // Release the drag
                clearChildrenCache();
                mTouchState = TOUCH_STATE_REST;
                mAllowLongPress = false;
                break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        boolean ret = (mTouchState != TOUCH_STATE_REST);
        Log.d(TAG, "Intercept touch event return:" + ret);
        return ret;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//	Log.d(TAG, "+ onTouchEvent()");
        if (mLocked) {
            return true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();
        final float x = ev.getX();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            /*
             * If being flinged and user touches, stop the fling. isFinished
             * will be false if being flinged.
             */
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            // Remember where the motion event started
            mLastMotionX = x;
            break;
        case MotionEvent.ACTION_MOVE:
//            Log.d(TAG, "+ onTouchEvent()----ACTION_MOVE:0");
            
            mTouchState = TOUCH_STATE_SCROLLING;
            if (mTouchState == TOUCH_STATE_SCROLLING) {
//            	Log.d(TAG, "+ onTouchEvent()----ACTION_MOVE:1");
                // Scroll to follow the motion event
                final int deltaX = (int) (mLastMotionX - x);
                mLastMotionX = x;

                if (deltaX < 0) {
//                	Log.d(TAG, "+ onTouchEvent()----ACTION_MOVE:2");
                    if (getScrollX() > 0) {
                    	Log.d(TAG, "+ onTouchEvent()----ACTION_MOVE:3");
                    	scrollBy(Math.max(-getScrollX(), deltaX), 0);
                    }
                } else if (deltaX > 0) {
//                	Log.d(TAG, "+ onTouchEvent()----ACTION_MOVE:4");
                    final int availableToScroll = getChildAt(getChildCount() - 1).getRight() -
                    getScrollX() - getWidth();
                    
                    if (availableToScroll > 0) {
//                    	Log.d(TAG, "+ onTouchEvent()----ACTION_MOVE:5");
                        scrollBy(Math.min(availableToScroll, deltaX), 0);
                    }
                }
            }
//            Log.d(TAG, "- onTouchEvent()----ACTION_MOVE:");
            break;
        case MotionEvent.ACTION_UP:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();

                if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                    // Fling hard enough to move left
                    snapToScreen(mCurrentScreen - 1);
                } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
                    // Fling hard enough to move right
                    snapToScreen(mCurrentScreen + 1);
                } else {
                    snapToDestination();
                }
                
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            mTouchState = TOUCH_STATE_REST;
            break;
        case MotionEvent.ACTION_CANCEL:
            mTouchState = TOUCH_STATE_REST;
        }
        
        return true;
    }

	private void snapToDestination() {
		final int screenWidth = getWidth();
		final int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
		snapToScreen(whichScreen);
	}

    void snapToScreen(int whichScreen) {
        if (!mScroller.isFinished()){
        	return;
        }

        enableChildrenCache();

        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        boolean changingScreens = whichScreen != mCurrentScreen;
        
        mNextScreen = whichScreen;
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && changingScreens && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int newX = whichScreen * getWidth();
        final int delta = newX - getScrollX();
        mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentScreen = mCurrentScreen;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.currentScreen != -1) {
            mCurrentScreen = savedState.currentScreen;
        }
    }

    public void scrollLeft() {
        if (mNextScreen == INVALID_PAGE && mCurrentScreen > 0 && mScroller.isFinished()) {
            snapToScreen(mCurrentScreen - 1);
        }
    }
    
	private void updateChildren() {
		if(!mLoopLayout){
			return;
		}
		
		if(mCurrentScreen == 1){
			setCurrentScreen(1);
			return;
		}
		
		final int size = mAdapter.getDataSize();
		switch (mCurrentScreen) {
		case 0:
			View rightMostChild = getChildAt(2);
			removeView(rightMostChild);
			if(size > 0){
				mAdapter.loadData(rightMostChild, (mCurrentDataIndex + size - 1) % size);
				mCurrentDataIndex = (mCurrentDataIndex + size - 1) % size;
			}
			addView(rightMostChild, 0);
			requestLayout();
			break;
		case 1:
			break;
		case 2:
			View leftMostChild = getChildAt(0);
			removeView(leftMostChild);
			if(size > 0){
				mAdapter.loadData(leftMostChild, (mCurrentDataIndex + 3) % size);
				mCurrentDataIndex = (mCurrentDataIndex + 1) % size;
			}
			addView(leftMostChild, -1);
			requestLayout();
			break;
		}
		
		mNextScreen = 1;
        final int delta = getWidth() - getScrollX();
		mScroller.startScroll(getScrollX(), 0, delta, 0, 1);
        mCurrentScreen = 1;
        
        notifyPageChange();
	}

	public void scrollRight() {
        if (mNextScreen == INVALID_PAGE && mCurrentScreen < getChildCount() -1 &&
                mScroller.isFinished()) {
            snapToScreen(mCurrentScreen + 1);
        }
    }

    public int getScreenForView(View v) {
        int result = -1;
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getChildAt(i)) {
                    return i;
                }
            }
        }
        return result;
    }

    /**
     * Unlocks the SlidingDrawer so that touch events are processed.
     *
     * @see #lock()
     */
    public void unlock() {
        mLocked = false;
    }

    /**
     * Locks the SlidingDrawer so that touch events are ignores.
     *
     * @see #unlock()
     */
    public void lock() {
        mLocked = true;
    }
    
    /**
     * @return True is long presses are still allowed for the current touch
     */
    public boolean allowLongPress() {
        return mAllowLongPress;
    }
    
    /**
     * Set true to allow long-press events to be triggered, usually checked by
     * {@link Launcher} to accept or block dpad-initiated long-presses.
     */
    public void setAllowLongPress(boolean allowLongPress) {
        mAllowLongPress = allowLongPress;
    }
    
    public void setOnPageSwitchListener(OnPageSwitchListener listener){
    	mOnPageSwitchListener = listener;
    }

    void moveToDefaultScreen() {
    	setCurrentScreen(mDefaultPage);
        getChildAt(mDefaultPage).requestFocus();
    }

    public static class SavedState extends BaseSavedState {
        int currentScreen = -1;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentScreen = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentScreen);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

	public void enableLoopMode(SlipPageAdapter adapter) {
		if(adapter == null){
			throw new IllegalArgumentException("Adapter can not be null!");
		}
		
		mAdapter = adapter;
		
		if (mViewsForLoopMode == null){
			mViewsForLoopMode = new ArrayList<View>();
		}else{
			mViewsForLoopMode.clear();
		}

		//Clear All children
		removeAllViews();
		
		// Add three view for slip
		int size = mAdapter.getDataSize();
		mCurrentDataIndex = size - 1;
		for(int i = -1; i < 2; i++){
			View view = mAdapter.createView();
			if(view == null){
				throw new RuntimeException("createView() returned null!");
			}
			
			if(size > 0){
				mAdapter.loadData(view, (i + size) % size);
			}
			mViewsForLoopMode.add(view);
			addView(view);
		}
		
		mLoopLayout = true;
		setCurrentScreen(1);
		updateChildren();
		
		notifyPageChange();
	}
	
	/**
	 * Notify page change in loop mode
	 */
	private void notifyPageChange(){
		int size = mAdapter.getDataSize();
		if(mAdapter!= null && size > 0){
			mAdapter.onPageSwitch((mCurrentDataIndex + 1) % size);
		}
	}
	
	/**
	 * Use for {@link #SlipPage()}in loop mode
	 * @author MikeWu
	 */
	public static abstract class SlipPageAdapter{
		/**
		 * Create View for loop mode, must return a new instance every time
		 * @return
		 */
		protected abstract View createView();
		
		/**
		 * Return the size of your data
		 * @return size of your data
		 */
		protected abstract int getDataSize();
		
		/**
		 * Load data to the view
		 * @param view the view returned by {@link #createView()}
		 * @param index the index of the data that we want
		 */
		protected abstract void loadData(View view, int index);
		
		/**
		 * Called when the page changed, override this function if needed
		 * @param index current showing data index
		 */
		protected void onPageSwitch(int index){
		}
	}
	
	public static interface OnPageSwitchListener{
		void onPageSwitched(int index);
	}
}
