package com.anbrul.commonfunction.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class IphoneListView extends ExpandableListView implements OnScrollListener {
    private int mOldHeaderState = -1;
    private static final int MAX_ALPHA = 255;

    private IphoneHeaderAdapter mAdapter;

    /**
     * The header view display at the list head
     */
    private View mHeaderView;

    /**
     * Is header view visible
     */
    private boolean mHeaderViewVisible;
    private int mHeaderViewWidth;
    private int mHeaderViewHeight;
    private boolean mUseDefaultHeaderState = true;
    
    //For touch event
    private int mDownX;
    private int mDownY;
    private boolean mHeaderPressed = false;
    
    public IphoneListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public IphoneListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IphoneListView(Context context) {
        super(context);
        init();
    }
    
    @TargetApi(9)
    private void init() {
        super.setOnScrollListener(this);
        setGroupIndicator(null); // Not enable group indicator for this moment

//        if (Build.VERSION.SDK_INT >= 9) {
//            setOverScrollMode(OVER_SCROLL_NEVER);
//        }

//        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//
//            @Override
//            public void onGlobalLayout() {
//                int width = getWidth();
//                if (width > 0) {
//                    setIndicatorBounds(width - 100, width - 10);
//                }
//                getViewTreeObserver().removeGlobalOnLayoutListener(this);
//            }
//        });
    }

    /**
     * Adapter for the customized listview
     */
    public interface IphoneHeaderAdapter {
        public static final int HEADER_STATE_GONE = 0;
        public static final int HEADER_STATE_VISIBLE = 1;
        public static final int HEADER_STATE_PUSHED_UP = 2;

        /**
         * Get Header State
         * 
         * @param groupPosition
         * @param childPosition
         * @return          
         */
        int getHeaderState(int groupPosition, int childPosition);

        /**
         * Update header view
         * 
         * @param header
         * @param groupPosition
         * @param childPosition
         * @param alpha
         */
        void configureHeaderView(View header, int groupPosition, int childPosition, int alpha);
        void onHeaderClicked(View header, int groupPosition);
    }

    public void setHeaderView(View view) {
        mHeaderView = view;
        
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        if (mHeaderView != null) {
            setFadingEdgeLength(0);
        }

        requestLayout();
    }

    /**
     * Called when header view clicked
     */
    private void headerViewClick() {
        long packedPosition = getExpandableListPosition(this.getFirstVisiblePosition());
        int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

        // Here we let adapter to handle the event
        mAdapter.onHeaderClicked(mHeaderView, groupPosition);
    }

    /**
     * If the header view is visible, check the click event.
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mHeaderViewVisible && mHeaderView != null) {
            Rect outRect = new Rect();
            
            switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) ev.getX();
                mDownY = (int) ev.getY();
                mHeaderView.getHitRect(outRect);
                if(outRect.contains(mDownX, mDownY)){
                    mHeaderPressed = true;
                    return true;
                }else{
                    mHeaderPressed = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                
                if(mHeaderPressed){
                    mHeaderView.getHitRect(outRect);
                    if(outRect.contains(x, y)){
                        headerViewClick();
                        mHeaderPressed = false;
                        return true;
                    }
                }
                mHeaderPressed = false;
                break;
            default:
                mHeaderPressed = false;
                break;
            }
        }

        return super.onTouchEvent(ev);

    }

    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (IphoneHeaderAdapter) adapter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final long flatPostion = getExpandableListPosition(getFirstVisiblePosition());
        final int groupPos = ExpandableListView.getPackedPositionGroup(flatPostion);
        final int childPos = ExpandableListView.getPackedPositionChild(flatPostion);
        int state = mUseDefaultHeaderState ? getHeaderState(groupPos, childPos) : mAdapter.getHeaderState(groupPos, childPos);
        if (mHeaderView != null && mAdapter != null && state != mOldHeaderState) {
            mOldHeaderState = state;
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
        }

        configureHeaderView(groupPos, childPos);
    }

    public void configureHeaderView(int groupPosition, int childPosition) {
        if (mHeaderView == null || mAdapter == null || ((ExpandableListAdapter) mAdapter).getGroupCount() == 0) {
            return;
        }

        int state = mUseDefaultHeaderState ? getHeaderState(groupPosition, childPosition) : mAdapter.getHeaderState(groupPosition, childPosition);

        switch (state) {
        case IphoneHeaderAdapter.HEADER_STATE_GONE: {
            mHeaderViewVisible = false;
            break;
        }

        case IphoneHeaderAdapter.HEADER_STATE_VISIBLE: {
            mAdapter.configureHeaderView(mHeaderView, groupPosition, childPosition, MAX_ALPHA);

            if (mHeaderView.getTop() != 0) {
                mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
            }

            mHeaderViewVisible = true;

            break;
        }

        case IphoneHeaderAdapter.HEADER_STATE_PUSHED_UP: {
            View firstView = getChildAt(0);
            int bottom = firstView.getBottom();

            int headerHeight = mHeaderView.getHeight();

            int y;

            int alpha;

            if (bottom < headerHeight) {
                y = (bottom - headerHeight);
                alpha = MAX_ALPHA * (headerHeight + y) / headerHeight;
            } else {
                y = 0;
                alpha = MAX_ALPHA;
            }

            mAdapter.configureHeaderView(mHeaderView, groupPosition, childPosition, alpha);

            if (mHeaderView.getTop() != y) {
                mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
            }

            mHeaderViewVisible = true;
            break;
        }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderViewVisible) {
            // Draw the group header view
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final long flatPos = getExpandableListPosition(firstVisibleItem);
        int groupPosition = ExpandableListView.getPackedPositionGroup(flatPos);
        int childPosition = ExpandableListView.getPackedPositionChild(flatPos);

        configureHeaderView(groupPosition, childPosition);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
    
    public void enableAdapterHeaderState(){
        mUseDefaultHeaderState = false;
    }
    
    public void disableAdapterHeaderState(){
        mUseDefaultHeaderState = true;
    }
    
    private int getHeaderState(int groupPosition, int childPosition) {
        int childCount = getExpandableListAdapter().getChildrenCount(groupPosition);
        if (childCount == 0) {
            return IphoneHeaderAdapter.HEADER_STATE_GONE;
        } else if (childPosition == childCount - 1) {
            return IphoneHeaderAdapter.HEADER_STATE_PUSHED_UP;
        } else if (childPosition == -1 && !isGroupExpanded(groupPosition)) {
            return IphoneHeaderAdapter.HEADER_STATE_GONE;
        } else {
            return IphoneHeaderAdapter.HEADER_STATE_VISIBLE;
        }
    }
}
